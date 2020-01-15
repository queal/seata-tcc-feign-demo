package com.fly.seata.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.BusinessActivityContext;
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
  @TwoPhaseBusinessAction(name = "TccActionTwo" , commitMethod = "storageReduceCommit", rollbackMethod = "storageReduceRollback")
  boolean storageReducePrepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "productId")long productId,@BusinessActionContextParameter(paramName = "count")int count);

  /**
   * Commit boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  boolean storageReduceCommit(BusinessActionContext actionContext);

  /**
   * Rollback boolean.
   *
   * @param actionContext the action context
   * @return the boolean
   */
  boolean storageReduceRollback(BusinessActionContext actionContext);

}
