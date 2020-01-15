package com.fly.seata.feign.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@FeignClient(name = "seata-tcc-rm-two")
public interface RmTwoApi {

  @GetMapping(value = "/storage/reduce/{productId}/{count}")
  public Boolean reduceStorage(@PathVariable("productId") long productId,@PathVariable("count") Integer count);

  @GetMapping(value = "/storage/increase")
  public void increaseStorage();
}
