package com.fly.seata.service.impl;

import com.fly.seata.dao.StorageDao;
import com.fly.seata.domain.Storage;
import com.fly.seata.service.StorageIncreaseService;
import io.seata.rm.tcc.TwoPhaseResult;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2020-01-14
 * @Description:
 */
@Service
public class StorageIncreaseServiceImpl implements StorageIncreaseService {

    private final static Logger LOGGER = LoggerFactory.getLogger(StorageIncreaseServiceImpl.class);

    @Autowired
    private StorageDao storageDao;

    @Override
    public void storageIncreasePrepare(BusinessActionContext actionContext) {
        Storage storage = new Storage();
        storage.setProductId(1000L);
        storage.setUsed(0);
        storageDao.insert(storage);
        Long storageId = storage.getId();
    }

    @Override
    public TwoPhaseResult storageIncreaseCommit(BusinessActionContext actionContext) {
        LOGGER.info("storageIncreaseCommit commit, xid:{} activityContext:{}" ,actionContext);
        return new TwoPhaseResult(true,"分支提交成功");
    }

    @Override
    public TwoPhaseResult storageIncreaseRollback(BusinessActionContext actionContext) {
        LOGGER.info("storageIncreaseRollback commit, xid:{} activityContext:{}" ,actionContext);
        return new TwoPhaseResult(true,"分支回滚成功");
    }
}
