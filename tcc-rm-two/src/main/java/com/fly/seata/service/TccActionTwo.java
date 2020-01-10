package com.fly.seata.service;

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
public interface TccActionTwo {

  /**
   * Prepare boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  @TwoPhaseBusinessAction(name = "TccActionTwo" , commitMethod = "commit", rollbackMethod = "rollback")
  boolean prepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "productId")long productId,@BusinessActionContextParameter(paramName = "count")int count);

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
