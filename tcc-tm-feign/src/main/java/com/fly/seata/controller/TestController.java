package com.fly.seata.controller;

import com.fly.seata.feign.api.RmOneApi;
import com.fly.seata.feign.api.RmTwoApi;
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
public class TestController {

  @Autowired
  private RmOneApi rmOneApi;

  @Autowired
  private RmTwoApi rmTwoApi;

  @GlobalTransactional
  @GetMapping("/tm/test")
  public String test(){
    String result = rmOneApi.rmOnetest();
    System.out.println("result:"+result);
    result = rmTwoApi.rmTwotest();
    System.out.println("result:"+result);
    return "ok";
  }

}
