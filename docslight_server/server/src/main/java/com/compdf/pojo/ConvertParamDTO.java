package com.compdf.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author ComPDFKit-WPH 2025/3/4 0004
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertParamDTO {

    private String type;

    private String password;

    @JsonProperty("enable_ai_layout")
    private boolean enableAiLayout;

    @JsonProperty("contain_image")
    private boolean containImage;

    @JsonProperty("json_contain_table")
    private boolean jsonContainTable;

    @JsonProperty("contain_annotation")
    private boolean containAnnotation;

    @JsonProperty("excel_all_content")
    private boolean excelAllContent;

    @JsonProperty("excel_single_table_page")
    private boolean excelSingleTablePage;

    @JsonProperty("excel_csv_format")
    private boolean excelCsvFormat;

    @JsonProperty("enable_ocr")
    private boolean enableOcr;

    @JsonProperty("compact_text_mode")
    private boolean compactTextMode;

    @JsonProperty("txt_table_format")
    private boolean txtTableFormat;

    @JsonProperty("image_path_enhance")
    private boolean imagePathEnhance;

    @JsonProperty("image_scaling")
    private String imageScaling;

    @JsonProperty("page_layout_mode")
    private String pageLayoutMode;

    @JsonProperty("image_color_mode")
    private String imageColorMode;

    @JsonProperty("image_format")
    private String imageFormat;

    @JsonProperty("page_ranges")
    private String pageRanges;

    @JsonProperty("language")
    private String ocrLanguage;
    @JsonProperty("file_id")
    private String fileId;

    /**
     * every_n_pages : 每 N 页拆分成一个 PDF
     * into_n_documents : 将 PDF 平均拆分成 N 个文件
     * odd:只保留奇数页
     * even:只保留偶数页
     * ranges:按自定义页码范围拆分
     */
    private String splitMode;

    private String splitArg;

    private String splitLabel;

    private String splitSeparator;

    private String splitOriginalNameFirst;
}
