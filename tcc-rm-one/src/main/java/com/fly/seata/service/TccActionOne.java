package com.fly.seata.service;

import com.fly.seata.dto.OrderDTO;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@LocalTCC
public interface TccActionOne {

  /**
   * Prepare boolean.
   *
   * @param actionContext the action context
   * @param order             the a
   * @return the boolean
   */
  @TwoPhaseBusinessAction(name = "TccActionOne" , commitMethod = "commit", rollbackMethod = "rollback")
  boolean createOrder(BusinessActionContext actionContext,@BusinessActionContextParameter(paramName = "order") OrderDTO order);

  /**
   * Commit boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  boolean commit(BusinessActionContext actionContext);

  /**
   * Rollback boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  boolean rollback(BusinessActionContext actionContext);

}
