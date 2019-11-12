package com.fly.seata.feign.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@FeignClient(value = "tcc-rm-one")
public interface RmOneApi {

  @RequestMapping(value="/rm1/test", method= RequestMethod.GET)
  public String rmOnetest();

}
