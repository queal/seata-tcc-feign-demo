package com.fly.seata.service.impl;

import com.fly.seata.service.api.TccActionTwo;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@Service
public class TccActionTwoImpl implements TccActionTwo {

  @Override
  public boolean prepare(BusinessActionContext actionContext, String b) {
    if(null == actionContext) {
      return false;
    }
    throw new RuntimeException("模拟抛出异常");
//    String xid = actionContext.getXid();
//    System.out.println("TccActionOne prepare, xid:" + xid);
//    return false;
  }

  @Override
  public boolean commit(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();
    System.out.println("TccActionOne commit, xid:" + xid);
    return true;
  }

  @Override
  public boolean rollback(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();
    System.out.println("TccActionOne rollback, xid:" + xid);
    return true;
  }

}
