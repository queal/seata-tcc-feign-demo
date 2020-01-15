package com.fly.seata.controller;

import com.fly.seata.service.StorageIncreaseService;
import com.fly.seata.service.TccActionTwo;
import io.seata.rm.tcc.api.BusinessActivityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @Autowired
  private StorageIncreaseService storageIncreasePrepare;

  @GetMapping(value = "/storage/reduce/{productId}/{count}")
  public Boolean reduceStorage(@PathVariable("productId") long productId,@PathVariable("count") Integer count){
    return tccActionTwo.storageReducePrepare(null,productId,count);
  }

  @GetMapping(value = "/storage/increase")
  public void increaseStorage(){
    BusinessActivityContext businessActivityContext = new BusinessActivityContext();
    storageIncreasePrepare.storageIncreasePrepare(null,businessActivityContext);
  }

}
