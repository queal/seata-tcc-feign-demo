package com.fly.seata.controller;

import com.fly.seata.service.api.TccActionTwo;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@RestController
public class RmTwoController {

  @Autowired
  private TccActionTwo tccActionTwo;

  @GetMapping(value = "/rm2/test")
  public String test(){
    tccActionTwo.prepare(null,"1");
    return "ok";
  }

}
