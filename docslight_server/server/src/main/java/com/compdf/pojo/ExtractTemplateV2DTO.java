package com.compdf.pojo;

import com.compdf.entity.ExtractFieldPojo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author ComPDFKit-WPH 2026/3/6
 */
//@EqualsAndHashCode(callSuper = true)
@Data
public class ExtractTemplateV2DTO {

    private Map<String, ExtractFieldPojo> keys;

    private String name;

    private String id;

    private String fileId;

    private Integer page;
    /**
     * 模板状态：0 未启用，1 启用，2 删除
     */
    private Integer status;
    private Map<String, Map<String, ExtractFieldPojo>> tableHeaders;

    public ExtractTemplateV2DTO() {
    }

    public ExtractTemplateV2DTO(ExtractTemplateDTO dto){
        this.name = dto.getName();
        this.id = dto.getId();
        this.fileId = dto.getFileId();
        this.page = dto.getPage();
        this.keys = dto.getKeys();
    }

}
