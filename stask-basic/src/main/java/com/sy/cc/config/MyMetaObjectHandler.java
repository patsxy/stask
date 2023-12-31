package com.sy.cc.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自动填充处理类
 * @author sy
 * @version 1.0
 * @see
 **/
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {


    @Override  //在执行mybatisPlus的insert()时，为我们自动给某些字段填充值，这样的话，我们就不需要手动给insert()里的实体类赋值了
    public void insertFill(MetaObject metaObject) {
        //其中方法参数中第一个是前面自动填充所对应的字段，第二个是要自动填充的值。第三个是指定实体类的对象
//        this.setFieldValByName("createTime", new Date(), metaObject);
//        this.setFieldValByName("updateTime", new Date(), metaObject);
        this.strictInsertFill(metaObject, "createTime", () -> new Date(), Date.class);
        this.strictInsertFill(metaObject, "updateTime", () -> new Date(), Date.class);

    }

    @Override//在执行mybatisPlus的update()时，为我们自动给某些字段填充值，这样的话，我们就不需要手动给update()里的实体类赋值了
    public void updateFill(MetaObject metaObject) {
       // this.setFieldValByName("updateTime", new Date(), metaObject);
        this.strictInsertFill(metaObject, "updateTime", () -> new Date(), Date.class);
    }
}