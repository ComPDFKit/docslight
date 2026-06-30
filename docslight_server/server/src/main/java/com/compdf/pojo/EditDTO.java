package com.compdf.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/8/8 星期五
 */
@Data
@ToString
public class EditDTO {

    private Integer page_id;
    private Integer id;
    /**
     * 用作添加时候传输，删除和更改不需要 <br/>
     * 坐标[左，上，右，上，右，下，左，下]
     */
    private List<Double> position;

    /**
     * add, delete, update
     */
    private String actionType;

    private String type;

    /**
     * 用作删除和更改时候传输，添加不需要
     */
    private Integer paragraph_id;

}
