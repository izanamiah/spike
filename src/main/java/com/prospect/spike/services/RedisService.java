package com.prospect.spike.services;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class RedisService {
    @Resource //equavalent to @Autowire in the case of duplicated name
    private JedisPool jedisPool;

    public RedisService setValue(String key, Long value){
        Jedis client = jedisPool.getResource();
        client.set(key,value.toString());
        client.close();
        return this;
    }

    public String getValue(String key){
        Jedis client = jedisPool.getResource();
        String value = client.get(key);
        client.close();
        return value;
    }

    /**
     * 缓存中库存判断和扣减 * @param key
     * @return
     * @throws Exception */

    public boolean stockDeductValidator(String key){
        try (Jedis client = jedisPool.getResource()) {
            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "    local stock = tonumber(redis.call('get',KEYS[1]))\n" +
                    "    if (stock <= 0) then\n" +
                    "        return -1\n" +
                    "    end;\n" +
                    "    \n" +
                    "    redis.call('decr',KEYS[1]);\n" +
                    "    return stock -1;\n" +
                    "end;\n" +
                    "\n" +
                    "return -1;";
            Long stock = (Long) client.eval(script, Collections.singletonList(key), Collections.emptyList());

            if (stock < 0) {
                System.out.println("库存不足");
                return false;
            }

            System.out.println("恭喜抢购成功");
            return true;

        } catch (Throwable throwable) {
            System.out.println("库存扣减失败" + throwable.toString());
            return false;
        }
    }

    /**
     * 超时未支付 Redis 库存回滚 *
     ** @param key
     */
    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }
}
