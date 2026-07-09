package com.compdf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compdf.entity.User;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
public interface UserMapper extends BaseMapper<User> {

    int selectPermissionByUserIdAndPermission(@NotBlank(message="[]不能为空") @Size(max= 36,message="编码长度不能超过36") @Length(max= 36,message="编码长度不能超过36") String userId, String code);

}
