package com.prospect.spike.web;

import com.prospect.spike.services.SeckillActivityService;
import com.prospect.spike.services.SeckillOverSellService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class SeckillOverSellController {

    @Resource
    private SeckillOverSellService seckillOverSellService;

    @Resource
    private SeckillActivityService seckillActivityService;


//    /**
//     * 简单 处理抢购请求
//     * @param seckillActivityId * @return
//     */
//    @ResponseBody
//    @RequestMapping("/seckill/{seckillActivityId}")
//    public String  seckil(@PathVariable long seckillActivityId){
//        return seckillOverSellService.processSeckill(seckillActivityId);
//    }

    /**
     * 使用 lua 脚本处理抢购请求
     * @param seckillActivityId * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId){
        boolean stockValidateResult =  seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidateResult ? "恭喜秒杀成功" : "商品已售完，请下次再来";
    }

}
