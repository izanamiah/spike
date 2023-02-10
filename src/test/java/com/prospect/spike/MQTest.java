package com.prospect.spike;

import com.prospect.spike.mq.RocketMQService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;

@SpringBootTest
public class MQTest {

    @Resource
    private RocketMQService service;

    @Test
    public void sendMQTest() throws Exception{
        service.sendMessage("test-jiuzhang","hello,world" + new Date().toString());
    }
}
