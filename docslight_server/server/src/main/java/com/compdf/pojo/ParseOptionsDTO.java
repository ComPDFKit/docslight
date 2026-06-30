package com.compdf.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * llm_parser 解析选项参数
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseOptionsDTO {

    @JsonProperty("use_doc_unwarping")
    @Builder.Default
    private Boolean useDocUnwarping = false;

    @JsonProperty("use_chart_recognition")
    @Builder.Default
    private Boolean useChartRecognition = false;

    @JsonProperty("use_seal_recognition")
    @Builder.Default
    private Boolean useSealRecognition = false;

    @JsonProperty("use_ocr_for_image_block")
    @Builder.Default
    private Boolean useOcrForImageBlock = false;

    @JsonProperty("use_layout_detection")
    @Builder.Default
    private Boolean useLayoutDetection = true;

    @JsonProperty("layout_shape_mode")
    @Builder.Default
    private String layoutShapeMode = "auto";

    @JsonProperty("merge_tables")
    @Builder.Default
    private Boolean mergeTables = true;

    @JsonProperty("relevel_titles")
    @Builder.Default
    private Boolean relevelTitles = true;

    @JsonProperty("concatenate_pages")
    @Builder.Default
    private Boolean concatenatePages = false;

    /**
     * 支持设置: ["number", "footnote", "header", "header_image", "footer", "footer_image", "aside_text"]
     */
    @JsonProperty("ignore_labels")
    @Builder.Default
    private List<String> ignoreLabels = new ArrayList<>();
}


