package com.example.dao.dao;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class StockOrder {
    private Integer id;
    private Integer sid;
    private String name;
    private Integer userId;
    private Date createTime;
}
