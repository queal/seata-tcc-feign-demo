package com.fly.seata.service.impl;

import com.fly.seata.dao.StorageDao;
import com.fly.seata.service.TccActionTwo;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2019-11-12
 * @Description:
 */
@Service
public class TccActionTwoImpl implements TccActionTwo {

  private final static Logger LOGGER = LoggerFactory.getLogger(TccActionTwoImpl.class);

  @Autowired
  private StorageDao storageDao;

  @Override
  public boolean prepare(BusinessActionContext actionContext,long productId,int count) {
    if(null == actionContext) {
      return false;
    }
    String xid = actionContext.getXid();
    LOGGER.info("TccActionOne prepare, xid:" + xid);
    storageDao.fozen(productId,count);
    return false;
  }

  @Override
  public boolean commit(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();
    LOGGER.info("TccActionOne commit, xid:" + xid);
//    storageDao.reduce()
    return true;
  }

  @Override
  public boolean rollback(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();
    LOGGER.info("TccActionOne rollback, xid:" + xid);
//    storageDao.rollback()
    return true;
  }

}
