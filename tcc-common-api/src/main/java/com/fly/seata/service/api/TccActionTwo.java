package com.fly.seata.service.api;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
public interface TccActionTwo {

  /**
   * Prepare boolean.
   *
   * @param actionContext the action context
   * @param b             the b
   * @return the boolean
   */
  @TwoPhaseBusinessAction(name = "TccActionTwo" , commitMethod = "commit", rollbackMethod = "rollback")
  public boolean prepare(BusinessActionContext actionContext, String b);

  /**
   * Commit boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  public boolean commit(BusinessActionContext actionContext);

  /**
   * Rollback boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  public boolean rollback(BusinessActionContext actionContext);

}
