package com.jiuzhang.seckill.services;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SeckillActivityService {

    @Resource
    private RedisService service;

    /**
     * 判断商品是否还有库存
     * @param activityId 商品ID
     * * @return
     * */

    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return service.stockDeductValidator(key);
    }

}
