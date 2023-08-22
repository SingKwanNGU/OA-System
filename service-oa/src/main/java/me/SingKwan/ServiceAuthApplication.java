package me.SingKwan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth
 * @className: ServiceAuthApplication
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/2 17:10
 * @version: 1.0
 */


@SpringBootApplication
public class ServiceAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAuthApplication.class,args);
    }
}
