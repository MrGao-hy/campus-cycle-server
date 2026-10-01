package com.campus.cycle.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 公共字段自动填充（createTime / updateTime 统一为毫秒时间戳）
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        long now = System.currentTimeMillis();
        this.strictInsertFill(metaObject, "createTime", Long.class, now);
        this.strictInsertFill(metaObject, "updateTime", Long.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Long.class, System.currentTimeMillis());
    }
}
