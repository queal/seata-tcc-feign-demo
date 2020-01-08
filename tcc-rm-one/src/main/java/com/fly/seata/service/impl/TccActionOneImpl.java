package com.fly.seata.service.impl;

import com.fly.seata.dao.OrderDao;
import com.fly.seata.domain.Order;
import com.fly.seata.dto.OrderDTO;
import com.fly.seata.service.TccActionOne;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@Service
public class TccActionOneImpl implements TccActionOne {

  private final static Logger LOGGER = LoggerFactory.getLogger(TccActionOneImpl.class);

  @Autowired
  private OrderDao orderDao;

  @Override
  public boolean createOrder(BusinessActionContext actionContext, OrderDTO order) {
    if(null == actionContext){
      return false;
    }
    String xid = actionContext.getXid();
    LOGGER.info("TccActionOne prepare, xid:" + xid);
    orderDao.insert(order);
    return true;
  }

  @Override
  public boolean commit(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();
    LOGGER.info("TccActionOne commit, xid:" + xid);
    return true;
  }

  @Override
  public boolean rollback(BusinessActionContext actionContext) {
    String xid = actionContext.getXid();

    //考虑空回滚
    LOGGER.info("TccActionOne rollback, xid:" + xid);
    return true;
  }

}
