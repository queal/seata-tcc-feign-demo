package com.fly.seata.feign.api;

import com.fly.seata.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@FeignClient(value = "seata-tcc-rm-one")
public interface RmOneApi {

  @PostMapping(value = "/order/create",consumes = MediaType.APPLICATION_JSON_VALUE)
  public String createOrder(@RequestBody OrderDTO order);

}
