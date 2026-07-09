package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("idp_file")
public class IdpFile extends BaseEntity {

    /**
     * convert time
     */
    private Long convertTime;

    /**
     * failure reason
     */
    private String failureReason;
    /**
     * failure Code
     */
    private String failureCode;
    /**
     * file name
     */
    private String fileName;
    /**
     * file path
     */
    private String filePath;
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 
     */
    private String outFileName;
    /**
     * 
     */
    private String outFilePath;
    /**
     * server id
     */
    private String serviceId;
    /**
     * file handle status. 0:create,1:处理中,2:success,3:fail
     */
    private Integer status;
    /**
     * TASK: EXTRACTION / PARSE / SPLIT
     */
    private String taskType;
    /**
     * task id
     */
    private String taskId;

    private String parameter;

    private String userId;
    private String type;
    private Integer pageCount;
    private String otherInfo;
    private Integer bulkOrder;

    /**
     * File source: UPLOAD / DMS / ...
     */
    private String source;

    /**
     * Group-template relation ID
     */
    private String groupTemplateId;

    /**
     * Review status: 0=pending, 1=finished
     */
    private Integer reviewStatus;
}
