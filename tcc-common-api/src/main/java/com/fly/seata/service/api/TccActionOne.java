package com.fly.seata.service.api;

import io.seata.rm.tcc.api.BusinessActionContext;
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
   * @param a             the a
   * @return the boolean
   */
  @TwoPhaseBusinessAction(name = "TccActionOne" , commitMethod = "commit", rollbackMethod = "rollback")
  public boolean prepare(BusinessActionContext actionContext, int a);

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
