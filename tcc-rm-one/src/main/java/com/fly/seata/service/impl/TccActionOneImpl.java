package com.fly.seata.service.impl;

import com.fly.seata.service.api.TccActionOne;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@Service
public class TccActionOneImpl implements TccActionOne {

  @Override
  public boolean prepare(BusinessActionContext actionContext, int a) {
    if(null == actionContext){
      return false;
    }
    String xid = actionContext.getXid();
    System.out.println("TccActionOne prepare, xid:" + xid);
    return true;
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
