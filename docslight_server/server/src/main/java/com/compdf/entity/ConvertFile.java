package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("convert_file")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertFile extends BaseEntity {

    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * Original filename
     */
    private String fileName;
    /**
     * Source file download address
     */
    private String fileUrl;
    /**
     * Source file open password
     */
    private String filePassword;
    /**
     * image file
     */
    private String imageUrl;
    /**
     * Conversion Complete Download Address
     */
    private String downloadUrl;
    /**
     * Conversion Complete Download file name
     */
    private String downloadFileName;
    /**
     * Document Conversion Format
     */
    private String convertType;
    /**
     * status
     */
    private String status;
    /**
     * failure_code
     */
    private String failureCode;
    /**
     * failure_reason
     */
    private String failureReason;
    /**
     * file_parameter
     */
    private String fileParameter;
    /**
     * containerId
     */
    private String containerId;
//    /**
//     * created time
//     */
//    private LocalDateTime createdAt;
//    /**
//     * updated time
//     */
//    private LocalDateTime updatedAt;
    /**
     * 0 - Normal, 1 - Deleted
     */
    private Integer delFlag;

}
