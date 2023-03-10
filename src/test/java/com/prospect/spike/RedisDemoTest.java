package com.prospect.spike;

import com.prospect.spike.services.RedisService;
import com.prospect.spike.services.SeckillActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedisDemoTest {
    @Resource
    private RedisService redisService;

    @Test
    public void stockTest(){
        String value = redisService.setValue("stock:19",10L).getValue("stock:19");
        System.out.println(value);
    }

    @Test
    public void stockDeductValidatorTest(){
        boolean result =  redisService.stockDeductValidator("stock:19");
        System.out.println("result:"+result);
        String stock =  redisService.getValue("stock:19");
        System.out.println("stock:"+stock);
    }



}
