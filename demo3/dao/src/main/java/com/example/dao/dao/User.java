package com.example.dao.dao;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
    private Long id;
    private String userName;
    public User(Long id,String userName){
        this.id=id;
        this.userName=userName;
    }
    public User(){
    }
}
