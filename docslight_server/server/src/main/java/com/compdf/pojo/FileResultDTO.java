package com.compdf.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ComPDFKit-WPH 2025/8/8 星期五
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResultDTO {

    private String fileId;
    private String downFileUrl;

}
