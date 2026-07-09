package com.compdf.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.io.Serializable;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
* 
* @TableName user
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user")
public class User extends BaseEntity implements Serializable  {

    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @Length(max= 32,message="编码长度不能超过32")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
    * 
    */
    private Long createTime;
    /**
    * 
    */
    private Long updateTime;
    /**
    * 
    */
    @Size(max= 255,message="编码长度不能超过255")
   
    @Length(max= 255,message="编码长度不能超过255")
    private String accessToken;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
   
    @Length(max= 100,message="编码长度不能超过100")
    private String nickname;
    /**
    * 
    */
    @Size(max= 255,message="编码长度不能超过255")
   
    @Length(max= 255,message="编码长度不能超过255")
    private String password;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
   
    @Length(max= 255,message="编码长度不能超过255")
    private String email;
    /**
    * 
    */
    @Size(max= 0,message="编码长度不能超过-1")
   
    @Length(max= 0,message="编码长度不能超过-1")
    private String avatar;
    /**
    * 
    */
    @Size(max= 32,message="编码长度不能超过32")
   
    @Length(max= 32,message="编码长度不能超过32")
    private String language;
    /**
    * 
    */
    @Size(max= 32,message="编码长度不能超过32")
   
    @Length(max= 32,message="编码长度不能超过32")
    private String colorSchema;
    /**
    * 
    */
    @Size(max= 64,message="编码长度不能超过64")
   
    @Length(max= 64,message="编码长度不能超过64")
    private String timezone;
    /**
    * 
    */
   
    private Date lastLoginTime;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 1,message="编码长度不能超过1")
   
    @Length(max= 1,message="编码长度不能超过1")
    private String isAuthenticated;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 1,message="编码长度不能超过1")
   
    @Length(max= 1,message="编码长度不能超过1")
    private String isActive;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 1,message="编码长度不能超过1")
   
    @Length(max= 1,message="编码长度不能超过1")
    private String isAnonymous;
    /**
    * 
    */
    @Size(max= 255,message="编码长度不能超过255")
   
    @Length(max= 255,message="编码长度不能超过255")
    private String loginChannel;
    /**
    * 
    */
    @Size(max= 1,message="编码长度不能超过1")
   
    @Length(max= 1,message="编码长度不能超过1")
    private String status;
    /**
    * 
    */
   
    private Integer isSuperuser;
    /**
    * User role: admin / manager / user
    */
    @NotBlank(message="[User role: admin / manager / user]不能为空")
    @Size(max= 36,message="编码长度不能超过36")
    @Length(max= 36,message="编码长度不能超过36")
    private String role;
    /**
    * Leader user ID
    */
    @Size(max= 36,message="编码长度不能超过36")
    @Length(max= 36,message="编码长度不能超过36")
    private String leaderId;
}
