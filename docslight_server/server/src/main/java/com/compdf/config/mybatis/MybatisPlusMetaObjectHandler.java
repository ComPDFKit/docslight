package com.compdf.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MybatisPlus自动填充设置
 *
 * @author ZhouQiang 2022/7/11
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        metaObject.setValue("createDate", now);
        metaObject.setValue("updateDate", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateDate", LocalDateTime.now());
    }

}
