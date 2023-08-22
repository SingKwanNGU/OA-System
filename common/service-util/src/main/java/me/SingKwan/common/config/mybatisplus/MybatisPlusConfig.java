package me.SingKwan.common.config.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.config.mybatisplus
 * @className: MybatisPlusConfig
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/4 16:46
 * @version: 1.0
 */
@MapperScan(basePackages = {"me.SingKwan.auth.mapper","me.SingKwan.process.mapper","me.SingKwan.wechat.mapper"})
@Configuration
public class MybatisPlusConfig {
        /**
         * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
         */
        @Bean
        public MybatisPlusInterceptor mybatisPlusInterceptor() {
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            return interceptor;
        }

        @Bean
        public ConfigurationCustomizer configurationCustomizer() {
            return configuration -> configuration.setUseDeprecatedExecutor(false);
        }



}
