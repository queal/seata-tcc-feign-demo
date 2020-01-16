package com.fly.seata.service;

import io.seata.rm.tcc.TwoPhaseResult;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * @author: peijiepang
 * @date 2020-01-14
 * @Description:
 */
@LocalTCC
public interface StorageIncreaseService {

    /**
     * 新增库存信息
     * @param actionContext
     * @return
     */
    @TwoPhaseBusinessAction(name = "storageIncrease",commitMethod = "storageIncreaseCommit",rollbackMethod = "storageIncreaseRollback")
    void storageIncreasePrepare(BusinessActionContext actionContext);

    TwoPhaseResult storageIncreaseCommit(BusinessActionContext actionContext);

    TwoPhaseResult storageIncreaseRollback(BusinessActionContext actionContext);


}
