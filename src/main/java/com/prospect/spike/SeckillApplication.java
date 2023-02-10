package com.prospect.spike;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.prospect.spike.db.mappers")
@ComponentScan(basePackages = {"com.prospect"})
public class SeckillApplication {
    public static void main(String[] args) {

        SpringApplication.run(SeckillApplication.class, args);
    }
}