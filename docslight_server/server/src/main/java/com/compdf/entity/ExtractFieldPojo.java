package com.compdf.entity;

import lombok.*;

/**
 * @author ComPDFKit-WPH 2026/2/2
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractFieldPojo {

    private String prompt;

    private String mapping;

}
