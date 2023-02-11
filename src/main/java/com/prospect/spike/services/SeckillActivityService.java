package com.prospect.spike.services;

import com.alibaba.fastjson.JSON;
import com.prospect.spike.db.dao.OrderDao;
import com.prospect.spike.db.dao.SeckillActivityDao;
import com.prospect.spike.db.po.Order;
import com.prospect.spike.db.po.SeckillActivity;
import com.prospect.spike.mq.RocketMQService;
import com.prospect.spike.util.SnowFlake;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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
      * @param seckillActivityId
      * @param userId
      * @return
      * @throws Exception
      **/

    public Order createOrder(long seckillActivityId, long userId) throws Exception{

        //Create Order
        SeckillActivity activity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        //Use SnowFlake to Generate Order
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(activity.getId());
        order.setUserId(userId);
        order.setOrderAmount(activity.getSeckillPrice().longValue());

        //2. Send Order Creation Message
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        /*
        * 3.发送订单付款状态校验消息
        * 开源RocketMQ支持延迟消息，但是不支持秒级精度。默认支持18个level的延迟消息，这是通
        * 过broker端的messageDelayLevel配置项确定的，如下:
        * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m
        30m 1h 2h
        * */

        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order),3);

        return order;
    }

    @Resource
    private OrderDao orderDao;

    /**
     * 订单支付完成处理
     * * @param orderNo */
    public void payOrderProcess(String orderNo) {
        Order order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = seckillActivityDao.deductStock(order.getSeckillActivityId());

        if (deductStockResult) {
            order.setPayTime(new Date());
            // 0 没有可用库存，无效订单
            // 1 已创建并等待支付
            // 2 完成支付
            order.setOrderStatus(2);
            orderDao.updateOrder(order);
        }
    }



}
