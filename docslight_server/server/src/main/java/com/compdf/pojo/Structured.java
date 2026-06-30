/**
 * Copyright 2025 bejson.com
 */
package com.compdf.pojo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2025-06-03 14:19:32
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",  // 用于区分的JSON字段名
        defaultImpl = Structured.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StructuredTextblock.class, name = "textblock"),
        @JsonSubTypes.Type(value = StructuredTable.class, name = "table"),
        @JsonSubTypes.Type(value = StructuredImageblock.class, name = "image"),
        @JsonSubTypes.Type(value = StructuredFooter.class, name = "footer"),
        @JsonSubTypes.Type(value = StructuredHeader.class, name = "header")
})
public class Structured {

    private List<Integer> pos;
//    private String type;
    private int id;


}