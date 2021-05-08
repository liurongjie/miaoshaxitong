package com.example.web.control;

import com.atlassian.guava.common.util.concurrent.RateLimiter;
import com.example.service.service.OrderService;
import com.example.service.service.StockService;
import com.example.service.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Controller
public class OrderController {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;


    //每秒放行10个请求
    RateLimiter rateLimiter = RateLimiter.create(10);

    @Autowired
    private StockService stockService;

    @RequestMapping("createWrongOrder/{sid}")
    @ResponseBody
    public String createWrongOrder(@PathVariable int sid) {
        LOGGER.info("购买物品编号sid=[{}]", sid);
        int id = 0;
        try {
            id = orderService.createWrongOrder(sid);
            LOGGER.info("创建订单id:[{}]", id);


        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return String.valueOf(id);

    }

    @RequestMapping("createOptimisticOrder/{sid}")
    @ResponseBody
    public String createOptimisticOrder(@PathVariable int sid) {
        LOGGER.info("等待时间");
        rateLimiter.acquire();
        // 非阻塞式获取令牌
//        if(!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
//            LOGGER.warn("你被限流了，真不幸");
//            return "购买失败，库存不足";
//        }
        LOGGER.info("购买物品编号sid=[{}]", sid);
        int id = 0;
        try {
            id = orderService.createOptimisticOrder(sid);
            LOGGER.info("购买成功");
        } catch (Exception e) {
            LOGGER.error("购买失败[{}]", e.getMessage());
            return "购买失败，内存不足";
        }
        return String.valueOf(id);
    }

    @RequestMapping("createPessimisticOrder/{sid}")
    @ResponseBody
    public String createPessimisticOrder(@PathVariable int sid) {
        LOGGER.info("等待时间");
        rateLimiter.acquire();
        // 非阻塞式获取令牌
//        if(!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
//            LOGGER.warn("你被限流了，真不幸");
//            return "购买失败，库存不足";
//        }
        LOGGER.info("购买物品编号sid=[{}]", sid);
        int id = 0;
        try {
            id = orderService.createPessimisticOrder(sid);
            LOGGER.info("购买成功");
        } catch (Exception e) {
            LOGGER.error("购买失败[{}]", e.getMessage());
            return "购买失败，内存不足";
        }
        return String.valueOf(id);

    }
    @RequestMapping("getVerifyHash/{sid}/{userId}")
    @ResponseBody
    public String getVerifyHash(@PathVariable int sid,@PathVariable int userId){
        String hash;
        try{
            hash=userService.getVerifyHash(sid,userId);
        }catch (Exception e){
            LOGGER.error("获取验证hash失败,原因:");
            e.printStackTrace();
            return "获取验证hash失败";
        }
        return String.format("请求请购验证hash值为:%s",hash);
    }



















}
