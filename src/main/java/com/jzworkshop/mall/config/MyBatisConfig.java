package com.jzworkshop.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.jzworkshop.mall.mbg.mapper", "com.jzworkshop.mall.dao"})
public class MyBatisConfig {
}
