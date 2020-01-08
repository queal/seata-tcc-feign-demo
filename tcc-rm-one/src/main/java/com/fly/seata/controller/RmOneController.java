package com.fly.seata.controller;

import com.fly.seata.dto.OrderDTO;
import com.fly.seata.service.TccActionOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * tcc rm分支注册--下单服务
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@RestController
public class RmOneController {

  @Autowired
  private TccActionOne tccActionOne;

  @PostMapping(value = "/order/create",consumes = MediaType.APPLICATION_JSON_VALUE)
  public String createOrder(@RequestBody OrderDTO order){
    tccActionOne.createOrder(null,order);
    return "ok";
  }

}
