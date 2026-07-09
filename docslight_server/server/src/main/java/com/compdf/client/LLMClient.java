package com.compdf.client;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.support.json.JSONUtils;
import com.compdf.config.ActuatorServiceManage;
import com.compdf.entity.DataExtractPojo;
import com.compdf.entity.DocSlightSettings;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.DataExtractDTO;
import com.compdf.pojo.ExtractTemplateDTO;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.DocSlightSettingsService;
import com.compdf.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author ComPDFKit-WPH 2025/2/20 0020
 * <p>
 * 大语言模型调用客户端
 */
@Component
@Slf4j
public class LLMClient {
    private final StringRedisTemplate redisTemplate;
    private static final RestTemplate restTemplate = new RestTemplate();
    private final ComPDFKitProperties properties;
    private final WebClient webClient;
    private final DocSlightSettingsService docSlightSettingsService;


    private static String QWEN_API_MODEL;
    private static final String HTTP_PREFIX = "http://";
    public String getActuatorServiceUrl(String taskId) {
        // 从ActuatorServiceManage类获取API执行器服务的URL
        String url;
        if (StringUtils.isEmpty(taskId)) {
            url = ActuatorServiceManage.getApiActuatorServiceUrl();
        }else {
            url = ActuatorServiceManage.getTaskActuatorServiceUrl(taskId);
        }
        // 在获取的URL前添加HTTP前缀，构造完整的URL并返回
        return HTTP_PREFIX + url;
    }
    public LLMClient(StringRedisTemplate redisTemplate, ComPDFKitProperties properties, DocSlightSettingsService docSlightSettingsService) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        webClient = WebClient.builder()
                .baseUrl("https://dashscope.aliyuncs.com")
                .defaultHeader("Authorization", "Bearer " + properties.getQwenAPIKey())
                .build();
        QWEN_API_MODEL = properties.getQwenAPIModel();
        this.docSlightSettingsService = docSlightSettingsService;
    }

    /**
     * 关键信息提取，非流式，图片视觉模型 </p>
     * 当keys和tableHandles 都为 null 时，抽取文档中所有关键信息
     *
     * @return 数据提取结果
     */
    public DataExtractDTO dataExtractOfVisualModel(DataExtractPojo dataExtractPojo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(dataExtractPojo.getFile()));
        DocSlightSettings settings = docSlightSettingsService.getSettings();
        formData.add("api_key", settings.getApikey());
        formData.add("mode", settings.getModel().toLowerCase());
        formData.add("base_url", "https://api-server.compdf.com");
        formData.add("cloud_extract_mode", "vlm");
        formData.add("local_llm_provider", settings.getLocalLlmProvider());
        formData.add("local_llm_model", settings.getLocalLlmModel());
        formData.add("local_llm_base_url", settings.getLocalLlmBaseUrl());
        formData.add("local_llm_api_key", settings.getLocalLlmApiKey());
        formData.add("fields", JsonUtils.getJsonString(dataExtractPojo.getExtractTemplateV2DTO()));
        String url = properties.getDocSlightHost()+"/api/extract";
        DataExtractDTO dataExtractDTO = new DataExtractDTO();
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    /*getActuatorServiceUrl(null) */
                    /*"http://192.168.20.11:8888" */
                           url,
                    new HttpEntity<>(formData, headers),
                    String.class
            );
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
            }
            JSONObject jsonObject = JsonUtils.jsonStringToBean(resp.getBody(), JSONObject.class);
            if (Objects.equals(jsonObject.getBool("success"), true)) {
                String result = jsonObject.getStr("results");
//                JSONObject keyResult = new JSONObject();
//                JSONObject tableResult = new JSONObject();
//                JSONObject newResult = new JSONObject();
//                Set<String> strings = result.keySet();
//                strings.forEach(s -> {
//                    if ("tableHeaders".equals(s)) {
//                        tableResult.set(s, result.getJSONObject(s));
//                    }else {
//                        keyResult.set(s, result.getJSONObject(s).getStr("value"));
//                    }
//                });
//                newResult.set("tableHeaders", tableResult);
//                newResult.set("keys", keyResult);
//                dataExtractDTO.setDetails(JsonUtils.getJsonString(newResult));
                dataExtractDTO.setDetails(result);
                return dataExtractDTO;
            } else {
                log.error("data extract error:{}", resp.getBody());
                throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
            }
        } catch (Exception e) {
            log.error("data extract error:{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
        }
    }

    /**
     * 文档分类
     *
     * @param classJson 分类模板JSON
     * @param rustFsId  文件ID
     * @return 模板名称，返回null代表未分类成功
     */
    public String extractClassify(String classJson, String rustFsId, String diyName, boolean isV2) {
        // 参数校验
        if (!StringUtils.hasText(rustFsId) || !StringUtils.hasText(classJson)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARAM_VALIDATE_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("rustfs_path", rustFsId);
        formData.add("class_json", classJson);
        if (StringUtils.hasText(diyName)) {
            formData.add("diy", diyName);
        }else {
            formData.add("diy", "");
        }
        String responseData;
        try {
            String url;
            if (isV2){
                url = "/extract/classify_v2";
            }else {
                url = "/extract/classify";
            }
            responseData = restTemplate.postForObject(
                    getActuatorServiceUrl(null)
                    /*"http://192.168.20.11:8888" */
                            + url,
                    new HttpEntity<>(formData, headers),
                    String.class
            );
            JSONObject jsonObject = JsonUtils.jsonStringToBean(responseData, JSONObject.class);
            if (Objects.equals(jsonObject.getStr("message"), "OK")) {
                String result = jsonObject.getStr("results");
                if (Objects.equals(jsonObject.getStr("results"), "Other")) {
                    return null;
                }
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }
    }

    /**
     * 关键信息提取，非流式，图片视觉模型在线API</p>
     * 当keys和tableHandles 都为 null 时，抽取文档中所有关键信息
     *
     * @param imgFiles     图片
     * @return 数据提取结果
     * @throws Exception Exception
     */
    public DataExtractDTO dataExtractOfVisualModelAPI(List<File> imgFiles, ExtractTemplateDTO extractTemplate, List<Integer> pages) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ".concat(properties.getQwenAPIKey()));
        DataExtractDTO extractDTO = new DataExtractDTO();
        Map<String, Object> details = new LinkedHashMap<>();

        List<String> keys;
        List<String> tableHandles;
        try {
            keys = extractTemplate.toList(extractTemplate.getKeys());
        } catch (Exception e) {
            keys = new ArrayList<>();
        }
        try{
            tableHandles = extractTemplate.toList(extractTemplate.getTableHeaders());
        }catch (Exception e){
            tableHandles = new ArrayList<>();
        }

        for (int i = 0; i < imgFiles.size(); i++) {
            Map<String, Object> payload = qwenBodyHandle(Collections.singletonList(imgFiles.get(i)), keys, tableHandles);
            HttpEntity<String> entity = new HttpEntity<>(JSONUtils.toJSONString(payload), headers);
            ResponseEntity<QwenResult> response = restTemplate.postForEntity(
                    "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                    entity,
                    QwenResult.class
            );
            log.info("llm:{}", response.getBody());
            String qaResult = Objects.requireNonNull(response.getBody()).getChoices().get(0).getMessage().getContent();
            LinkedHashMap map = JsonUtils.jsonStringToBean(qaResult, LinkedHashMap.class);
            if (!CollectionUtils.isEmpty(keys) && CollectionUtils.isEmpty(tableHandles)) {
                map.put("tables", null);
            }
            if (CollectionUtils.isEmpty(keys) && !CollectionUtils.isEmpty(tableHandles)) {
                LinkedHashMap tablesMap = new LinkedHashMap();
                tablesMap.put("tables", map.get("tables"));
                map = tablesMap;
            }
            if (map.get("tables") != null){
                String tables = JsonUtils.getJsonString(map.get("tables"));
                try {
                    // 尝试将字符串解析为JSONArray
                    JSONArray jsonArray = new JSONArray(tables);

                    // 检查第一个元素是否是JSONArray
                    if (jsonArray.get(0) instanceof JSONArray) {
                        // 如果是二维数组，则无需转换
                    } else {
                        // 如果不是二维数组，则将其转换为二维数组
                        JSONArray outerArray = new JSONArray();
                        outerArray.put(jsonArray);
                        map.put("tables", outerArray);
                    }
                } catch (JSONException e) {
                    // 如果解析出错，打印错误信息
                    log.error("tables analysis Failed");
                    map.put("tables", null);
                }
            }
//            Map<String, Object> newMap = new LinkedHashMap<>();
//            for (Object key : map.keySet()) {
//                if(key.toString().equals("tables")){
//                    String tableStr = JsonUtils.getJsonString(map.get(key));
//                    String newTableStr = tableStr.replaceAll("(\"[^\"(]*)\\([^)]*\\)", "$1");
//                    newMap.put(key.toString(), JsonUtils.jsonStringToList(newTableStr, List.class));
//                }else {
//                    newMap.put(key.toString().substring(0, key.toString().lastIndexOf("(")),
//                            (map.get(key) == "" || map.get(key) == null) ? null : map.get(key));
//                }
//            }

//            extractDTO.setDetails(map);
            if (CollectionUtils.isEmpty(pages)) {
                details.put("Page-" + (i+1), map);
            } else {
                details.put("Page-" + pages.get(i), map);
            }
        }
        extractDTO.setDetails(JsonUtils.getJsonString(details));
        return extractDTO;
    }

    /**
     * 抽取结果处理
     * @param map resultMap
     * @param keys keys
     * @param tableHandles tableHandles
     * @return 处理后结果
     */
    private static Map<String, Object> extractionResultProcessing(Map<String, Object> map, List<String> keys, List<String> tableHandles) {
        Map<String, Object> newResult = new LinkedHashMap<>();

        if (map == null){
            throw new ComPDFKitException(ErrorInfoEnum.FILE_INFORMATION_EXTRACTION_FAILED);
        }
        if (!CollectionUtils.isEmpty(keys) && CollectionUtils.isEmpty(tableHandles)) {
            newResult.put("tables", null);
        }
        if (CollectionUtils.isEmpty(keys) && !CollectionUtils.isEmpty(tableHandles)) {
            Map<String, Object> tablesMap = new LinkedHashMap<>();
            tablesMap.put("tables", map.get("tables"));
            newResult = tablesMap;
        }
        if (!CollectionUtils.isEmpty(keys)){
            for (String key : keys) {
                newResult.put(key, map.get(key));
            }
        }
//        if (map.get("tables") != null){
//            String tables = JsonUtils.getJsonString(map.get("tables"));
//            try {
//                // 尝试将字符串解析为JSONArray
//                JSONArray jsonArray = new JSONArray(tables);
//
//                // 检查第一个元素是否是JSONArray
//                if (jsonArray.get(0) instanceof JSONArray) {
//                    // 如果是二维数组，则无需转换
//                    newResult.put("tables", jsonArray);
//                } else {
//                    // 如果不是二维数组，则将其转换为二维数组
//                    JSONArray outerArray = new JSONArray();
//                    outerArray.put(jsonArray);
//                    newResult.put("tables", outerArray);
//                }
//            } catch (JSONException e) {
//                // 如果解析出错，打印错误信息
//                log.error("tables analysis Failed");
//                newResult.put("tables", null);
//            }
//        }
        List<List<Map<String, Object>>> finalTables = new ArrayList<>();
        List<Map<String, Object>> tableList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tableHandles)){
            Object tables = map.get("tables");
            if (!Objects.isNull(tables) && !(CollectionUtils.isEmpty((List<List<Map<String, Object>>>)tables))){
                List<List<Map<String, Object>>> jsonArray = (List<List<Map<String, Object>>>)tables;
                List<Map<String, Object>> array = jsonArray.get(0);
                for (Map<String, Object> stringObjectMap : array) {
                    Map<String, Object> tableMap = new LinkedHashMap<>();
                    for (String tableKey : tableHandles) {
                        Object value = stringObjectMap.get(tableKey);
                        tableMap.put(tableKey, value == "" ? null : value);
                    }
                    tableList.add(tableMap);
                }
                finalTables.add(tableList);
                newResult.put("tables", finalTables);

            }else {
                Map<String, Object> tableMap = new LinkedHashMap<>();
                for (String tableKey : tableHandles) {
                    tableMap.put(tableKey, null);
                }
                tableList.add(tableMap);
                finalTables.add(tableList);
                newResult.put("tables", finalTables);
            }
        }
        if (CollectionUtils.isEmpty(keys) && CollectionUtils.isEmpty(tableHandles)){
            return map;
        }
        return newResult;
    }


    /**
     * 图片转成base64
     *
     * @param imageFilePath 图片路径
     * @return base64编码
     * @throws IOException IOException
     */
    private static String encodeImageToBase64(String imageFilePath) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(imageFilePath)));
    }

    /**
     * 关键信息提取，流式数据，图片视觉模型在线API</p>
     * 当keys和tableHandles 都为 null 时，抽取文档中所有关键信息
     *
     * @param imgFiles     图片
     * @param keys         关键字
     * @param tableHandles 表格表头
     * @return 数据提取结果
     * @throws IOException Exception
     */
    public SseEmitter dataExtractOfVisualModelAPIStream(List<File> imgFiles,
                                                        List<String> keys,
                                                        List<String> tableHandles,
                                                        StreamCompletionCallback callback) throws IOException {
        SseEmitter emitter = new SseEmitter();
        // Prepare headers
        StringBuffer result = new StringBuffer();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + properties.getQwenAPIKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = qwenBodyHandle(imgFiles, keys, tableHandles);
        payload.put("stream", true);
        payload.put("stream_options", Collections.singletonMap("include_usage", "json_object"));
        // Send the request and stream the response
        Flux<String> responseFlux = webClient.post()
                .uri("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                .headers(h -> h.addAll(headers))
                .bodyValue(JSONUtils.toJSONString(payload))
                .retrieve()
                .bodyToFlux(String.class);

        responseFlux.subscribe(
                data -> {
                    try {
                        try {
                            QwenResult qwenResult = new ObjectMapper().readValue(data, QwenResult.class);
                            result.append(qwenResult.getChoices().get(0).getDelta().getContent());
                            emitter.send(SseEmitter.event().data(qwenResult.getChoices().get(0).getDelta().getContent()));
                        } catch (JsonProcessingException e) {
                            if ("[DONE]".equals(data)){
                                emitter.send(SseEmitter.event().data(data));
                            } else {
                                log.error(data, e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    // 流正常结束时调用回调函数
                    callback.onComplete(emitter, result.toString());
                }
        );
        return emitter;
    }

    /**
     * qwenAPI body 处理
     *
     * @param keys         关键字
     * @param tableHandles 表格表头
     * @return 数据提取结果
     * @throws IOException IOException
     */
    private Map<String, Object> qwenBodyHandle(List<File> imgFiles, List<String> keys, List<String> tableHandles) throws IOException {
        List<Map<String, Object>> userContentObj = new ArrayList<>();

        for (File imgFile : imgFiles) {
            Map<String, Object> jsonObject = new LinkedHashMap<>();
            jsonObject.put("type", "image_url");
            jsonObject.put("image_url", Collections.singletonMap("url", "data:image/png;base64," + encodeImageToBase64(imgFile.getPath())));
            Map<String, Object> textObject = new LinkedHashMap<>();
            textObject.put("type", "text");
            if (CollectionUtils.isEmpty(keys) && CollectionUtils.isEmpty(tableHandles)) {
                log.info("keys and table is null");
                textObject.put("text", "You need to extract all key information fields (key:value, String type) and all table data (tables:List<List<Map<String, String>>>) from this image. Please output the correct JSON string. Only output data for the identified key information and table_headers, and do not include any other irrelevant content. Return the data in the natural order as they appear in the document.");
            } else {
                log.info("keys and table is not null,{},{}", keys, tableHandles);
                textObject.put("text", "You need to extract the key information fields (key:value, String type) and table data (tables:List<List<Map<String, String>>>) from this image. The keys are: " + keys.toString() + ", and the table_headers are: [" + tableHandles.toString() + "]. Please output the correct JSON string. Only output data for the specified key and table_header, and do not include any other irrelevant content. Return the data in the order specified in the documentation. ");
            }
            userContentObj.add(jsonObject);
            userContentObj.add(textObject);
        }

        Map<String, Object> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userContentObj);

        Map<String, Object> sysMessageMap = new HashMap<>();
        sysMessageMap.put("role", "system");
        String content;
//        if (CollectionUtils.isEmpty(keys) && CollectionUtils.isEmpty(tableHandles)) {
        content = "EXTRACT_SYSTEM_PROMPT = \"\"\"---Goal---\n" +
                "Please output JSON strings, avoid other irrelevant content, and return them in the order of the top and bottom of the document.\n" +
                "- Extract content I need in the image.\n" +
                "- The output must be a strictly valid JSON object.\n" +
                "- Use the same language as used in the image.\n" +
                "\n" +
                "---Rules----\n" +
                "1.Key-Value Extraction\n" +
                "- The keys in the output MUST match exactly with the provided keys (case-sensitive, punctuation-sensitive).\n" +
                "- Extract key-value pairs based on layout (e.g., label-value structure, colon-separated fields).\n" +
                "- If a label exists without a value, assign an empty string as its value.\n" +
                "- Each piece of information must appear only once:\n" +
                "    · Do not repeat the same content in both key-values and tables.\n" +
                "    · If an item appears as a key-value, do not extract it again in a table.\n" +
                "2.Table Extraction\n" +
                "- Automatically recognize the content of images corresponding to given form table header.\n" +
                "- Extract each table into a list of row objects, grouped inside \"tables\": [[...]].\n" +
                "- Maintain column headers and values exactly as shown in the image.\n" +
                "- If multiple tables need to be extracted, return each table as a separate arrays within \"tables\".\n" +
                "3. JSON Format Requirements\n" +
                "- Return only the raw JSON.\n" +
                "- Escape all double quotes inside strings using \\\" (for example, string: \"OK\" becomes \\\"OK\\\").\n" +
                "- Do not include explanations, markdown fences (like ```json), or comments.\n" +
                "\n" +
                "---Output Format Examples---\n" +
                "- Key-Value Example:\n" +
                "{\n" +
                "\"Name\": \"John\",\n" +
                "\"Age\": \"18\"\n" +
                "}\n" +
                "- Table Example:\n" +
                "| Subject   | Score       |\n" +
                "|-----------|-------------|\n" +
                "| Math      | 95          |\n" +
                "| English   |             |\n" +
                "| Chinese   | 90          |\n" +
                "->\n" +
                "\"tables\": [[\n" +
                "{\n" +
                "    \"Subject\": \"Math\",\n" +
                "    \"Score\": \"95\"\n" +
                "},\n" +
                "{\n" +
                "    \"Subject\": \"English\",\n" +
                "    \"Score\": \"\"\n" +
                "},\n" +
                "{\n" +
                "    \"Subject\": \"Chinese\",\n" +
                "    \"Score\": \"90\"\n" +
                "}\n" +
                "]]\n" +
                "- Full Output Example:\n" +
                "{\n" +
                "    \"Name\": \"John\",\n" +
                "    \"Age\": \"18\",\n" +
                "    \"tables\": [[\n" +
                "    {\n" +
                "        \"Subject\": \"Math\",\n" +
                "        \"Score\": \"95\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Subject\": \"English\",\n" +
                "        \"Score\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Subject\": \"Chinese\",\n" +
                "        \"Score\": \"90\"\n" +
                "    }\n" +
                "    ]]\n" +
                "}\n" +
                "\"\"\"";
//        } else {
//            content = "你需要提取出这些图片中的关键信息字段 key:value(String 类型) 和 表格数据 (tables:List<List<Map<String, String>>>)，其中关键信息字段为：" + keys.toString() + ", 表格表头为：" + tableHandles.toString() + ",请输出JSON 字符串，不要输出其它无关内容，按照文档上下顺序返回。\n" +
//                    "下面示例仅作为JSON结构参考，关键信息字段和表格数据请按照实际图片处理。示例：\n" +
//                    "Q：图片中需要提取的关键信息字段有Invoice Number、Invoice Date、Zip Code、Company Name、Tax、Total Excluding Tax、Total。需要提取的表格的表头为Num、Products、Payment Cycle、Gear Level、Price。\n" +
//                    "A：{\n" +
//                    "            \"Invoice Number\": \"202401291234567890\",\n" +
//                    "            \"Invoice Date\": \"01/29/2024\",\n" +
//                    "            \"Zip Code\": \"92868\",\n" +
//                    "            \"Company Name\": \"PDF Technologies, inc.\",\n" +
//                    "            \"Tax\": \"5%\",\n" +
//                    "            \"Total Excluding Tax\": \"$ 20.40\",\n" +
//                    "            \"Total\": \"$ 428.40\",\n" +
//                    "            \"tables\": [[\n" +
//                    "                {\n" +
//                    "                    \"Num\": \"1\",\n" +
//                    "                    \"Products\": \"ComPDFKit API\",\n" +
//                    "                    \"Payment Cycle\": \"Package Payments\",\n" +
//                    "                    \"Gear Level\": \"5000\",\n" +
//                    "                    \"Price\": \"$ 408.00\"\n" +
//                    "                },\n" +
//                    "                {" +
//                    "                    \"Num\": \"2\",\n" +
//                    "                    \"Products\": \"ComPDFKit API\",\n" +
//                    "                    \"Payment Cycle\": \"Package Payments\",\n" +
//                    "                    \"Gear Level\": \"1000\",\n" +
//                    "                    \"Price\": \"$ 99.00\"\n" +
//                    "                 }\n" +
//                    "            ]]\n" +
//                    "        }";
//            ;
//        }

        sysMessageMap.put("content", content);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(sysMessageMap);
        messages.add(userMessageMap);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", QWEN_API_MODEL);
        payload.put("messages", messages);
        payload.put("response_format", Collections.singletonMap("type", "json_object"));
        return payload;
    }

    public DataExtractDTO dataExtractOfVisualModelFileStream(DataExtractPojo dataExtractPojo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(dataExtractPojo.getFile()));
        formData.add("extract_fields", JsonUtils.getJsonString(dataExtractPojo.getExtractTemplateDTO()));
        DataExtractDTO dataExtractDTO = new DataExtractDTO();
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    getActuatorServiceUrl(null)
                            /*"http://192.168.20.11:8888" */
                            + "/extract/run",
                    new HttpEntity<>(formData, headers),
                    String.class
            );
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
            }
            JSONObject jsonObject = JsonUtils.jsonStringToBean(resp.getBody(), JSONObject.class);
            if (Objects.equals(jsonObject.getStr("message"), "OK")) {
                String result = jsonObject.getStr("results");
                dataExtractDTO.setDetails(result);
                return dataExtractDTO;
            } else {
                log.error("data extract error:{}", resp.getBody());
                throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
            }
        } catch (Exception e) {
            log.error("data extract error:{}", e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.EXTRACT_ERROR);
        }
    }


    public  static class Message {
        private String content;

        private String role;

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return this.content;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getRole() {
            return this.role;
        }
    }


    public  static class Choices {
        private Message message;
        private Delta delta;

        public Delta getDelta() {
            return delta;
        }

        public void setDelta(Delta delta) {
            this.delta = delta;
        }

        private String finish_reason;

        private int index;

        private String logprobs;

        public void setMessage(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return this.message;
        }

        public void setFinish_reason(String finish_reason) {
            this.finish_reason = finish_reason;
        }

        public String getFinish_reason() {
            return this.finish_reason;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public void setLogprobs(String logprobs) {
            this.logprobs = logprobs;
        }

        public String getLogprobs() {
            return this.logprobs;
        }
    }

    public static class Delta{

        private String content;
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class Usage {
        private int prompt_tokens;

        private int completion_tokens;

        private int total_tokens;

        public void setPrompt_tokens(int prompt_tokens) {
            this.prompt_tokens = prompt_tokens;
        }

        public int getPrompt_tokens() {
            return this.prompt_tokens;
        }

        public void setCompletion_tokens(int completion_tokens) {
            this.completion_tokens = completion_tokens;
        }

        public int getCompletion_tokens() {
            return this.completion_tokens;
        }

        public void setTotal_tokens(int total_tokens) {
            this.total_tokens = total_tokens;
        }

        public int getTotal_tokens() {
            return this.total_tokens;
        }
    }
    @ToString
    public static class QwenResult {
        private List<Choices> choices;

        private String object;

        private Usage usage;

        private int created;

        private String system_fingerprint;
//
        private String model;

        private String id;

        public void setChoices(List<Choices> choices) {
            this.choices = choices;
        }

        public List<Choices> getChoices() {
            return this.choices;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public String getObject() {
            return this.object;
        }

        public void setUsage(Usage usage) {
            this.usage = usage;
        }

        public Usage getUsage() {
            return this.usage;
        }

        public void setCreated(int created) {
            this.created = created;
        }

        public int getCreated() {
            return this.created;
        }

        public void setSystem_fingerprint(String system_fingerprint) {
            this.system_fingerprint = system_fingerprint;
        }

        public String getSystem_fingerprint() {
            return this.system_fingerprint;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getModel() {
            return this.model;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}
