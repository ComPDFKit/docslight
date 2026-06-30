package com.compdf.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author ComPDFKit-WPH 2025/6/25 星期三
 */
@Data
@ToString
@Builder
public class FileScheduleDTO {

    private String fileId;
    /**
     * 是否暂停
     */
    private Boolean isPause;
    /**
     * 总页数
     */
    private Integer totalPageCount;
    /**
     * 当前页
     */
    private Integer currentPageCount;
}
