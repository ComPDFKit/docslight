package com.compdf.utils;

import com.compdf.entity.ExtractFieldPojo;
import com.compdf.entity.IdpFile;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.ExtractTemplateDTO;
import com.compdf.pojo.ExtractTemplateV2DTO;
import com.compdf.properties.ResonacProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ComPDFKit-WPH 2025/7/7 星期一
 */
@Slf4j
public class JsonExtractConvert {

    private static final ObjectMapper ORDERED_MAPPER = new ObjectMapper();

    /**
     * 使用Jackson解析JSON字符串为LinkedHashMap，100%保证key的原始顺序。
     */
    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> parseOrderedMap(String jsonStr) {
        try {
            return ORDERED_MAPPER.readValue(jsonStr, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            log.error("parseOrderedMap error: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

    /** 从Map安全取String */
    private static String mapStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : v.toString();
    }

    /** 从Map安全取String，带默认值 */
    private static String mapOpt(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v == null ? def : v.toString();
    }

    @SuppressWarnings("unchecked")
    public static String json2csv(String jsonStr, String outputPath, ExtractTemplateV2DTO extractTemplateDTO) {
        try {
            LinkedHashMap<String, Object> jsonData = parseOrderedMap(jsonStr);
            StringBuilder csvResult = new StringBuilder();

            for (Map.Entry<String, Object> pageEntry : jsonData.entrySet()) {
                String pageKey = pageEntry.getKey();
                LinkedHashMap<String, Object> pageContent = (LinkedHashMap<String, Object>) pageEntry.getValue();
                csvResult.append("# ").append(pageKey).append("\n");

                boolean hasOtherItems = pageContent.keySet().stream().anyMatch(k -> !"tables".equals(k));

                if (hasOtherItems) {
                    csvResult.append("Key,Value\n");
                    for (Map.Entry<String, Object> ce : pageContent.entrySet()) {
                        String key = ce.getKey();
                        if (!"tables".equals(key)) {
                            Object value = ce.getValue();
                            String mappedKey = StringUtils.hasText(Objects.isNull(extractTemplateDTO.getKeys().get(key)) ? "" : extractTemplateDTO.getKeys().get(key).getMapping()) ? extractTemplateDTO.getKeys().get(key).getMapping() : key;
                            csvResult.append(escapeCsv(mappedKey)).append(",").append(escapeCsv(value.toString())).append("\n");
                        }
                    }
                    csvResult.append("\n");
                }

                if (pageContent.containsKey("tables")) {
                    LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) pageContent.get("tables");
                    for (Map.Entry<String, List<LinkedHashMap<String, Object>>> tableEntry : tablesMap.entrySet()) {
                        String tableName = tableEntry.getKey();
                        List<LinkedHashMap<String, Object>> table = tableEntry.getValue();
                        if (table.isEmpty()) continue;

                        List<String> headers = new ArrayList<>(table.get(0).keySet());
                        List<String> newHeaders = new ArrayList<>();
                        Map<String, ExtractFieldPojo> tableHeaderMapping = extractTemplateDTO.getTableHeaders().get(tableName);
                        for (String h : headers) {
                            ExtractFieldPojo fp = tableHeaderMapping != null ? tableHeaderMapping.get(h) : null;
                            newHeaders.add(StringUtils.hasText(Objects.isNull(fp) ? "" : fp.getMapping()) ? fp.getMapping() : h);
                        }

                        csvResult.append(tableName).append("\n");
                        csvResult.append(String.join(",", newHeaders)).append("\n");

                        for (LinkedHashMap<String, Object> row : table) {
                            List<String> rowData = new ArrayList<>();
                            for (String header : headers) {
                                rowData.add(escapeCsv(mapOpt(row, header, "")));
                            }
                            csvResult.append(String.join(",", rowData)).append("\n");
                        }
                        csvResult.append("\n");
                    }
                }
                csvResult.append("\n");
            }
            writeCsvWithBom(Paths.get(outputPath), csvResult.toString());
//            try (FileWriter writer = new FileWriter(outputPath)) {
//                writer.write(csvResult.toString());
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputPath;
    }

    @SuppressWarnings("unchecked")
    public static String json2excel(String jsonStr, String outputPath, ExtractTemplateV2DTO extractTemplateDTO) {
        try {
            LinkedHashMap<String, Object> jsonData = parseOrderedMap(jsonStr);

            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(outputPath)) {

                Sheet textSheet = workbook.createSheet("Text Filed");
                int rowNum = 0;
                Row headerRow = textSheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("Page");
                headerRow.createCell(1).setCellValue("Key");
                headerRow.createCell(2).setCellValue("Value");

                for (Map.Entry<String, Object> pageEntry : jsonData.entrySet()) {
                    String pageKey = pageEntry.getKey();
                    LinkedHashMap<String, Object> pageContent = (LinkedHashMap<String, Object>) pageEntry.getValue();
                    for (Map.Entry<String, Object> ce : pageContent.entrySet()) {
                        String key = ce.getKey();
                        if (!"tables".equals(key)) {
                            Row row = textSheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(pageKey);
                            row.createCell(1).setCellValue(StringUtils.hasText(Objects.isNull(extractTemplateDTO.getKeys().get(key)) ? "" : extractTemplateDTO.getKeys().get(key).getMapping()) ? extractTemplateDTO.getKeys().get(key).getMapping() : key);
                            row.createCell(2).setCellValue(ce.getValue().toString());
                        }
                    }
                }

                for (Map.Entry<String, Object> pageEntry : jsonData.entrySet()) {
                    String pageKey = pageEntry.getKey();
                    LinkedHashMap<String, Object> pageContent = (LinkedHashMap<String, Object>) pageEntry.getValue();
                    if (pageContent.containsKey("tables")) {
                        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) pageContent.get("tables");
                        for (Map.Entry<String, List<LinkedHashMap<String, Object>>> tableEntry : tablesMap.entrySet()) {
                            String tableName = tableEntry.getKey();
                            List<LinkedHashMap<String, Object>> table = tableEntry.getValue();
                            if (table.isEmpty()) continue;

                            String sheetName = pageKey + tableName;
                            if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
                            Sheet sheet = workbook.createSheet(sheetName);
                            int tableRowNum = 0;
                            Row tableHeaderRow = sheet.createRow(tableRowNum++);

                            List<String> headers = new ArrayList<>(table.get(0).keySet());
                            Map<String, ExtractFieldPojo> tableHeaderMapping = extractTemplateDTO.getTableHeaders().get(tableName);
                            for (int h = 0; h < headers.size(); h++) {
                                ExtractFieldPojo fp = tableHeaderMapping != null ? tableHeaderMapping.get(headers.get(h)) : null;
                                tableHeaderRow.createCell(h).setCellValue(StringUtils.hasText(Objects.isNull(fp) ? "" : fp.getMapping()) ? fp.getMapping() : headers.get(h));
                            }

                            for (LinkedHashMap<String, Object> rowData : table) {
                                Row row = sheet.createRow(tableRowNum++);
                                for (int k = 0; k < headers.size(); k++) {
                                    row.createCell(k).setCellValue(mapOpt(rowData, headers.get(k), ""));
                                }
                            }
                        }
                    }
                }
                workbook.write(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputPath;
    }

    public static String json2txt(String jsonStr, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            jsonStr = new String(Files.readAllBytes(Paths.get(jsonStr)));
            LinkedHashMap<String, Object> jsonData = parseOrderedMap(jsonStr);
            writer.write(ORDERED_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return outputPath;
    }

    // CSV转义处理
    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // 从RESULT字符串中提取数值或特定格式的值
    private static String extractNumericOrValue(String result) {
        if (result == null || result.isEmpty()) {
            return "";
        }

        // 处理日期格式 "Sep. 26, 2024" -> "20240926"
        if (result.matches(".*[A-Za-z]+\\.?\\s+\\d+,\\s+\\d{4}.*")) {
//            try {
//                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("MMM. dd, yyyy", java.util.Locale.ENGLISH);
//                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyyMMdd");
//                java.util.Date date = inputFormat.parse(result);
//                return outputFormat.format(date);
//            } catch (Exception e) {
                // 如果解析失败，返回原值
                return result;
//            }
        }

        // 处理 "Passed" 等文本值，尝试从对应的SPECIFICATION中提取数值
        if ("Passed".equalsIgnoreCase(result)) {
            return result;
        }

        // 提取数值（去除单位如 g/ml, %, ppm 等）
        // 匹配数字开头的数值，避免匹配到以.开头的情况
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return result;
    }

    /**
     * 从SPECIFICATION字符串中提取纯数值
     * 例如: "max. 0.3%" -> "0.3", "min. 98.0%" -> "98.0", "max. 5ppm" -> "5"
     */
    private static String extractNumericFromSpec(String spec) {
        if (spec == null || spec.isEmpty()) {
            return "";
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(spec);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return spec;
    }

    // JSON转JSON（根据模板映射键值）
    @SuppressWarnings("unchecked")
    public static void json2json(String json, String path, ExtractTemplateV2DTO extractTemplateDTO) {
        try {
            Map<String, ExtractFieldPojo> keys = extractTemplateDTO.getKeys();
            Map<String, Map<String, ExtractFieldPojo>> tableHandles = extractTemplateDTO.getTableHeaders();

            // 使用Jackson解析JSON，保持key的顺序（LinkedHashMap）
            ObjectMapper objectMapper = new ObjectMapper();
            LinkedHashMap<String, LinkedHashMap<String, Object>> jsonObject =
                    objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, LinkedHashMap<String, Object>>>() {});

            LinkedHashMap<String, Object> newResult = new LinkedHashMap<>();
            // 遍历所有页面键
            for (Map.Entry<String, LinkedHashMap<String, Object>> pageEntry : jsonObject.entrySet()) {
                String pageKey = pageEntry.getKey();
                LinkedHashMap<String, Object> pageData = pageEntry.getValue();
                LinkedHashMap<String, Object> newPageData = new LinkedHashMap<>();

                for (Map.Entry<String, Object> contentEntry : pageData.entrySet()) {
                    String key = contentEntry.getKey();
                    if (!"tables".equals(key)) {
                        String value = String.valueOf(contentEntry.getValue());
                        if (StringUtils.hasText(Objects.isNull(keys.get(key)) ? "" : keys.get(key).getMapping())) {
                            newPageData.put(keys.get(key).getMapping(), value);
                        } else {
                            newPageData.put(key, value);
                        }
                    } else {
                        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) contentEntry.getValue();
                        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> newTablesMap = new LinkedHashMap<>();
                        for (Map.Entry<String, List<LinkedHashMap<String, Object>>> tableEntry : tablesMap.entrySet()) {
                            String tableName = tableEntry.getKey();
                            List<LinkedHashMap<String, Object>> table = tableEntry.getValue();
                            List<LinkedHashMap<String, Object>> newTable = new ArrayList<>();
                            Map<String, ExtractFieldPojo> tableHeaderMapping = tableHandles.get(tableName);
                            for (LinkedHashMap<String, Object> rowData : table) {
                                LinkedHashMap<String, Object> newRowData = new LinkedHashMap<>();
                                for (Map.Entry<String, Object> rowEntry : rowData.entrySet()) {
                                    String rowKey = rowEntry.getKey();
                                    String rowValue = String.valueOf(rowEntry.getValue());
                                    ExtractFieldPojo fp = tableHeaderMapping != null ? tableHeaderMapping.get(rowKey) : null;
                                    if (StringUtils.hasText(Objects.isNull(fp) ? "" : fp.getMapping())) {
                                        newRowData.put(fp.getMapping(), rowValue);
                                    } else {
                                        newRowData.put(rowKey, rowValue);
                                    }
                                }
                                newTable.add(newRowData);
                            }
                            newTablesMap.put(tableName, newTable);
                        }
                        newPageData.put(key, newTablesMap);
                    }
                }
                newResult.put(pageKey, newPageData);
            }
            // 将新的JSON对象写入文件, 覆盖原有文件内容
            String resultJson = objectMapper.writeValueAsString(newResult);
            Files.write(Paths.get(path), resultJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }
    }
    public static void json2csvOnResonac(String json, String path, ResonacProperties resonacProperties, ExtractTemplateDTO extractTemplateDTO) {
        int index = resonacProperties.getTemplateList().indexOf(extractTemplateDTO.getId());
        if (index == -1) {
            json2csv(json, path, new ExtractTemplateV2DTO(extractTemplateDTO));
            return;
        }
        try {
//            JSONObject jsonObject = JsonUtils.jsonStringToBean(json, JSONObject.class);
            JSONObject jsonObject = new JSONObject(json);
            JSONObject pageData = jsonObject.getJSONObject("Page_1");
            StringBuilder csvContent = new StringBuilder();
            Path outPath = Paths.get(path);
            switch (index + 1) {
                case 1:
                    // 第1行: "Lot Number","值"
                    csvContent.append("\"Lot Number\",\"").append(pageData.getString("Lot Number")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");
                    // 获取tables数组
                    JSONArray tables = pageData.getJSONArray("tables");
                    if (tables.length() > 0) {
                        JSONArray table = tables.getJSONArray(0);
                        StringBuilder headerRow = new StringBuilder();
                        StringBuilder valueRow = new StringBuilder();
                        for (int i = 0; i < table.length(); i++) {
                            JSONObject row = table.getJSONObject(i);
                            String testName = row.getString("TEST");
                            String result = row.getString("RESULT");
                            // 当RESULT为Passed时，从SPECIFICATION中提取纯数值
                            if ("Passed".equalsIgnoreCase(result)) {
                                String spec = row.optString("SPECIFICATION", "");
                                result = extractNumericFromSpec(spec);
                            } else {
                                // 处理RESULT值，去除单位等
                                result = extractNumericOrValue(result);
                            }
                            if (i > 0) {
                                headerRow.append(",");
                                valueRow.append(",");
                            }
                            // 带双引号输出
                            headerRow.append("\"").append(testName).append("\"");
                            valueRow.append("\"").append(result).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");
                    // 写入文件
                    writeCsvWithBom(outPath, csvContent.toString());
                    // Files.write(outPath, csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 2:
                    // 获取tables数组
                    JSONArray tables2 = pageData.getJSONArray("tables");
                    if (tables2.length() > 0) {
                        JSONArray table2 = tables2.getJSONArray(0);
                        int columnCount = table2.length();

                        // 收集所有Type of Test作为表头
                        StringBuilder headerRow2 = new StringBuilder();
                        for (int i = 0; i < columnCount; i++) {
                            JSONObject row = table2.getJSONObject(i);
                            String typeOfTest = row.getString("Type of Test");
                            if (i > 0) {
                                headerRow2.append(",");
                            }
                            headerRow2.append(typeOfTest);
                        }

                        // 获取第一个Sample Name对象来确定样本名称列表
                        JSONObject firstRowObj = table2.getJSONObject(0);
                        JSONObject sampleNameObj = firstRowObj.getJSONObject("Sample Name");
                        List<String> sampleNameList = new ArrayList<>();
                        Iterator<String> sampleKeys = sampleNameObj.keys();
                        while (sampleKeys.hasNext()) {
                            sampleNameList.add(sampleKeys.next());
                        }
                        String[] sampleNames = sampleNameList.toArray(new String[0]);

                        // 按样本顺序排序（基于Lot号）
                        Arrays.sort(sampleNames, (a, b) -> {
                            String lotA = a.replaceAll(".*Lot:([^)]+).*", "$1");
                            String lotB = b.replaceAll(".*Lot:([^)]+).*", "$1");
                            return lotA.compareTo(lotB);
                        });

                        // 生成空行（列数-1个逗号）
                        StringBuilder emptyRow = new StringBuilder();
                        for (int i = 0; i < columnCount - 1; i++) {
                            emptyRow.append(",");
                        }

                        // 为每个样本生成一个CSV文件
                        for (int sampleIdx = 0; sampleIdx < sampleNames.length; sampleIdx++) {
                            String sampleName = sampleNames[sampleIdx];
                            // 从样本名称中提取Lot号
                            String lotNumber = sampleName.replaceAll(".*Lot:([^)]+).*", "$1");

                            StringBuilder csvContentN = new StringBuilder();
                            // 第1行: Lot Number,批次号,空列...
                            csvContentN.append("Lot Number,").append(lotNumber);
                            for (int i = 0; i < columnCount - 2; i++) {
                                csvContentN.append(",");
                            }
                            csvContentN.append("\n");
                            // 第2行: 空行
                            csvContentN.append(emptyRow).append("\n");
                            // 第3行: 表头
                            csvContentN.append(headerRow2).append("\n");
                            // 第4行: 数据值
                            StringBuilder valueRow2 = new StringBuilder();
                            for (int i = 0; i < columnCount; i++) {
                                JSONObject row = table2.getJSONObject(i);
                                JSONObject sampleValues = row.getJSONObject("Sample Name");
                                String value = sampleValues.getString(sampleName);
                                // 去除 < 符号
                                value = value.replace("<", "");
                                if (i > 0) {
                                    valueRow2.append(",");
                                }
                                valueRow2.append(value);
                            }
                            csvContentN.append(valueRow2).append("\n");
                            // 第5行: 空行
                            csvContentN.append("\n");

                            // 写入文件
                            String outCsvPath = path.replace(".csv", "_" + (sampleIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath), csvContentN.toString());
                            // Files.write(Paths.get(outCsvPath), csvContentN.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 3:
                    // 获取JSON数据（模板3格式，没有Page_1层级）
//                    JSONObject jsonData3 = new JSONObject(json);

                    // 第1行: "ロットNo.","值"
                    String lotN = pageData.getString("ロットNo.");
                    csvContent.append("\"ロットNo.\",\"").append(lotN).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables3 = pageData.getJSONArray("tables");
                    if (tables3.length() > 0) {
                        JSONArray table3 = tables3.getJSONArray(0);
                        StringBuilder headerRow3 = new StringBuilder();
                        StringBuilder valueRow3 = new StringBuilder();

                        for (int i = 0; i < table3.length(); i++) {
                            JSONObject row = table3.getJSONObject(i);
                            String itemName = row.getString("検査項目");
                            String itemValue = row.getString("検査結果");

                            if (i > 0) {
                                headerRow3.append(",");
                                valueRow3.append(",");
                            }
                            // 带引号输出
                            headerRow3.append("\"").append(itemName).append("\"");
                            valueRow3.append("\"").append(itemValue).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow3).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow3).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(outPath, csvContent.toString());
                    // Files.write(outPath, csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;

                case 4:
                    // 获取JSON数据（模板4格式）
//                    JSONObject jsonData4 = new JSONObject(json);
                    JSONArray tables4 = pageData.getJSONArray("tables");
                    if (tables4.length() > 0) {
                        JSONArray table4 = tables4.getJSONArray(0);

                        // 定义表头顺序（除Lot No.外）
                        String[] headers4 = {"製造年月日", "保証期間", "外観", "NV", "比重", "粘度", "pH",
                                "イソプロピルアクリレート濃度", "Na", "K", "Fe", "Al", "Cl", "色相", "数量", "（缶数）"};

                        // 为每个批次生成一个CSV文件
                        for (int rowIdx = 0; rowIdx < table4.length(); rowIdx++) {
                            JSONObject row = table4.getJSONObject(rowIdx);
                            StringBuilder csvContent4 = new StringBuilder();

                            // 第1行: Lot No.,值
                            csvContent4.append("Lot No.,").append(row.getString("Lot No.")).append("\n");
                            // 第2行: 空行
                            csvContent4.append("\n");
                            // 第3行: 表头
                            csvContent4.append(String.join(",", headers4)).append("\n");
                            // 第4行: 数据值
                            StringBuilder valueRow4 = new StringBuilder();
                            for (int i = 0; i < headers4.length; i++) {
                                String value;
                                try {
                                    value = row.getString(headers4[i]);
                                } catch (JSONException e) {
                                    value = "";
                                }
                                if (StringUtils.isEmpty(value)){
                                    value = "";
                                }else {
                                    // 处理日期格式 "2025/8/21" -> "20250821"
                                    if (headers4[i].equals("製造年月日") || headers4[i].equals("保証期間")) {
                                        value = value.replace("/", "");
                                        // 补齐日期格式，如 "202581" -> "20250801" 不对，应该是按/分割处理
                                        String[] dateParts = row.getString(headers4[i]).split("/");
                                        if (dateParts.length == 3) {
                                            String year = dateParts[0];
                                            String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
                                            String day = dateParts[2].length() == 1 ? "0" + dateParts[2] : dateParts[2];
                                            value = year + month + day;
                                        }
                                    }
                                    // 去除 < 符号
                                    value = value.replace("<", "");
                                    // 淡黃色 -> 淡黄色（日文汉字转换）
                                    value = value.replace("黃", "黄");
                                }
                                if (i > 0) {
                                    valueRow4.append(",");
                                }
                                valueRow4.append(value);
                            }
                            csvContent4.append(valueRow4).append("\n");
                            // 第5行: 空行
                            csvContent4.append("\n");

                            // 写入文件
                            String outCsvPath4 = path.replace(".csv", "_" + (rowIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath4), csvContent4.toString());
                            // Files.write(Paths.get(outCsvPath4), csvContent4.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 5:
                    // 获取JSON数据（模板5格式，简单键值对）
//                    JSONObject jsonData5 = new JSONObject(json);

                    // 第1行: Lot No.,值,,
                    csvContent.append("Lot No.,").append(pageData.getString("Lot No.")).append(",,\n");
                    // 第2行: 空行
                    csvContent.append(",,,\n");

                    // 第3行: 表头（固定顺序）
                    csvContent.append("Appearance,Manufacturing date,Assay(%),Melting point(°c)\n");

                    // 第4行: 数据值
                    String appearance5 = pageData.getString("Appearance");
                    // 日期格式转换 "14.05.2025" -> "20250514"
                    String mfgDate5 = pageData.getString("Manufacturing date");
//                    String[] dateParts5 = mfgDate5.split("\\.");
//                    if (dateParts5.length == 3) {
//                        mfgDate5 = dateParts5[2] + dateParts5[1] + dateParts5[0];
//                    }
                    String assay5 = pageData.getString("Assay(%)");
                    // Melting point取整 "107.4" -> "107"
                    String meltingPoint5 = pageData.getString("Melting point(°c)");
                    if (meltingPoint5.contains(".")) {
                        meltingPoint5 = meltingPoint5.substring(0, meltingPoint5.indexOf("."));
                    }
                    csvContent.append(appearance5).append(",").append(mfgDate5).append(",")
                            .append(assay5).append(",").append(meltingPoint5).append("\n");
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                   // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 6:
                    // 获取JSON数据（模板6格式）
//                    JSONObject jsonData6 = new JSONObject(json);

                    // 第1行: Lot番号,值
                    csvContent.append("Lot番号,").append(pageData.getString("Lot番号")).append("\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables6 = pageData.getJSONArray("tables");
                    if (tables6.length() > 0) {
                        JSONArray table6 = tables6.getJSONArray(0);
                        StringBuilder headerRow6 = new StringBuilder();
                        StringBuilder valueRow6 = new StringBuilder();

                        for (int i = 0; i < table6.length(); i++) {
                            JSONObject row = table6.getJSONObject(i);
                            String itemName = row.getString("試験項目");
                            String itemValue = row.getString("分析値");

                            // 处理数值：去除"以下"，去除小数点后的0
                            itemValue = itemValue.replace("以下", "");
                            // 处理小数 "10.0" -> "10", "1.0" -> "1"
                            if (itemValue.matches("\\d+\\.0")) {
                                itemValue = itemValue.substring(0, itemValue.indexOf("."));
                            }

                            if (i > 0) {
                                headerRow6.append(",");
                                valueRow6.append(",");
                            }
                            headerRow6.append(itemName);
                            valueRow6.append(itemValue);
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow6).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow6).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                   // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 7:
                    // 获取JSON数据（模板7格式）
//                    JSONObject jsonData7 = new JSONObject(json);

                    // 第1行: "Lot No.","值"
                    csvContent.append("\"Lot No.\",\"").append(pageData.getString("Lot No.")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables7 = pageData.getJSONArray("tables");
                    if (tables7.length() > 0) {
                        JSONArray table7 = tables7.getJSONArray(0);
                        StringBuilder headerRow7 = new StringBuilder();
                        StringBuilder valueRow7 = new StringBuilder();

                        for (int i = 0; i < table7.length(); i++) {
                            JSONObject row = table7.getJSONObject(i);
                            String testItem = row.getString("TEST ITEM");
                            String testResult = row.getString("TEST RESULT");

                            if (i > 0) {
                                headerRow7.append(",");
                                valueRow7.append(",");
                            }
                            // 带双引号输出
                            headerRow7.append("\"").append(testItem).append("\"");
                            valueRow7.append("\"").append(testResult).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow7).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow7).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    //Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 8:
                    // 获取JSON数据（模板8格式）
//                    JSONObject jsonData8 = new JSONObject(json);

                    // 第1行: "製造番号（LOTNo.）","值"
                    csvContent.append("\"製造番号（LOTNo.）\",\"").append(pageData.getString("製造番号（LOTNo.）")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables8 = pageData.getJSONArray("tables");
                    if (tables8.length() > 0) {
                        JSONArray table8 = tables8.getJSONArray(0);
                        StringBuilder headerRow8 = new StringBuilder();
                        StringBuilder valueRow8 = new StringBuilder();

                        for (int i = 0; i < table8.length(); i++) {
                            JSONObject row = table8.getJSONObject(i);
                            String itemName = row.getString("試験項目");
                            String itemValue = row.getString("測定値");

                            if (i > 0) {
                                headerRow8.append(",");
                                valueRow8.append(",");
                            }
                            // 带双引号输出
                            headerRow8.append("\"").append(itemName).append("\"");
                            valueRow8.append("\"").append(itemValue).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow8).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow8).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 9:
                    // 获取JSON数据（模板9格式）
//                    JSONObject jsonData9 = new JSONObject(json);

                    // 第1行: "ロットNo.","值"
                    csvContent.append("\"ロットNo.\",\"").append(pageData.getString("ロットNo.")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables9 = pageData.getJSONArray("tables");
                    if (tables9.length() > 0) {
                        JSONArray table9 = tables9.getJSONArray(0);
                        StringBuilder headerRow9 = new StringBuilder();
                        StringBuilder valueRow9 = new StringBuilder();

                        boolean isFirst9 = true;
                        for (int i = 0; i < table9.length(); i++) {
                            JSONObject row = table9.getJSONObject(i);
                            String itemName = row.getString("項目");
                            String itemValue = row.getString("結果");
                            String judgmentValue = row.getString("判定");
                            // 特殊处理"全長"字段：按空格切分成多个值
                            if ("全長".equals(itemName) && itemValue.contains(" ")) {
                                String[] values = itemValue.split(" ");
                                for (String val : values) {
                                    if (!isFirst9) {
                                        headerRow9.append(",");
                                        valueRow9.append(",");
                                    }
                                    isFirst9 = false;
                                    headerRow9.append("\"").append(itemName).append("\"");
                                    valueRow9.append("\"").append(val).append("\"");
                                }
                            } else if ("外観".equals(itemName)){
                                if (!isFirst9) {
                                    headerRow9.append(",");
                                    valueRow9.append(",");
                                }
                                isFirst9 = false;
                                // 带双引号输出
                                headerRow9.append("\"").append(itemName).append("\"");
                                valueRow9.append("\"").append(judgmentValue).append("\"");
                            } else {
                                if (!isFirst9) {
                                    headerRow9.append(",");
                                    valueRow9.append(",");
                                }
                                isFirst9 = false;
                                // 带双引号输出
                                headerRow9.append("\"").append(itemName).append("\"");
                                valueRow9.append("\"").append(itemValue).append("\"");
                            }
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow9).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow9).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    //Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 10:
                    // 获取JSON数据（模板10格式）
//                    JSONObject jsonData10 = new JSONObject(json);

                    // 第1行: LOT NO.,值
                    csvContent.append("LOT NO.,").append(pageData.getString("LOT NO.")).append("\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables10 = pageData.getJSONArray("tables");
                    if (tables10.length() > 0) {
                        JSONArray table10 = tables10.getJSONArray(0);
                        StringBuilder headerRow10 = new StringBuilder();
                        StringBuilder valueRow10 = new StringBuilder();

                        for (int i = 0; i < table10.length(); i++) {
                            JSONObject row = table10.getJSONObject(i);
                            String itemName = row.getString("検查項目");
                            String itemValue = row.getString("測定値");
                            String judgmentValue = row.getString("判定");

                            // 去除单位 "kPa", "mm" 等
                            itemValue = itemValue.replace(" kPa", "").replace(" mm", "");

                            if (i > 0) {
                                headerRow10.append(",");
                                valueRow10.append(",");
                            }
                            headerRow10.append(itemName);
                            valueRow10.append(StringUtils.hasText(itemValue.trim()) ? itemValue : judgmentValue);
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow10).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow10).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                   // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 11:
                    // 获取JSON数据（模板11格式）
//                    JSONObject jsonData11 = new JSONObject(json);

                    // 第1行: "製造ロット","值"
                    csvContent.append("\"製造ロット\",\"").append(pageData.getString("製造ロット")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables11 = pageData.getJSONArray("tables");
                    if (tables11.length() > 0) {
                        JSONArray table11 = tables11.getJSONArray(0);
                        StringBuilder headerRow11 = new StringBuilder();
                        StringBuilder valueRow11 = new StringBuilder();

                        boolean isFirst11 = true;
                        StringBuilder headerRow11_2 = new StringBuilder();
                        StringBuilder valueRow11_2 = new StringBuilder();
                        boolean isFirst11_2 = true;

                        for (int i = 0; i < table11.length(); i++) {
                            JSONObject row = table11.getJSONObject(i);
                            String itemName = row.getString("試験項目");
                            String itemValue = row.getString("型番");

                            // 按"-"切分值成两个值（型番）
                            if (itemValue.contains("-")) {
                                String[] values = itemValue.split("-", 2);
                                for (String val : values) {
                                    if (!isFirst11) {
                                        headerRow11.append(",");
                                        valueRow11.append(",");
                                    }
                                    isFirst11 = false;
                                    headerRow11.append("\"").append(itemName).append("\"");
                                    valueRow11.append("\"").append(val).append("\"");
                                }
                            } else {
                                if (!isFirst11) {
                                    headerRow11.append(",");
                                    valueRow11.append(",");
                                }
                                isFirst11 = false;
                                headerRow11.append("\"").append(itemName).append("\"");
                                valueRow11.append("\"").append(itemValue).append("\"");
                            }

                            // 型番2: 写入规则和型番一致
                            String itemValue2 = row.optString("型番2", "");
                            if (StringUtils.hasText(itemValue2)) {
                                if (itemValue2.contains("-")) {
                                    String[] values2 = itemValue2.split("-", 2);
                                    for (String val : values2) {
                                        if (!isFirst11_2) {
                                            headerRow11_2.append(",");
                                            valueRow11_2.append(",");
                                        }
                                        isFirst11_2 = false;
                                        headerRow11_2.append("\"").append(itemName).append("\"");
                                        valueRow11_2.append("\"").append(val).append("\"");
                                    }
                                } else {
                                    if (!isFirst11_2) {
                                        headerRow11_2.append(",");
                                        valueRow11_2.append(",");
                                    }
                                    isFirst11_2 = false;
                                    headerRow11_2.append("\"").append(itemName).append("\"");
                                    valueRow11_2.append("\"").append(itemValue2).append("\"");
                                }
                            }
                        }
//                        // 第3行: 表头（型番）
                        csvContent.append(headerRow11).append("\n");
                        // 第4行: 数据值（型番）
                        csvContent.append(valueRow11).append("\n");
                        // 型番2数据（如果存在）
                        if (headerRow11_2.length() > 0) {
//                            csvContent.append(headerRow11_2).append("\n");
                            csvContent.append(valueRow11_2).append("\n");
                        }
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 12:
                    // 获取JSON数据（模板12格式）
//                    JSONObject jsonData12 = new JSONObject(json);
                    JSONArray tables12 = pageData.getJSONArray("tables");
                    if (tables12.length() > 0) {
                        JSONArray table12 = tables12.getJSONArray(0);

                        // 定义表头顺序（除Lot No.外）
                        String[] headers12 = {"Manufacturing date", "Appearance", "Solubility", "Melting point(°C)",
                                "Arsenic(As203)(μg/g)", "Heavy metals(Pb)(μg/g)", "Residue on ignition(%)",
                                "Loss on drying(%)", "Assay(%)"};

                        // 为每个批次生成一个CSV文件
                        for (int rowIdx = 0; rowIdx < table12.length(); rowIdx++) {
                            JSONObject row = table12.getJSONObject(rowIdx);
                            StringBuilder csvContent12 = new StringBuilder();

                            // 第1行: Lot No.,值
                            csvContent12.append("Lot No.,").append(row.getString("Lot No.")).append("\n");
                            // 第2行: 空行
                            csvContent12.append("\n");
                            // 第3行: 表头
                            csvContent12.append(String.join(",", headers12)).append("\n");
                            // 第4行: 数据值
                            StringBuilder valueRow12 = new StringBuilder();
                            for (int i = 0; i < headers12.length; i++) {
                                String value = row.getString(headers12[i]);
                                if (i > 3){
                                    value = extractNumericFromSpec(value);
                                }

                                // 处理日期格式 "08.09.2023" -> "20230908"
//                                if (headers12[i].equals("Manufacturing date")) {
//                                    String[] dateParts = value.split("\\.");
//                                    if (dateParts.length == 3) {
//                                        value = dateParts[2] + dateParts[1] + dateParts[0];
//                                    }
//                                }
                                if (i > 0) {
                                    valueRow12.append(",");
                                }
                                valueRow12.append(value);
                            }
                            csvContent12.append(valueRow12).append("\n");
                            // 第5行: 空行
                            csvContent12.append("\n");

                            // 写入文件
                            String outCsvPath12 = path.replace(".csv", "_" + (rowIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath12), csvContent12.toString());
                            // Files.write(Paths.get(outCsvPath12), csvContent12.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 13:
                    // 获取JSON数据（模板13格式）
//                    JSONObject jsonData13 = new JSONObject(json);

                    // 第1行: "ロット表示","值"
                    String date = pageData.getString("ロット表示"); // 2025年11月7日 -> 20251107
                    // 尝试解析 "2025年11月7日" 格式的日期为 yyyyMMdd
                    java.util.regex.Matcher dateMatcher = java.util.regex.Pattern.compile("(\\d{4})\\D+(\\d{1,2})\\D+(\\d{1,2})").matcher(date);
                    if (dateMatcher.find()) {
                        String year = dateMatcher.group(1);
                        String month = dateMatcher.group(2).length() == 1 ? "0" + dateMatcher.group(2) : dateMatcher.group(2);
                        String day = dateMatcher.group(3).length() == 1 ? "0" + dateMatcher.group(3) : dateMatcher.group(3);
                        date = year + month + day;
                    } else {
                        date = date.replaceAll("[^0-9]", "");
                    }

                    csvContent.append("\"Lot No.\",\"").append(date).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables13 = pageData.getJSONArray("tables");
                    if (tables13.length() > 0) {
                        JSONArray table13 = tables13.getJSONArray(0);
                        StringBuilder headerRow13 = new StringBuilder();
                        StringBuilder valueRow1 = new StringBuilder();
                        StringBuilder valueRow2 = new StringBuilder();
                        StringBuilder valueRow3 = new StringBuilder();

                        for (int i = 0; i < table13.length(); i++) {
                            JSONObject row = table13.getJSONObject(i);
                            String itemName = row.getString("検査項⽬");
                            String val1 = row.getString("1");
                            String val2 = row.getString("2");
                            String val3 = row.getString("3");

                            if (i > 0) {
                                headerRow13.append(",");
                                valueRow1.append(",");
                                valueRow2.append(",");
                                valueRow3.append(",");
                            }
                            // 带双引号输出
                            headerRow13.append("\"").append(itemName).append("\"");
                            valueRow1.append("\"").append(val1).append("\"");
                            valueRow2.append("\"").append(val2).append("\"");
                            valueRow3.append("\"").append(val3).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow13).append("\n");
                        // 第4行: 数据值1
                        csvContent.append(valueRow1).append("\n");
                        // 第5行: 数据值2
                        csvContent.append(valueRow2).append("\n");
                        // 第6行: 数据值3
                        csvContent.append(valueRow3).append("\n");
                    }
                    // 第7行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                   // Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 14:
                    // 获取JSON数据（模板14格式）
//                    JSONObject jsonData14 = new JSONObject(json);

                    // 第1行: "製品ロット","值"
                    csvContent.append("\"製品ロット\",\"").append(pageData.getString("製品ロット")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables14 = pageData.getJSONArray("tables");
                    if (tables14.length() > 0) {
                        JSONArray table14 = tables14.getJSONArray(0);
                        StringBuilder headerRow14 = new StringBuilder();
                        StringBuilder valueRow14 = new StringBuilder();

                        for (int i = 0; i < table14.length(); i++) {
                            JSONObject row = table14.getJSONObject(i);
                            String itemName = row.getString("檢查項⽬");
                            String itemValue = row.getString("測定");

                            if (i > 0) {
                                headerRow14.append(",");
                                valueRow14.append(",");
                            }
                            // 带双引号输出
                            headerRow14.append("\"").append(itemName).append("\"");
                            valueRow14.append("\"").append(itemValue).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow14).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow14).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    //Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 15:
                    // 获取JSON数据（模板15格式）
//                    JSONObject jsonData15 = new JSONObject(json);

                    // 第1行: "Lot No.","值" - 从"ロット表示"字段提取，写入时使用"Lot No."
                    csvContent.append("\"Lot No.\",\"").append(pageData.optString("LOT No.", "")).append("\"\n");
                    // 第2行: "MFG. DATE","值" - 日期格式固定为 "yyyyMMdd"
                    String mfgDate15 = pageData.optString("MFG. DATE", "");
                    // 处理 "22-04-29" 格式 -> "20220429"
//                    if (mfgDate15.contains("-")) {
//                        String[] mfgParts = mfgDate15.split("-");
//                        if (mfgParts.length == 3) {
//                            String year = mfgParts[0].length() == 2 ? "20" + mfgParts[0] : mfgParts[0];
//                            mfgDate15 = year + mfgParts[1] + mfgParts[2];
//                        }
//                    }
                    csvContent.append("\"MFG. DATE\",\"").append(mfgDate15).append("\"\n");
                    // 第3行: "EXPIRATION DATE","值" - 日期格式固定为 "yyyyMMdd"
                    String expDate15 = pageData.optString("EXPIRATION DATE", "");
                    // 处理 "22-10-28" 格式 -> "20221028"
//                    if (expDate15.contains("-")) {
//                        String[] expParts = expDate15.split("-");
//                        if (expParts.length == 3) {
//                            String year = expParts[0].length() == 2 ? "20" + expParts[0] : expParts[0];
//                            expDate15 = year + expParts[1] + expParts[2];
//                        }
//                    }
                    csvContent.append("\"EXPIRATION DATE\",\"").append(expDate15).append("\"\n");
                    // 第4行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables15 = pageData.getJSONArray("tables");
                    if (tables15.length() > 0) {
                        JSONArray table15 = tables15.getJSONArray(0);
                        StringBuilder headerRow15 = new StringBuilder();
                        StringBuilder valueRow15 = new StringBuilder();

                        for (int i = 0; i < table15.length(); i++) {
                            JSONObject row = table15.getJSONObject(i);
                            String itemName = row.getString("ITEM");
                            String itemValue = row.getString("RESULT");

                            if (i > 0) {
                                headerRow15.append(",");
                                valueRow15.append(",");
                            }
                            // 带双引号输出
                            headerRow15.append("\"").append(itemName).append("\"");
                            valueRow15.append("\"").append(itemValue).append("\"");
                        }
                        // 第5行: 表头
                        csvContent.append(headerRow15).append("\n");
                        // 第6行: 数据值
                        csvContent.append(valueRow15).append("\n");
                    }
                    // 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    //Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 16:
                    // 获取JSON数据（模板16格式）
//                    JSONObject jsonData16 = new JSONObject(json);

                    // 第1行: "Lot No.","值"
                    csvContent.append("\"Lot No.\",\"").append(pageData.getString("Lot No.")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables16 = pageData.getJSONArray("tables");
                    if (tables16.length() > 0) {
                        JSONArray table16 = tables16.getJSONArray(0);
                        StringBuilder headerRow16 = new StringBuilder();
                        StringBuilder valueRow16 = new StringBuilder();

                        for (int i = 0; i < table16.length(); i++) {
                            JSONObject row = table16.getJSONObject(i);
                            String testItem = row.getString("Test Item");
                            String testResult = row.getString("Test Result");

                            if (i > 0) {
                                headerRow16.append(",");
                                valueRow16.append(",");
                            }
                            // 带双引号输出
                            headerRow16.append("\"").append(testItem).append("\"");
                            valueRow16.append("\"").append(testResult).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow16).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow16).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(Paths.get(path), csvContent.toString());
                    //Files.write(Paths.get(path), csvContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    break;
                case 17:
                    // 获取JSON数据（模板17格式）
//                    JSONObject jsonData17 = new JSONObject(json);
                    JSONArray tables17 = pageData.getJSONArray("tables");
                    if (tables17.length() > 0) {
                        JSONArray table17 = tables17.getJSONArray(0);

                        // 定义表头顺序（除Serial Number外）
                        String[] headers17 = {"Nominal Cut-off Molecular Weight", "Permeate(Liter/Hr at 100kPa 25°C)",
                                "Visual inspection", "Dimension", "Final Bubble Test", "Final Judgement"};

                        // 为每个批次生成一个CSV文件
                        for (int rowIdx = 0; rowIdx < table17.length(); rowIdx++) {
                            JSONObject row = table17.getJSONObject(rowIdx);
                            StringBuilder csvContent17 = new StringBuilder();

                            // 第1行: "Serial Number","值"
                            csvContent17.append("\"Serial Number\",\"").append(row.getString("Serial Number")).append("\"\n");
                            // 第2行: 空行
                            csvContent17.append("\n");
                            // 第3行: 表头（带双引号）
                            StringBuilder headerRow17 = new StringBuilder();
                            for (int i = 0; i < headers17.length; i++) {
                                if (i > 0) {
                                    headerRow17.append(",");
                                }
                                headerRow17.append("\"").append(headers17[i]).append("\"");
                            }
                            csvContent17.append(headerRow17).append("\n");
                            // 第4行: 数据值（带双引号）
                            StringBuilder valueRow17 = new StringBuilder();
                            for (int i = 0; i < headers17.length; i++) {
                                if (i > 0) {
                                    valueRow17.append(",");
                                }
                                valueRow17.append("\"").append(row.getString(headers17[i])).append("\"");
                            }
                            csvContent17.append(valueRow17).append("\n");
                            // 第5行: 空行
                            csvContent17.append("\n");

                            // 写入文件
                            String outCsvPath17 = path.replace(".csv", "_" + (rowIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath17), csvContent17.toString());
                           // Files.write(Paths.get(outCsvPath17), csvContent17.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 18:
                    // 获取JSON数据（模板18格式）
//                    JSONObject jsonData18 = new JSONObject(json);

                    // 解析LOT NO. "25091211-25100811" -> ["25091211", "25100811"]
                    String lotNoStr = pageData.getString("LOT NO.");
                    String[] lotNos = lotNoStr.split("-");

                    // 解析PRODUCTION DATE "September 12,2025-October 08,2025"
                    String prodDateStr = pageData.getString("PRODUCTION DATE");
                    String[] prodDates = prodDateStr.split("-");

                    JSONArray tables18 = pageData.getJSONArray("tables");
                    if (tables18.length() > 0) {
                        JSONArray table18 = tables18.getJSONArray(0);

                        // 为每个批次生成一个CSV文件
                        for (int lotIdx = 0; lotIdx < lotNos.length; lotIdx++) {
                            StringBuilder csvContent18 = new StringBuilder();
                            String lotNo = lotNos[lotIdx].trim();
                            String prodDate = prodDates[lotIdx].trim();

                            // 第1行: "LOT NO.","值"
                            csvContent18.append("\"LOT NO.\",\"").append(lotNo).append("\"\n");
                            // 第2行: "PRODUCTION DATE","值"
                            csvContent18.append("\"PRODUCTION DATE\",\"").append(prodDate).append("\"\n");
                            // 第3行: 空行
                            csvContent18.append("\n");

                            // 收集表头和数据
                            StringBuilder headerRow18 = new StringBuilder();
                            StringBuilder valueRow18 = new StringBuilder();

                            for (int i = 0; i < table18.length(); i++) {
                                JSONObject row = table18.getJSONObject(i);
                                String itemName = row.getString("ITEM");
                                String resultKey = "RESULT" + (lotIdx + 1);
                                String itemValue = row.getString(resultKey);
                                if (!itemName.equals("Manufacture data") && !itemName.equals("Expiry date")){
                                    itemValue = extractNumericOrValue(itemValue);
                                }
                                if (i > 0) {
                                    headerRow18.append(",");
                                    valueRow18.append(",");
                                }
                                headerRow18.append("\"").append(itemName).append("\"");
                                valueRow18.append("\"").append(itemValue).append("\"");
                            }

//                            // 添加额外的日期列
//                            // 从lotNo提取日期 "25091211" -> "20250912"
//                            String mfgDate = "20" + lotNo.substring(0, 6);
//                            // 有效期为生产日期+1年
//                            String expDate = String.valueOf(Integer.parseInt(mfgDate.substring(0, 4)) + 1) + mfgDate.substring(4);
//
//                            headerRow18.append(",Manufactare data,Expiry date,PRODUCTION DATE");
//                            valueRow18.append(",").append(mfgDate).append(",").append(expDate).append(",").append(mfgDate);

                            // 第3行: 表头
                            csvContent18.append(headerRow18).append("\n");
                            // 第4行: 数据值
                            csvContent18.append(valueRow18).append("\n");
                            // 第5行: 空行
                            csvContent18.append("\n");

                            // 写入文件
                            String outCsvPath18 = path.replace(".csv", "_" + (lotIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath18), csvContent18.toString());
//                            Files.write(Paths.get(outCsvPath18), csvContent18.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 19:
                    // 获取JSON数据（模板4格式）
//                    JSONObject jsonData4 = new JSONObject(json);
                    JSONArray tables19 = pageData.getJSONArray("tables");
                    if (tables19.length() > 0) {
                        JSONArray table4 = tables19.getJSONArray(0);

                        // 定义表头顺序（除Lot No.外）
                        String[] headers4 = {"製造年月日", "保証期間", "外観", "NV", "比重", "粘度", "pH",
                                "參考値(*)", "Na", "K", "Fe", "Al", "Cl", "数量", "（缶数）"};

                        // 为每个批次生成一个CSV文件
                        for (int rowIdx = 0; rowIdx < table4.length(); rowIdx++) {
                            JSONObject row = table4.getJSONObject(rowIdx);
                            StringBuilder csvContent4 = new StringBuilder();

                            // 第1行: Lot No.,值
                            csvContent4.append("Lot No.,").append(row.getString("Lot No.")).append("\n");
                            // 第2行: 空行
                            csvContent4.append("\n");
                            // 第3行: 表头
                            csvContent4.append(String.join(",", headers4)).append("\n");
                            // 第4行: 数据值
                            StringBuilder valueRow4 = new StringBuilder();
                            for (int i = 0; i < headers4.length; i++) {
                                String value;
                                try {
                                    value = row.getString(headers4[i]);
                                } catch (JSONException e) {
                                    value = "";
                                }
                                if (StringUtils.isEmpty(value)){
                                    value = "";
                                }else {
                                    // 处理日期格式 "2025/8/21" -> "20250821"
                                    if (headers4[i].equals("製造年月日") || headers4[i].equals("保証期間")) {
                                        value = value.replace("/", "");
                                        // 补齐日期格式，如 "202581" -> "20250801" 不对，应该是按/分割处理
                                        String[] dateParts = row.getString(headers4[i]).split("/");
                                        if (dateParts.length == 3) {
                                            String year = dateParts[0];
                                            String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
                                            String day = dateParts[2].length() == 1 ? "0" + dateParts[2] : dateParts[2];
                                            value = year + month + day;
                                        }
                                    }
                                    // 去除 < 符号
                                    value = value.replace("<", "");
                                    // 淡黃色 -> 淡黄色（日文汉字转换）
                                    value = value.replace("黃", "黄");
                                }
                                if (i > 0) {
                                    valueRow4.append(",");
                                }
                                valueRow4.append(value);
                            }
                            csvContent4.append(valueRow4).append("\n");
                            // 第5行: 空行
                            csvContent4.append("\n");

                            // 写入文件
                            String outCsvPath4 = path.replace(".csv", "_" + (rowIdx + 1) + ".csv");
                            writeCsvWithBom(Paths.get(outCsvPath4), csvContent4.toString());
                            // Files.write(Paths.get(outCsvPath4), csvContent4.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                    break;
                case 20:
                    // 获取JSON数据（模板20格式）
                    // 第1行: "Lot No.","值"
                    csvContent.append("\"Lot No.\",\"").append(pageData.getString("Lot No.")).append("\"\n");
                    // 第2行: 空行
                    csvContent.append("\n");

                    // 获取tables数组
                    JSONArray tables20 = pageData.getJSONArray("tables");
                    if (tables20.length() > 0) {
                        JSONArray table20 = tables20.getJSONArray(0);
                        StringBuilder headerRow20 = new StringBuilder();
                        StringBuilder valueRow20 = new StringBuilder();

                        for (int i = 0; i < table20.length(); i++) {
                            JSONObject row = table20.getJSONObject(i);
                            String itemName = row.getString("ITEM");
                            String result20 = row.getString("RESULT");
                            String spec20 = row.optString("SPEC", "");

                            // 如果RESULT为pass，取SPEC的纯数值
                            String outputValue;
                            if ("pass".equalsIgnoreCase(result20.trim())) {
                                outputValue = extractNumericFromSpec(spec20);
                            } else {
                                outputValue = extractNumericOrValue(result20);
                            }

                            if (i > 0) {
                                headerRow20.append(",");
                                valueRow20.append(",");
                            }
                            headerRow20.append("\"").append(itemName).append("\"");
                            valueRow20.append("\"").append(outputValue).append("\"");
                        }
                        // 第3行: 表头
                        csvContent.append(headerRow20).append("\n");
                        // 第4行: 数据值
                        csvContent.append(valueRow20).append("\n");
                    }
                    // 第5行: 空行
                    csvContent.append("\n");

                    // 写入文件
                    writeCsvWithBom(outPath, csvContent.toString());
                    break;
                case 21:
                    // 获取JSON数据（模板21格式）
                    // 所有行导出在一个CSV中，表头: Lot No., Concentration (ppm)-1, Concentration (ppm)-2
                    StringBuilder csvContent21 = new StringBuilder();
                    csvContent21.append("\"Lot No.\",\"Concentration (ppm)-1\",\"Concentration (ppm)-2\"\n");

                    JSONArray tables21 = pageData.getJSONArray("tables");
                    if (tables21.length() > 0) {
                        JSONArray table21 = tables21.getJSONArray(0);
                        for (int i = 0; i < table21.length(); i++) {
                            JSONObject row = table21.getJSONObject(i);
                            String lotNo21 = row.getString("Lot No.");
                            String concentration = row.getString("Concentration (ppm)");

                            // 解析 "200 (203)" -> part1="200", part2="203"
                            String concPart1 = concentration;
                            String concPart2 = "";
                            java.util.regex.Matcher concMatcher = java.util.regex.Pattern.compile("^\\s*([^(]+?)\\s*\\(([^)]+)\\)\\s*$").matcher(concentration);
                            if (concMatcher.find()) {
                                concPart1 = concMatcher.group(1).trim();
                                concPart2 = concMatcher.group(2).trim();
                            }

                            csvContent21.append("\"").append(lotNo21).append("\",\"")
                                    .append(concPart1).append("\",\"")
                                    .append(concPart2).append("\"\n");
                        }
                    }
                    // 空行
                    csvContent21.append("\n");

                    // 写入文件
                    writeCsvWithBom(outPath, csvContent21.toString());
                    break;
                default:
                    json2csv(json, path, new ExtractTemplateV2DTO(extractTemplateDTO));
            }
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    public static void json2csvCompress(Map<String, List<IdpFile>> groupTemplateMap, Path tempDir, File outFile) {
        try {
            // outFile should be a .zip file
            List<File> csvFiles = new ArrayList<>();

            for (Map.Entry<String, List<IdpFile>> groupEntry : groupTemplateMap.entrySet()) {
                List<IdpFile> idpFileList = groupEntry.getValue();
                if (idpFileList.isEmpty()) continue;

                // Get template name from the first file's parameter
                ExtractTemplateV2DTO extractTemplateDTO = JsonUtils.jsonStringToBean(idpFileList.get(0).getParameter(), ExtractTemplateV2DTO.class);
                String templateName = extractTemplateDTO.getName();
                if (!StringUtils.hasText(templateName)) {
                    templateName = groupEntry.getKey();
                }
                // Sanitize template name for file system
                String safeTemplateName = templateName.replaceAll("[\\\\/:*?\"<>|]", "_");

                // Collect all entries for this template group
                List<Map.Entry<IdpFile, LinkedHashMap<String, Object>>> groupEntries = new ArrayList<>();
                for (IdpFile idpFile : idpFileList) {
                    String jsonStr = new String(Files.readAllBytes(Paths.get(idpFile.getOutFilePath())), StandardCharsets.UTF_8);
                    JsonExtractConvert.json2json(jsonStr, idpFile.getOutFilePath(), JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateV2DTO.class));

                    jsonStr = new String(Files.readAllBytes(Paths.get(idpFile.getOutFilePath())));
                    LinkedHashMap<String, Object> jsonData = parseOrderedMap(jsonStr);
                    LinkedHashMap<String, Object> flatJson = flattenPageKeys(jsonData);
                    groupEntries.add(new AbstractMap.SimpleEntry<>(idpFile, flatJson));
                }

                // Collect all keys and table names for this group
                LinkedHashSet<String> allKeys = new LinkedHashSet<>();
                LinkedHashSet<String> allTableNames = new LinkedHashSet<>();

                for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                    LinkedHashMap<String, Object> json = entry.getValue();
                    for (String key : json.keySet()) {
                        if (!"tables".equals(key)) allKeys.add(key);
                    }
                    if (json.containsKey("tables")) {
                        LinkedHashMap<String, List<Object>> tbls = (LinkedHashMap<String, List<Object>>) json.get("tables");
                        allTableNames.addAll(tbls.keySet());
                    }
                }

                // ===== 1. Generate templateName-All.csv (existing logic) =====
                StringBuilder csvContent = new StringBuilder();
                List<String> headerList = new ArrayList<>();
                headerList.add("\"Task ID\"");
                headerList.add("\"File Name\"");
                for (String key : allKeys) headerList.add("\"" + key.replace("\"", "\"\"") + "\"");
                List<String> tableNameList = new ArrayList<>(allTableNames);
                for (String tableName : tableNameList) headerList.add("\"" + tableName.replace("\"", "\"\"") + "\"");
                csvContent.append(String.join(",", headerList)).append("\n");

                for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                    IdpFile idpFile = entry.getKey();
                    LinkedHashMap<String, Object> json = entry.getValue();
                    List<String> rowValues = new ArrayList<>();
                    rowValues.add("\"" + (idpFile.getId() != null ? idpFile.getId().replace("\"", "\"\"") : "") + "\"");
                    rowValues.add("\"" + idpFile.getFileName().replace("\"", "\"\"") + "\"");

                    for (String key : allKeys) {
                        String value = mapOpt(json, key, "");
                        rowValues.add("\"" + value.replace("\"", "\"\"") + "\"");
                    }

                    LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = json.containsKey("tables")
                            ? (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables")
                            : new LinkedHashMap<>();
                    for (String tableName : tableNameList) {
                        List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                        if (table != null && !table.isEmpty()) {
                            StringBuilder tableStr = new StringBuilder();
                            for (int j = 0; j < table.size(); j++) {
                                LinkedHashMap<String, Object> row = table.get(j);
                                if (j > 0) tableStr.append(";");
                                boolean first = true;
                                for (Map.Entry<String, Object> re : row.entrySet()) {
                                    if (!first) tableStr.append(",");
                                    tableStr.append(re.getKey()).append(":").append(re.getValue() == null ? "" : re.getValue().toString());
                                    first = false;
                                }
                            }
                            rowValues.add("\"" + tableStr.toString().replace("\"", "\"\"") + "\"");
                        } else {
                            rowValues.add("\"\"");
                        }
                    }
                    csvContent.append(String.join(",", rowValues)).append("\n");
                }

                File allCsvFile = new File(tempDir.toFile(), safeTemplateName + "-All.csv");
                writeCsvWithBom(allCsvFile.toPath(), csvContent.toString());
                csvFiles.add(allCsvFile);

                // ===== 2. Generate templateName-tableName.csv for each table =====
                for (String tableName : allTableNames) {
                    // Collect all table headers across all files for this table
                    LinkedHashSet<String> tableHeaders = new LinkedHashSet<>();
                    for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                        LinkedHashMap<String, Object> json = entry.getValue();
                        if (json.containsKey("tables")) {
                            LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap =
                                    (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables");
                            List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                            if (table != null && !table.isEmpty()) {
                                tableHeaders.addAll(table.get(0).keySet());
                            }
                        }
                    }
                    if (tableHeaders.isEmpty()) continue;

                    List<String> headersList = new ArrayList<>(tableHeaders);
                    StringBuilder tableCsv = new StringBuilder();
                    // Header row: Task ID, File Name, then table column headers
                    List<String> tableHeaderRow = new ArrayList<>();
                    tableHeaderRow.add("\"Task ID\"");
                    tableHeaderRow.add("\"File Name\"");
                    for (String h : headersList) {
                        tableHeaderRow.add("\"" + h.replace("\"", "\"\"") + "\"");
                    }
                    tableCsv.append(String.join(",", tableHeaderRow)).append("\n");

                    // Data rows
                    for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                        IdpFile idpFile = entry.getKey();
                        LinkedHashMap<String, Object> json = entry.getValue();
                        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = json.containsKey("tables")
                                ? (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables")
                                : new LinkedHashMap<>();
                        List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                        if (table != null && !table.isEmpty()) {
                            for (LinkedHashMap<String, Object> row : table) {
                                List<String> rowValues = new ArrayList<>();
                                rowValues.add("\"" + (idpFile.getId() != null ? idpFile.getId().replace("\"", "\"\"") : "") + "\"");
                                rowValues.add("\"" + idpFile.getFileName().replace("\"", "\"\"") + "\"");
                                for (String h : headersList) {
                                    String val = mapOpt(row, h, "");
                                    rowValues.add("\"" + val.replace("\"", "\"\"") + "\"");
                                }
                                tableCsv.append(String.join(",", rowValues)).append("\n");
                            }
                        }
                    }

                    String safeTableName = tableName.replaceAll("[\\\\/:*?\"<>|]", "_");
                    File tableCsvFile = new File(tempDir.toFile(), safeTemplateName + "-" + safeTableName + ".csv");
                    writeCsvWithBom(tableCsvFile.toPath(), tableCsv.toString());
                    csvFiles.add(tableCsvFile);
                }
            }

            // ===== 3. Package all CSV files into a ZIP =====
            try (FileOutputStream fos = new FileOutputStream(outFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (File csvFile : csvFiles) {
                    zos.putNextEntry(new ZipEntry(csvFile.getName()));
                    Files.copy(csvFile.toPath(), zos);
                    zos.closeEntry();
                }
            }

            // Clean up temporary CSV files
            for (File csvFile : csvFiles) {
                if (!csvFile.delete()) {
                    log.warn("Failed to delete temporary CSV file: {}", csvFile.getAbsolutePath());
                }
            }

        } catch (IOException e) {
            log.error("json2csvCompress error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Flatten page-keyed JSON structure. If top-level values are all Maps (page keys like "page-1"),
     * merge their contents into a single LinkedHashMap. Otherwise return as-is.
     */
    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> flattenPageKeys(LinkedHashMap<String, Object> jsonData) {
        // Check if all top-level values are Maps (page structure)
        boolean allPages = !jsonData.isEmpty();
        for (Object val : jsonData.values()) {
            if (!(val instanceof Map)) {
                allPages = false;
                break;
            }
        }

        if (!allPages) {
            return jsonData;
        }

        LinkedHashMap<String, Object> merged = new LinkedHashMap<>();
        LinkedHashMap<String, List<Object>> mergedTables = new LinkedHashMap<>();

        for (Object pageVal : jsonData.values()) {
            LinkedHashMap<String, Object> pageContent = (LinkedHashMap<String, Object>) pageVal;
            for (Map.Entry<String, Object> ce : pageContent.entrySet()) {
                if ("tables".equals(ce.getKey())) {
                    LinkedHashMap<String, List<Object>> pageTables = (LinkedHashMap<String, List<Object>>) ce.getValue();
                    for (Map.Entry<String, List<Object>> tableEntry : pageTables.entrySet()) {
                        mergedTables.computeIfAbsent(tableEntry.getKey(), k -> new ArrayList<>()).addAll(tableEntry.getValue());
                    }
                } else {
                    merged.put(ce.getKey(), ce.getValue());
                }
            }
        }
        if (!mergedTables.isEmpty()) {
            merged.put("tables", mergedTables);
        }
        return merged;
    }

    @SuppressWarnings("unchecked")
    public static void json2excelCompress(Map<String, List<IdpFile>> groupTemplateMap, Path tempDir, File outFileXlsx) {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(outFileXlsx)) {

            for (Map.Entry<String, List<IdpFile>> groupEntry : groupTemplateMap.entrySet()) {
                List<IdpFile> idpFileList = groupEntry.getValue();
                if (idpFileList.isEmpty()) continue;

                // Get template name from the first file's parameter
                ExtractTemplateV2DTO extractTemplateDTO = JsonUtils.jsonStringToBean(idpFileList.get(0).getParameter(), ExtractTemplateV2DTO.class);
                String templateName = extractTemplateDTO.getName();
                if (!StringUtils.hasText(templateName)) {
                    templateName = groupEntry.getKey();
                }

                // Collect all entries for this template group
                List<Map.Entry<IdpFile, LinkedHashMap<String, Object>>> groupEntries = new ArrayList<>();
                for (IdpFile idpFile : idpFileList) {
                    try {
                        String jsonStr = new String(Files.readAllBytes(Paths.get(idpFile.getOutFilePath())), StandardCharsets.UTF_8);
                        JsonExtractConvert.json2json(jsonStr, idpFile.getOutFilePath(), JsonUtils.jsonStringToBean(idpFile.getParameter(), ExtractTemplateV2DTO.class));
                        jsonStr = new String(Files.readAllBytes(Paths.get(idpFile.getOutFilePath())));
                        LinkedHashMap<String, Object> jsonData = parseOrderedMap(jsonStr);
                        LinkedHashMap<String, Object> flatJson = flattenPageKeys(jsonData);
                        groupEntries.add(new AbstractMap.SimpleEntry<>(idpFile, flatJson));
                    } catch (Exception e) {
                        log.warn("Failed to read JSON for file: {}, path: {}", idpFile.getFileName(), idpFile.getOutFilePath(), e);
                    }
                }

                if (groupEntries.isEmpty()) continue;

                // Collect all keys and table names for this template group
                LinkedHashSet<String> allKeys = new LinkedHashSet<>();
                LinkedHashSet<String> allTableNames = new LinkedHashSet<>();
                for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                    LinkedHashMap<String, Object> json = entry.getValue();
                    for (String key : json.keySet()) {
                        if (!"tables".equals(key)) allKeys.add(key);
                    }
                    if (json.containsKey("tables")) {
                        LinkedHashMap<String, Object> tbls = (LinkedHashMap<String, Object>) json.get("tables");
                        allTableNames.addAll(tbls.keySet());
                    }
                }
                List<String> tableNameList = new ArrayList<>(allTableNames);

                // ========== Summary Sheet: templateName-ALL ==========
                String summarySheetName = safeSheetName(workbook, templateName + "-ALL");
                Sheet summarySheet = workbook.createSheet(summarySheetName);
                int summaryRowNum = 0;
                Row summaryHeaderRow = summarySheet.createRow(summaryRowNum++);
                int colIdx = 0;
                summaryHeaderRow.createCell(colIdx++).setCellValue("Task ID");
                summaryHeaderRow.createCell(colIdx++).setCellValue("File Name");
                for (String key : allKeys) summaryHeaderRow.createCell(colIdx++).setCellValue(key);
                for (String tableName : tableNameList) summaryHeaderRow.createCell(colIdx++).setCellValue(tableName);

                for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                    IdpFile idpFile = entry.getKey();
                    LinkedHashMap<String, Object> json = entry.getValue();
                    Row dataRow = summarySheet.createRow(summaryRowNum++);
                    int dataColIdx = 0;
                    dataRow.createCell(dataColIdx++).setCellValue(idpFile.getId() != null ? idpFile.getId() : "");
                    dataRow.createCell(dataColIdx++).setCellValue(idpFile.getFileName() != null ? idpFile.getFileName() : "");
                    for (String key : allKeys) dataRow.createCell(dataColIdx++).setCellValue(mapOpt(json, key, ""));

                    LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = json.containsKey("tables")
                            ? (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables")
                            : new LinkedHashMap<>();
                    for (String tableName : tableNameList) {
                        List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                        if (table != null && !table.isEmpty()) {
                            StringBuilder tableStr = new StringBuilder();
                            for (int j = 0; j < table.size(); j++) {
                                LinkedHashMap<String, Object> row = table.get(j);
                                if (j > 0) tableStr.append(";");
                                boolean first = true;
                                for (Map.Entry<String, Object> re : row.entrySet()) {
                                    if (!first) tableStr.append(",");
                                    tableStr.append(re.getKey()).append(":").append(re.getValue() == null ? "" : re.getValue().toString());
                                    first = false;
                                }
                            }
                            dataRow.createCell(dataColIdx++).setCellValue(tableStr.toString());
                        } else {
                            dataRow.createCell(dataColIdx++).setCellValue("");
                        }
                    }
                }

                // ========== Table Detail Sheets: templateName-tableName ==========
                for (String tableName : tableNameList) {
                    LinkedHashSet<String> tableHeaders = new LinkedHashSet<>();
                    for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                        LinkedHashMap<String, Object> json = entry.getValue();
                        if (json.containsKey("tables")) {
                            LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables");
                            List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                            if (table != null) {
                                for (LinkedHashMap<String, Object> row : table) tableHeaders.addAll(row.keySet());
                            }
                        }
                    }
                    if (tableHeaders.isEmpty()) continue;

                    String tableSheetName = safeSheetName(workbook, templateName + "-" + tableName);
                    Sheet tableSheet = workbook.createSheet(tableSheetName);
                    int tableRowNum = 0;
                    Row tableHeaderRow = tableSheet.createRow(tableRowNum++);
                    int tColIdx = 0;
                    tableHeaderRow.createCell(tColIdx++).setCellValue("Task ID");
                    tableHeaderRow.createCell(tColIdx++).setCellValue("File Name");
                    List<String> headerList = new ArrayList<>(tableHeaders);
                    for (String header : headerList) tableHeaderRow.createCell(tColIdx++).setCellValue(header);

                    for (Map.Entry<IdpFile, LinkedHashMap<String, Object>> entry : groupEntries) {
                        IdpFile idpFile = entry.getKey();
                        LinkedHashMap<String, Object> json = entry.getValue();
                        if (!json.containsKey("tables")) continue;
                        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> tablesMap = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>) json.get("tables");
                        List<LinkedHashMap<String, Object>> table = tablesMap.get(tableName);
                        if (table == null) continue;
                        for (LinkedHashMap<String, Object> rowData : table) {
                            Row tableDataRow = tableSheet.createRow(tableRowNum++);
                            int tdColIdx = 0;
                            tableDataRow.createCell(tdColIdx++).setCellValue(idpFile.getId() != null ? idpFile.getId() : "");
                            tableDataRow.createCell(tdColIdx++).setCellValue(idpFile.getFileName() != null ? idpFile.getFileName() : "");
                            for (String header : headerList) tableDataRow.createCell(tdColIdx++).setCellValue(mapOpt(rowData, header, ""));
                        }
                    }
                }
            }

            workbook.write(fos);
        } catch (Exception e) {
            log.error("json2excelCompress error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a safe Excel sheet name: remove illegal characters, truncate to 31 chars,
     * and ensure uniqueness within the workbook.
     */
    private static String safeSheetName(Workbook workbook, String name) {
        if (name == null || name.isEmpty()) {
            name = "Sheet";
        }
        // Remove illegal Excel sheet name characters: \ / ? * [ ]
        name = name.replaceAll("[\\\\/?*\\[\\]]", "_");
        // Truncate to 31 characters (Excel limit)
        if (name.length() > 31) {
            name = name.substring(0, 31);
        }
        // Ensure uniqueness
        String baseName = name;
        int suffix = 1;
        while (workbook.getSheet(name) != null) {
            String suffixStr = "_" + suffix;
            int maxBaseLen = 31 - suffixStr.length();
            name = (baseName.length() > maxBaseLen ? baseName.substring(0, maxBaseLen) : baseName) + suffixStr;
            suffix++;
        }
        return name;
    }


    /**
     * 写入带 UTF-8 BOM 的 CSV 文件，确保 Windows Excel 正确识别中文
     */
    private static void writeCsvWithBom(Path path, String content) throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + bytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(bytes, 0, result, bom.length, bytes.length);
        Files.write(path, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
