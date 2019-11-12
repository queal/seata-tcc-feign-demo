package com.fly.seata.controller;

import com.fly.seata.service.api.TccActionOne;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * tcc rm分支注册
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@RestController
public class RmOneController {

  @Autowired
  private TccActionOne tccActionOne;

  @GetMapping(value = "/rm1/test")
  public String rmOnetest(){
    tccActionOne.prepare(null,1);
    return "ok";
  }

}
