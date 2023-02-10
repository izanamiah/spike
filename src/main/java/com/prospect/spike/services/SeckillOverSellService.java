package com.prospect.spike.services;

import com.prospect.spike.db.dao.SeckillActivityDao;
import com.prospect.spike.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    public String processSeckill(long activityId){
        SeckillActivity activity = seckillActivityDao.querySeckillActivityById(activityId);
        int availableStock = activity.getAvailableStock();
        String result;

        if (availableStock > 0){
            result = "恭喜，抢购成功";
            System.out.println(result);
            availableStock -= 1;
            activity.setAvailableStock(availableStock);
            seckillActivityDao.updateSeckillActivity(activity);
        } else {
            result = "抱歉，抢购失败，商品被抢完了";
            System.out.println(result);
        }

        return result;
    }
}
