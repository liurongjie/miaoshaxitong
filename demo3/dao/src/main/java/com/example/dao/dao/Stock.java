package com.example.dao.dao;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Stock {
    private Integer id;

    private String name;

    private Integer count;

    private Integer sale;

    private Integer version;
}
