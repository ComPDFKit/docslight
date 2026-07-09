package com.compdf.utils;

import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.DocumentAnalysisResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonLayoutConverter {


    public static void markdownToTxt(String mdStr, String outputPath) throws IOException {
        if (mdStr == null || mdStr.isEmpty()) {
            return;
        }

        StringBuilder result = new StringBuilder();
        String[] lines = mdStr.split("\n");

        boolean inHtmlTable = false;
        StringBuilder htmlTableBuilder = new StringBuilder();
        boolean inMarkdownTable = false;
        List<String[]> markdownTableRows = new ArrayList<>();

        for (String s : lines) {
            String line = s;
            String trimmedLine = line.trim();

            // 去除图片标签 - markdown格式 ![...](...)
            if (trimmedLine.matches("!\\[.*?]\\(.*?\\)")) {
                continue;
            }
            // 去除行内图片标签
            line = line.replaceAll("!\\[.*?]\\(.*?\\)", "");

            // 检测HTML表格开始
            if (trimmedLine.toLowerCase().contains("<table")) {
                inHtmlTable = true;
                htmlTableBuilder = new StringBuilder();
            }

            if (inHtmlTable) {
                htmlTableBuilder.append(line).append("\n");

                // 检测HTML表格结束
                if (trimmedLine.toLowerCase().contains("</table>")) {
                    inHtmlTable = false;
                    String tableText = convertHtmlTableToTxt(htmlTableBuilder.toString());
                    result.append(tableText).append("\n");
                }
                continue;
            }

            // 检测Markdown表格 (包含 | 的行)
            if (trimmedLine.startsWith("|") && trimmedLine.endsWith("|")) {
                // 跳过分隔行 (如 |---|---|)
                if (trimmedLine.matches("\\|[-:\\s|]+\\|")) {
                    continue;
                }

                if (!inMarkdownTable) {
                    inMarkdownTable = true;
                    markdownTableRows = new ArrayList<>();
                }

                // 解析表格行
                String[] cells = trimmedLine.split("\\|");
                List<String> cellList = new ArrayList<>();
                for (String cell : cells) {
                    String trimmed = cell.trim();
                    if (!trimmed.isEmpty()) {
                        cellList.add(trimmed);
                    }
                }
                if (!cellList.isEmpty()) {
                    markdownTableRows.add(cellList.toArray(new String[0]));
                }
                continue;
            } else if (inMarkdownTable) {
                // Markdown表格结束，转换并输出
                inMarkdownTable = false;
                String tableText = convertMarkdownTableToTxt(markdownTableRows);
                result.append(tableText).append("\n");
                markdownTableRows.clear();
            }

            // 处理普通行，去除markdown格式标记
            line = removeMarkdownFormatting(line);

            if (!line.trim().isEmpty()) {
                result.append(line).append("\n");
            } else if (result.length() > 0 && !result.toString().endsWith("\n\n")) {
                result.append("\n");
            }
        }

        // 处理文件末尾的表格
        if (inMarkdownTable && !markdownTableRows.isEmpty()) {
            String tableText = convertMarkdownTableToTxt(markdownTableRows);
            result.append(tableText).append("\n");
        }

        // 写入文件
        File file = new File(outputPath);
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(result.toString());
        }
    }

    private static String convertHtmlTableToTxt(String htmlTable) {
        List<List<String>> tableData = parseHtmlTable(htmlTable);
        return formatTableAsText(tableData);
    }

    private static String convertMarkdownTableToTxt(List<String[]> rows) {
        List<List<String>> tableData = new ArrayList<>();
        for (String[] row : rows) {
            tableData.add(Arrays.asList(row));
        }
        return formatTableAsText(tableData);
    }

    private static String formatTableAsText(List<List<String>> tableData) {
        if (tableData == null || tableData.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 使用三个空格分隔每列，不对齐
        for (List<String> row : tableData) {
            sb.append(String.join("   ", row));
            sb.append("\n");
        }

        return sb.toString();
    }

    private static String removeMarkdownFormatting(String line) {
        // 去除标题标记 # ## ### 等
        line = line.replaceAll("^#{1,6}\\s*", "");
        // 去除粗体 **text** 或 __text__
        line = line.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        line = line.replaceAll("__(.+?)__", "$1");
        // 去除斜体 *text* 或 _text_
        line = line.replaceAll("\\*(.+?)\\*", "$1");
        line = line.replaceAll("_(.+?)_", "$1");
        // 去除删除线 ~~text~~
        line = line.replaceAll("~~(.+?)~~", "$1");
        // 去除行内代码 `code`
        line = line.replaceAll("`(.+?)`", "$1");
        // 去除链接 [text](url)
        line = line.replaceAll("\\[(.+?)]\\(.+?\\)", "$1");
        // 去除HTML注释
        line = line.replaceAll("<!--.*?-->", "");
        // 去除代码块标记
        line = line.replaceAll("^```.*$", "");
        return line;
    }

    public static void main(String[] args) throws IOException {
        markdownToTxt(new String(Files.readAllBytes(Paths.get("C:\\Users\\00\\Downloads\\中文(1)_comidp_batch_parse (3)\\tmpvnnih8vv_merge.md"))), "C:\\Users\\00\\Downloads\\中文(1)_comidp_batch_parse (3)\\tmpvnnih8vv_merge.txt");
    }

    public static void json2markdown(DocumentAnalysisResult jsonData, String outputPath) throws IOException {
        List<String> markdown = new ArrayList<>();
        Map<Integer, List<DocumentAnalysisResult.Detail>> pages = jsonData.getDetail().stream()
                .collect(Collectors.groupingBy(DocumentAnalysisResult.Detail::getPage_id));

        for (Integer pageId : new TreeSet<>(pages.keySet())) {
            markdown.add("## Page " + pageId);
            List<DocumentAnalysisResult.Detail> paragraphs = pages.get(pageId).stream()
                    .sorted(Comparator.comparingInt(DocumentAnalysisResult.Detail::getParagraph_id))
                    .collect(Collectors.toList());

            for (DocumentAnalysisResult.Detail para : paragraphs) {
                String ptype = para.getType();
                String text = para.getText() != null ? para.getText() : "";
                switch (ptype) {
                    case "title":
                        markdown.add("# " + text+"\n");
                        break;
                    case "paragraph":
                    case "reference":
                    case "category":
                    case "ordered_list":
                    case "unordered_list":
                    case "list":
                        markdown.add(text);
                        break;
                    case "image":
                    case "formula":
                        String imagePath = para.getImage_url() != null ? para.getImage_url() : "";
                        markdown.add("![Image](" + imagePath + ")\n");
                        break;
                    case "standard_table":
                    case "unstandard_table":
                    case "table":
                        markdown.add(text+"\n");
                        break;
                    case "code":
                    case "algorithm":
                        markdown.add("```\n" + text + "\n```");
                        break;
                    case "header":
                    case "footer":
                    case "page_number":
                        markdown.add("<!-- " + text + " -->");
                        break;
                    case "table_title":
                    case "figure_title":
                        markdown.add("**" + text + "**");
                        break;
                    case "table_caption":
                    case "figure_caption":
                        markdown.add("_" + text + "_");
                        break;
                }
            }
        }

        File file = new File(outputPath);
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : markdown) {
                writer.write(line);
                writer.newLine();
            }
        }
    }


    private static List<List<String>> parseHtmlTable(String html) {
        List<List<String>> tableData = new ArrayList<>();
        if (html == null || html.isEmpty()) return tableData;

        try {
            Document doc = Jsoup.parse(html);
            for (Element row : doc.select("tr")) {
                List<String> rowData = new ArrayList<>();
                for (Element cell : row.select("td, th")) {
                    rowData.add(cell.text().trim());
                }
                tableData.add(rowData);
            }
        } catch (Exception e) {
            // Fallback to empty table on parse error
        }
        return tableData;
    }

    /**
     * 解析导出结果文件处理
     * @param outFile 导出结果文件 .zip
     * @param exportFormat exportFormat
     */
    public static void layoutFileHandle(File outFile, String exportFormat) {
        try {
            String outFolder = outFile.getPath().replace(".zip", "");
            ZipUtil.unZip(outFile.getPath(), outFolder);
            File[] files = Paths.get(outFolder).toFile().listFiles();
            switch (exportFormat) {
                case "MD":
                    // 删除多余的json文件和TXT文件
                    if (files != null) {
                        for (File file : files) {
                            String fileName = file.getName().toUpperCase();
                            if (fileName.endsWith(".JSON") || fileName.endsWith(".TXT")) {
                                FileUtils.deleteFile(file.toPath());
                            }
                        }
                    }
                    FileUtils.deleteFile(outFile.toPath());
                    ZipUtil.zipFolder(outFolder, outFile.getPath());
                    break;
                case "TXT":
                    // 删除多余的json文件和md文件
                    if (files != null) {
                        for (File file : files) {
                            String fileName = file.getName().toUpperCase();
                            if (fileName.endsWith(".JSON") || fileName.endsWith(".MD")) {
                                FileUtils.deleteFile(file.toPath());
                            }
                        }
                    }
                    FileUtils.deleteFile(outFile.toPath());
                    ZipUtil.zipFolder(outFolder, outFile.getPath());
                    break;
                case "DOCX":
                    // 调用转档处理
                    break;
                case "PDF":
                    // 调用转档处理
                    break;
                case "JSON":
                default:
                    // 删除多余的md文件和TXT文件
                    if (files != null) {
                        for (File file : files) {
                            String fileName = file.getName().toUpperCase();
                            if (fileName.endsWith(".MD") || fileName.endsWith(".TXT")) {
                                FileUtils.deleteFile(file.toPath());
                            }
                        }
                    }
                    FileUtils.deleteFile(outFile.toPath());
                    ZipUtil.zipFolder(outFolder, outFile.getPath());
                    break;
            }
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }


    static class Point implements Comparable<Point> {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Point other) {
            if (this.y != other.y) {
                return Double.compare(this.y, other.y);
            }
            return Double.compare(this.x, other.x);
        }

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }

    static class Rect {
        public Point leftTop;
        public Point rightBottom;

        public Rect(Point leftTop, Point rightBottom) {
            this.leftTop = leftTop;
            this.rightBottom = rightBottom;
        }

        public boolean containsRect(Rect other) {
            return !(this.leftTop.x <= other.leftTop.x) ||
                    !(this.leftTop.y <= other.leftTop.y) ||
                    !(this.rightBottom.x >= other.rightBottom.x) ||
                    !(this.rightBottom.y >= other.rightBottom.y);
        }
    }


    private static final Map<String, String> TYPE_DICT = new HashMap<String, String>() {{
        put("paragraph", "paragraph");
    }};

    public static final List<String> CATALOG_TYPE_LIST = Arrays.asList("catalogue", "title", "figure_title", "table_title");

    public static void sortPageItemsByPos(DocumentAnalysisResult.Page page) {
        List<DocumentAnalysisResult.Content>  content = page.getContent();
        if (content == null) return;

        List<DocumentAnalysisResult.Content> contentList = new ArrayList<>(content);

        // 根据位置排序
        contentList.sort((a, b) -> {
            List<Double> aPos = a.getPosition();
            List<Double> bPos = b.getPosition();
            if (aPos == null || bPos == null || aPos.size() < 2 || bPos.size() < 2) return 0;
            Point aPoint = new Point(aPos.get(0), aPos.get(1));
            Point bPoint = new Point(bPos.get(0), bPos.get(1));
            return aPoint.compareTo(bPoint);
        });

        // 更新ID并替换内容
        List<DocumentAnalysisResult.Content> newContent = new ArrayList<>();
        for (int i = 0; i < contentList.size(); i++) {
            DocumentAnalysisResult.Content item = contentList.get(i);
            item.setId(i);
            newContent.add(item);
        }
        page.setContent(newContent);
    }

    public static void sortDetailsByPosAndPageId(List<DocumentAnalysisResult.Detail> details) {
        List<DocumentAnalysisResult.Detail> detailsList = new ArrayList<>(details);

        // 根据page_id和位置排序
        detailsList.sort((a, b) -> {
            int pageIdA = a.getPage_id();
            int pageIdB = b.getPage_id();
            if (pageIdA != pageIdB) {
                return Integer.compare(pageIdA, pageIdB);
            }

            List<Double> aPos = a.getPosition();
            List<Double> bPos = b.getPosition();
            if (aPos == null || bPos == null || aPos.size() < 2 || bPos.size() < 2) return 0;
            Point aPoint = new Point(aPos.get(0), aPos.get(1));
            Point bPoint = new Point(bPos.get(0), bPos.get(1));
            return aPoint.compareTo(bPoint);
        });

        // 更新paragraph_id
        List<DocumentAnalysisResult.Detail> newDetails = new ArrayList<>();
        for (int i = 0; i < detailsList.size(); i++) {
            DocumentAnalysisResult.Detail detail = detailsList.get(i);
            detail.setParagraph_id(i);
            newDetails.add(detail);
        }
        details.clear();
        details.addAll(newDetails);
    }

    public static void sortCatalogByPosAndPageId(List<DocumentAnalysisResult.TocItem> tocItems) {
        List<DocumentAnalysisResult.TocItem> tocItemList = new ArrayList<>(tocItems);

        // 根据page_id和位置排序
        tocItemList.sort((a, b) -> {
            int pageIdA = a.getPage_id();
            int pageIdB = b.getPage_id();
            if (pageIdA != pageIdB) {
                return Integer.compare(pageIdA, pageIdB);
            }

            List<Double> aPos = a.getPos();
            List<Double> bPos = b.getPos();
            if (aPos == null || bPos == null || aPos.size() < 2 || bPos.size() < 2) return 0;
            Point aPoint = new Point(aPos.get(0), aPos.get(1));
            Point bPoint = new Point(bPos.get(0), bPos.get(1));
            return aPoint.compareTo(bPoint);
        });

        // 更新paragraph_id
        List<DocumentAnalysisResult.TocItem> newDetails = new ArrayList<>();
        for (int i = 0; i < tocItemList.size(); i++) {
            DocumentAnalysisResult.TocItem detail = tocItemList.get(i);
            detail.setParagraph_id(i);
            newDetails.add(detail);
        }
        tocItemList.clear();
        tocItemList.addAll(newDetails);
    }

    public static void insertItemByPos(DocumentAnalysisResult data, int pageId, double[] pos, String text, String itemType) {
        List<DocumentAnalysisResult.Page> pages = data.getPages();
        if (pageId < 1 || pageId > pages.size()) {
            throw new IllegalArgumentException("Invalid page ID: " + pageId);
        }

        DocumentAnalysisResult.Page page = pages.get(pageId - 1);
        List<DocumentAnalysisResult.Content> content = page.getContent();

        // 创建新内容项
        DocumentAnalysisResult.Content newItem = new DocumentAnalysisResult.Content();
        newItem.setId(content.size());
        newItem.setPosition(Arrays.stream(pos).boxed().collect(Collectors.toList()));
        newItem.setText(text);
        newItem.setType(TYPE_DICT.getOrDefault(itemType, itemType));
        content.add(newItem);

        sortPageItemsByPos(page);

        // 添加详细信息
        List<DocumentAnalysisResult.Detail> details = data.getDetail();
        DocumentAnalysisResult.Detail newDetail = new DocumentAnalysisResult.Detail();
        newDetail.setPage_id(pageId);
        newDetail.setParagraph_id(content.size() - 1);
        newDetail.setPosition(Arrays.stream(pos).boxed().collect(Collectors.toList()));
        newDetail.setText(text);
        newDetail.setType(itemType);
        details.add(newDetail);

        sortDetailsByPosAndPageId(details);

        if (Objects.equals(itemType, "title") || Objects.equals(itemType, "catalogue")
                || Objects.equals(itemType, "figure_title") || Objects.equals(itemType, "table_title")) {
            List<DocumentAnalysisResult.TocItem> tocItems = data.getCatalog().getToc();
            DocumentAnalysisResult.TocItem newTocItem =  new DocumentAnalysisResult.TocItem();
            newTocItem.setPage_id(pageId);
            newTocItem.setParagraph_id(content.size() - 1);
            newTocItem.setPos(Arrays.stream(pos).boxed().collect(Collectors.toList()));
            newTocItem.setType(itemType);
            newTocItem.setContent(text);
            tocItems.add(newTocItem);

            sortCatalogByPosAndPageId(tocItems);
        }

        System.out.printf("Inserted new item at page %d, pos: %s, text: '%s', type: '%s'%n",
                pageId, Arrays.toString(pos), text, itemType);
    }

}

