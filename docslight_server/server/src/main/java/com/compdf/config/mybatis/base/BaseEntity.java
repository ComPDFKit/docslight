package com.compdf.config.mybatis.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties({"page", "size"})
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public BaseEntity() {
        this.page = 0L;
        this.size = 10L;
    }

    /**
     * 当前页
     */
    @TableField(exist = false)
    private Long page;

    /**
     * 当前页行数
     */
    @TableField(exist = false)
    private Long size;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
}
