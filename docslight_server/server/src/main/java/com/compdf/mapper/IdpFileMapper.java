package com.compdf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compdf.entity.IdpFile;
import com.compdf.pojo.UserInfoPojo;
import org.apache.ibatis.annotations.Param;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface IdpFileMapper extends BaseMapper<IdpFile> {

    String selectLeaderIdByUserId(String userId);

    UserInfoPojo selectLeaderIdAndRoleByUserId(String userId);
}
