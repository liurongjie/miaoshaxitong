package com.example.service.service;

import com.example.dao.dao.Stock;
import com.example.dao.dao.StockOrder;
import com.example.dao.mapper.StockOrderMapper;
import com.example.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);


    @Autowired
    private StockService stockService;

    @Autowired
    private StockOrderMapper orderMapper;

  //  @Autowired
  //  private UserMapper userMapper;

    @Override
    public int createWrongOrder(int sid) {
        //校验库存
        Stock stock = checkStock(sid);
        //扣库存
        saleStock(stock);
        //创建订单
        int id = createOrder(stock);
        return id;
    }

    @Override
    public int createOptimisticOrder(int sid) {
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        boolean success = saleStockOptimistic(stock);
        if (!success){
            throw new RuntimeException("过期库存值，更新失败");
        }
        //创建订单
        createOrder(stock);
        return stock.getCount() - (stock.getSale()+1);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int createPessimisticOrder(int sid){
        //校验库存(悲观锁for update)
        Stock stock = checkStockForUpdate(sid);
        //更新库存
        saleStock(stock);
        //创建订单
        createOrder(stock);
        return stock.getCount() - (stock.getSale());
    }

    @Override
    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) throws Exception {



        return 1;
    }

    @Override
    public void createOrderByMq(Integer sid, Integer userId) throws Exception {

        // 模拟多个用户同时抢购，导致消息队列排队等候10秒
        Thread.sleep(10000);

        Stock stock;
        //校验库存（不要学我在trycatch中做逻辑处理，这样是不优雅的。这里这样处理是为了兼容之前的秒杀系统文章）
        try {
            stock = checkStock(sid);
        } catch (Exception e) {
            LOGGER.info("库存不足！");
            return;
        }
        //乐观锁更新库存
        boolean updateStock = saleStockOptimistic(stock);
        if (!updateStock) {
            LOGGER.warn("扣减库存失败，库存已经为0");
            return;
        }

        LOGGER.info("扣减库存成功，剩余库存：[{}]", stock.getCount() - stock.getSale() - 1);
        stockService.delStockCountCache(sid);
        LOGGER.info("删除库存缓存");

        //创建订单
        LOGGER.info("写入订单至数据库");
        createOrderWithUserInfoInDB(stock, userId);
        LOGGER.info("写入订单至缓存供查询");
        createOrderWithUserInfoInCache(stock, userId);
        LOGGER.info("下单完成");

    }

    @Override
    public Boolean checkUserOrderInfoInCache(Integer sid, Integer userId) throws Exception {
       return false;
    }

    /**
     * 检查库存
     * @param sid
     * @return
     */
    private Stock checkStock(int sid) {
        Stock stock = stockService.getStockById(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 检查库存 ForUpdate
     * @param sid
     * @return
     */
    private Stock checkStockForUpdate(int sid) {
        Stock stock = stockService.getStockByIdForUpdate(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 更新库存
     * @param stock
     */
    private void saleStock(Stock stock) {
        stock.setSale(stock.getSale() + 1);
        stockService.updateStockById(stock);
    }

    /**
     * 更新库存 乐观锁
     * @param stock
     */
    private boolean saleStockOptimistic(Stock stock) {
        LOGGER.info("查询数据库，尝试更新库存");
        int count = stockService.updateStockByOptimistic(stock);
        return count != 0;
    }

    /**
     * 创建订单
     * @param stock
     * @return
     */
    private int createOrder(Stock stock) {
        StockOrder order=new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        int id=orderMapper.insertSelective(order);
        return id;
    }

    /**
     * 创建订单：保存用户订单信息到数据库
     * @param stock
     * @return
     */
    private int createOrderWithUserInfoInDB(Stock stock, Integer userId) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setUserId(userId);
        return orderMapper.insertSelective(order);
    }

    /**
     * 创建订单：保存用户订单信息到缓存
     * @param stock
     * @return 返回添加的个数
     */
    private Long createOrderWithUserInfoInCache(Stock stock, Integer userId) {
       return Long.valueOf(1);
    }
}
