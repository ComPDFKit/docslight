package com.compdf.pojo;

/**
 * @author ComPDFKit-WPH 2025/8/8 星期五
 */
import java.util.List;

public class DocumentAnalysisResult {
    private int success_count;
    private int total_count;
    private String version;
    private List<Page> pages;
    private Catalog catalog;
    private List<Metric> metrics;
    private List<Detail> detail;

    // Getters and setters
    public int getSuccess_count() { return success_count; }
    public void setSuccess_count(int success_count) { this.success_count = success_count; }

    public int getTotal_count() { return total_count; }
    public void setTotal_count(int total_count) { this.total_count = total_count; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public List<Page> getPages() { return pages; }
    public void setPages(List<Page> pages) { this.pages = pages; }

    public Catalog getCatalog() { return catalog; }
    public void setCatalog(Catalog catalog) { this.catalog = catalog; }

    public List<Metric> getMetrics() { return metrics; }
    public void setMetrics(List<Metric> metrics) { this.metrics = metrics; }

    public List<Detail> getDetail() { return detail; }
    public void setDetail(List<Detail> detail) { this.detail = detail; }

    public static class Page {
        private int angle;
        private int page_id;
        private String image_id;
        private int width;
        private int height;
        private List<Content> content;

        // Getters and setters
        public int getAngle() { return angle; }
        public void setAngle(int angle) { this.angle = angle; }

        public int getPage_id() { return page_id; }
        public void setPage_id(int page_id) { this.page_id = page_id; }

        public String getImage_id() { return image_id; }
        public void setImage_id(String image_id) { this.image_id = image_id; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public List<Content> getContent() { return content; }
        public void setContent(List<Content> content) { this.content = content; }
    }

    public static class Content {
        private int id;
        private double score;
        private String text;
        private String type;
        private List<Double> position;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<Double> getPosition() { return position; }
        public void setPosition(List<Double> position) { this.position = position; }
    }

    public static class Catalog {
        private List<TocItem> toc;

        // Getters and setters
        public List<TocItem> getToc() { return toc; }
        public void setToc(List<TocItem> toc) { this.toc = toc; }
    }

    public static class TocItem {
        private List<Double> pos;
        private int paragraph_id;
        private int page_id;
        private String content;
        private String type;

        // Getters and setters
        public List<Double> getPos() { return pos; }
        public void setPos(List<Double> pos) { this.pos = pos; }

        public int getParagraph_id() { return paragraph_id; }
        public void setParagraph_id(int paragraph_id) { this.paragraph_id = paragraph_id; }

        public int getPage_id() { return page_id; }
        public void setPage_id(int page_id) { this.page_id = page_id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Metric {
        private int page_image_width;
        private int page_image_height;
        private int page_id;
        private int angle;
        private String image_id;

        // Getters and setters
        public int getPage_image_width() { return page_image_width; }
        public void setPage_image_width(int page_image_width) { this.page_image_width = page_image_width; }

        public int getPage_image_height() { return page_image_height; }
        public void setPage_image_height(int page_image_height) { this.page_image_height = page_image_height; }

        public int getPage_id() { return page_id; }
        public void setPage_id(int page_id) { this.page_id = page_id; }

        public int getAngle() { return angle; }
        public void setAngle(int angle) { this.angle = angle; }

        public String getImage_id() { return image_id; }
        public void setImage_id(String image_id) { this.image_id = image_id; }
    }

    public static class Detail {
        private int page_id;
        private int paragraph_id;
        private String text;
        private String type;
        private String image_url;
        private List<Double> position;
        private List<String> tags;
        private List<Cell> cells;

        // Getters and setters
        public int getPage_id() { return page_id; }
        public void setPage_id(int page_id) { this.page_id = page_id; }

        public int getParagraph_id() { return paragraph_id; }
        public void setParagraph_id(int paragraph_id) { this.paragraph_id = paragraph_id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getImage_url() { return image_url; }
        public void setImage_url(String image_url) { this.image_url = image_url; }

        public List<Double> getPosition() { return position; }
        public void setPosition(List<Double> position) { this.position = position; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public List<Cell> getCells() { return cells; }
        public void setCells(List<Cell> cells) { this.cells = cells; }
    }

    public static class Cell {
        private int row_span;
        private String text;
        private String type;
        private int col;
        private int col_span;
        private List<Double> position;
        private int row;

        // Getters and setters
        public int getRow_span() { return row_span; }
        public void setRow_span(int row_span) { this.row_span = row_span; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public int getCol() { return col; }
        public void setCol(int col) { this.col = col; }

        public int getCol_span() { return col_span; }
        public void setCol_span(int col_span) { this.col_span = col_span; }

        public List<Double> getPosition() { return position; }
        public void setPosition(List<Double> position) { this.position = position; }

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }
    }
}