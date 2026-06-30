package com.compdf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compdf.entity.TemplatePin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板置顶 Mapper
 *
 * @author ComPDFKit 2026/06/01
 */
public interface TemplatePinMapper extends BaseMapper<TemplatePin> {

    /**
     * 查询用户置顶的分组模板ID列表（按置顶时间倒序）
     *
     * @param userId 用户ID
     * @return 置顶的分组模板ID列表
     */
    List<String> selectPinnedGroupTemplateIds(@Param("userId") String userId);
}
