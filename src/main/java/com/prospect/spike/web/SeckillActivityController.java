package com.prospect.spike.web;

import com.prospect.spike.db.dao.OrderDao;
import com.prospect.spike.db.dao.SeckillActivityDao;
import com.prospect.spike.db.dao.SeckillCommodityDao;
import com.prospect.spike.db.po.Order;
import com.prospect.spike.db.po.SeckillActivity;
import com.prospect.spike.db.po.SeckillCommodity;
import com.prospect.spike.services.RedisService;
import com.prospect.spike.services.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {
    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";
    }
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @ResponseBody
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException {
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        List<SeckillActivity> seckillActivities =
                seckillActivityDao.querySeckillActivitysByStatus(1);
        resultMap.put("seckillActivities", seckillActivities);
        return "seckill_activity";
    }

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    @Autowired
    SeckillActivityService seckillActivityService;
    /**
     * 处理抢购请求
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @Resource
    private RedisService redisService;
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(
            @PathVariable long userId,
            @PathVariable long seckillActivityId
    ){
        boolean stockValidateResult = false;
        ModelAndView modelAndView = new ModelAndView();

        /*
         * 判断用户是否在已购名单中
         */
        if (redisService.isInLimitMember(seckillActivityId, userId)) {
            //提示用户已经在限购名单中，返回结果
            modelAndView.addObject("resultInfo", "对不起，您已经在限购名单中");
            modelAndView.setViewName("seckill_result");
            return modelAndView;
        }

        try {
            /*
             * 确认是否能够进行秒杀
             * */
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo","秒杀成功，订单创建中，订单ID:" + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                redisService.addLimitMember(seckillActivityId,userId);
            } else {
                modelAndView.addObject("resultInfo","对不起，商品库存不足");
            }
        } catch (Exception exception) {
            log.error("秒杀系统异常" + exception.toString());
            modelAndView.addObject("resultInfo","秒杀失败");
        }

        modelAndView.setViewName("seckill_result");
        return modelAndView;

    }

    /**
     * 订单查询
     * @param orderNo * @return
     */

    @Resource
    private OrderDao orderDao;

    @RequestMapping("seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(
            @PathVariable String orderNo
    ){
      log.info("订单查询，订单号" + orderNo);
      Order order = orderDao.queryOrder(orderNo);
      ModelAndView modelAndView = new ModelAndView();

      if (order != null) {
          modelAndView.setViewName("order");
          modelAndView.addObject("order",order);
          SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
          modelAndView.addObject("seckillActivity",seckillActivity);
      } else {
          modelAndView.setViewName("order_wait");
      }
      return modelAndView;
    }


    /**
     * 订单支付
     * * @return
     * */

    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(
            @PathVariable String orderNo
    ) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return"redirect:/seckill/orderQuery/" + orderNo;
    }


    /**
     * 同步抢购倒计时
     * 获取当前服务器端的时间
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime() {
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // new Date()为获取当前系统时间
        String date = df.format(new Date());
        return date;
    }




};

