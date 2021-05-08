package com.example.service.service;

import com.example.dao.dao.Stock;
import com.example.dao.dao.User;
import com.example.dao.mapper.UserMapper;
import com.example.dao.utils.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String SALT = "randomString";
    private static final int ALLOW_COUNT = 10;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StockService stockService;

    @Override
    public String getVerifyHash(Integer sid, Integer userId) throws Exception {

        // 验证是否在抢购时间内
        LOGGER.info("请自行验证是否在抢购时间内");


        // 检查用户合法性
        User user = userMapper.selectByPrimaryKey(userId.longValue());
        if (user == null) {
            throw new Exception("用户不存在");
        }
        LOGGER.info("用户信息：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new Exception("商品不存在");
        }
        LOGGER.info("商品信息：[{}]", stock.toString());

        // 生成hash
        String verify = SALT + sid + userId;
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());
        String hashKey= CacheKey.HASH_KEY.getKey()+"_"+sid+"_"+userId;
        stringRedisTemplate.opsForValue().set(hashKey,verifyHash,3600,TimeUnit.SECONDS);
        LOGGER.info("Redis写入:[{}][{}]",hashKey,verifyHash);

        // 将hash和用户商品信息存入redis

        return hashKey;
    }

    @Override
    public int addUserCount(Integer userId) throws Exception {
     return 1;
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
       return true;
    }
}
