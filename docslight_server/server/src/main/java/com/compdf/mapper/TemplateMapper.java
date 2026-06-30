package com.compdf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compdf.entity.Template;
import com.compdf.pojo.GroupPojo;
import com.compdf.pojo.GroupTemplatePojo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
public interface TemplateMapper extends BaseMapper<Template> {

    void insertGroupInfo(String id, String groupName, String userId);

    void insertGroupTemplate(@Param("id") String id, @Param("groupId") String groupId, @Param("templateId") String templateId, @Param("order") int order);

    int restoreGroupTemplate(@Param("groupId") String groupId, @Param("templateId") String templateId, @Param("order") int order);

    List<GroupPojo> selectGroupInfoByUserId(String userId);

    List<GroupTemplatePojo> selectGroupTemplatesByGroupId(@Param("groupId") String groupId, @Param("userId") String userId);

    List<GroupTemplatePojo> selectAllGroupTemplatesByGroupId(@Param("groupId") String groupId);

    List<GroupTemplatePojo> selectGroupTemplatesByGroupIds(@Param("groupIds") List<String> groupIds, @Param("userId") String userId);

    List<GroupTemplatePojo> selectGroupTemplatesByGroupTemplateIds(@Param("groupTemplateIds") List<String> groupTemplateIds);

    List<String> selectTemplatesByLeaderId(@Param("userId") String userId, @Param("leaderId") String leaderId);

    GroupTemplatePojo selectGroupTemplateByGroupTemplateId(@Param("groupTemplateId") String groupTemplateId);

    Template selectTemplateByGroupTemplateId(@Param("groupTemplateId") String groupTemplateId);

    void deleteGroupTemplate(@Param("groupTemplateId") String groupTemplateId);

    void deleteGroupTemplateByGroupIdAndTemplateId(@Param("groupId") String groupId, @Param("templateId") String templateId);

    List<GroupTemplatePojo> selectGroupTemplateByTemplateId(String templateId);
}
