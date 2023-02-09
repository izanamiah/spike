package com.jiuzhang.seckill.services;

import com.alibaba.fastjson.JSON;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.po.Order;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.mq.RocketMQService;
import com.jiuzhang.seckill.util.SnowFlake;
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

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RocketMQService rocketMQService;


    /**
     * datacenterId; 数据中心
     * machineId; 机器标识
     * 在分布式环境中可以从机器配置上读取
     * 单机开发环境中先写死
     */

    private SnowFlake snowFlake = new SnowFlake(1,1);



     /**
     * 创建订单
     *
     * @param seckillActivityId
      * @param userId
      * @return
      * @throws Exception
      */

    public Order createOrder(long seckillActivityId, long userId) throws Exception{
        SeckillActivity activity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        //Use SnowFlake to Generate Order
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(activity.getId());
        order.setUserId(userId);
        order.setOrderAmount(activity.getSeckillPrice().longValue());
        //Send Order Creation Message
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        return order;
    }

}
