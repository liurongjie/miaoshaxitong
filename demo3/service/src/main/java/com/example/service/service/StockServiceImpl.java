package com.example.service.service;


import com.example.dao.dao.Stock;
import com.example.dao.mapper.StockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockServiceImpl.class);

   @Autowired
   private StockMapper stockMapper;



    @Override
    public Integer getStockCount(int sid) {
        Integer stockLeft;
        stockLeft = getStockCountByCache(sid);
        LOGGER.info("缓存中取得库存数：[{}]", stockLeft);
        if (stockLeft == null) {
            stockLeft = getStockCountByDB(sid);
            LOGGER.info("缓存未命中，查询数据库，并写入缓存");
            setStockCountCache(sid, stockLeft);
        }
        return stockLeft;
    }

    @Override
    public int getStockCountByDB(int id) {
        Stock stock = stockMapper.selectByPrimaryKey(id);
        return stock.getCount() - stock.getSale();
    }

    @Override
    public Integer getStockCountByCache(int id) {
      return 1;
    }

    @Override
    public void setStockCountCache(int id, int count) {
       return;
    }

    @Override
    public void delStockCountCache(int id) {

        LOGGER.info("删除商品id：[{}] 缓存", id);
    }

    @Override
    public Stock getStockById(int id) {
        return stockMapper.selectByPrimaryKey(id);
    }

    @Override
    public Stock getStockByIdForUpdate(int id) {
        return stockMapper.selectByPrimaryKeyForUpdate(id);
    }



    @Override
    public int updateStockById(Stock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockByOptimistic(Stock stock) {
        return stockMapper.updateByOptimistic(stock);
    }
}
