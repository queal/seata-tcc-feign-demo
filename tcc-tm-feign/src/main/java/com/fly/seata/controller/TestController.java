package com.fly.seata.controller;

import com.fly.seata.dto.OrderDTO;
import com.fly.seata.feign.api.RmOneApi;
import com.fly.seata.feign.api.RmTwoApi;
import io.seata.spring.annotation.GlobalTransactional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/tm/purchase")
  public String test(HttpServletRequest request,@RequestBody OrderDTO orderDTO){
      String orderNo = Long.valueOf(RandomUtils.nextLong()).toString();
      orderDTO.setOrderNo(orderNo);
      rmOneApi.createOrder(orderDTO);
      //更新操作-热点数据测试
      boolean bol = rmTwoApi.reduceStorage(orderDTO.getProductId(),orderDTO.getCount());
      if(!bol){
          throw new RuntimeException("库存扣减失败");
      }
      return "ok";
  }

}
