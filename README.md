## TCC简介
在2PC（两阶段提交）协议中，事务管理器分两阶段协调资源管理，资源管理器对外提供了3个操作，分别是一阶段的准备操作，二阶段的提交操作和回滚操作；

TCC服务作为一种事务资源，遵循两阶段提交协议，由业务层面自定义，需要用户根据业务逻辑编码实现；其包含Try、Confirm 和 Cancel 3个操作，其中Try操作对应分布式事务一阶段的准备，Confirm操作对应分布式事务二阶段提交，Cancel对应分布式事务二阶段回滚：

- Try：资源的检查和预留；

- Comfirm：使用预留的资源，完成真正的业务操作；要求Try成功Confirm 一定要能成功；

- Cancel：释放预留资源；

TCC的3个方法均由用户根据业务场景编码实现，并对外发布成微服务，供事务管理器调用；事务管理器在一阶段调用TCC的Try方法，在二阶段提交时调用Confirm方法，在二阶段回滚时调用Cancel方法。

## seata tcc实现
TCC服务由用户编码实现并对外发布成微服务，目前支持3种形式的TCC微服务，分别是：
- SofaRpc服务-蚂蚁开源：用户将实现的TCC操作对外发布成 SofaRpc 服务，事务管理器通过订阅SofaRpc服务，来协调TCC资源；
- Dubbo服务：将TCC发布成dubbo服务，事务管理器订阅dubbo服务，来协调TCC资源；
- Local TCC：本地普通的TCC Bean，非远程服务；事务管理器通过本地方法调用，来协调TCC 资源；

目前荐于我司使用的微服务是spring cloud组件，微服务调用的rpc是feign(http协议)，故tcc选取用**local tcc模式**即可！！！

## springCloud-Feign seata local tcc接入指南

### 1. 引入jar包
```xml
<!--seata组件包-->
<dependency>
  <groupId>io.seata</groupId>
  <artifactId>seata-all</artifactId>
  <version>${seata.version}</version>
</dependency>

<!--spring cloud 相关定制-->
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-alibaba-seata</artifactId>
  <version>x.y.z</version>
</dependency>
```
注意：[seata兼容版本说明](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

### 2. seata 注册中心配置
registry.conf配置文件，euraka中的application是指seata的服务端的服务器，这边要注意seata server有事物分组的概念，用于不同业务方的集群分区。
```
registry {
  # 注册中心支持file 、nacos 、eureka、redis、zk,推荐eureka做负载均衡
  type = "eureka"

  eureka {
    serviceUrl = "http://192.168.202.137:8761/eureka"
    # seata server注册中心的服务名
    application = "seata-server-default-group"
    weight = "1"
  }
}

config {
  # 配置中心支持file、nacos 、apollo、zk,推荐apollo
  type = "file"

  file {
    name = "file.conf"
  }
}
```

### 3. seata 配置中心配置
file.conf配置文件，这里需要注意service中的vgroup_mapping配置，其中vgroup_mapping.my_test_tx_group的my_test_tx_group是表示逻辑服务分组，值表示seata server的实际服务分组，一定要存在seata serve的分组名
```
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}

service {
  #vgroup->rgroup
  vgroup_mapping.my_test_tx_group = "seata-server-default-group"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
  disableGlobalTransaction = false
}

client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
  report.retry.count = 5
  tm.commit.retry.count = 1
  tm.rollback.retry.count = 1
}

transaction {
  undo.data.validation = true
  undo.log.serialization = "jackson"
  undo.log.save.days = 7
  #schedule delete expired undo_log in milliseconds
  undo.log.delete.period = 86400000
  undo.log.table = "undo_log"
}

support {
  ## spring
  spring {
    # auto proxy the DataSource bean
    datasource.autoproxy = false
  }
}
```

### 4. RM 配置服务分组名
application.yml配置文件
```yaml
spring:
  cloud:
    alibaba:
      seata:
        ## 该服务分组名一定要和file.conf配置文件中的service.vgroup_mapping一致，不然找不到对应的seata server集群名
        tx-service-group: my_test_tx_group
```

### 5. RM 配置local tcc
注解@LocalTCC 和 @TwoPhaseBusinessAction 一定要配置在接口中，不能写在实现类!!!
```java
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
```

### 6. TM 全局事物配置
```java
@RestController
public class TestController {

  @Autowired
  private RmOneApi rmOneApi; // rm tcc接口

  @Autowired
  private RmTwoApi rmTwoApi; // rm tcc接口

  // 全局事物开启
  @GlobalTransactional
  @GetMapping("/tm/test")
  public String test(){
    String result = rmOneApi.rmOnetest();
    System.out.println("result:"+result);
    result = rmTwoApi.rmTwotest();
    System.out.println("result:"+result);
    return "ok";
  }
}
```

7. 观察日志

TM端启动日志发现有tcc注册成功的日志，即可说明配置成功！
```
2019-11-13 14:41:00.230  INFO 34664 --- [imeoutChecker_1] i.s.c.r.netty.NettyClientChannelManager  : will connect to 192.168.202.149:8091
2019-11-13 14:41:00.231  INFO 34664 --- [imeoutChecker_1] i.s.core.rpc.netty.NettyPoolableFactory  : NettyPool create channel to transactionRole:TMROLE,address:192.168.202.149:8091,msg:< RegisterTMRequest{applicationId='tcc-rm-one', transactionServiceGroup='my_test_tx_group'} >
2019-11-13 14:41:00.243  INFO 34664 --- [imeoutChecker_1] i.s.core.rpc.netty.NettyPoolableFactory  : register success, cost 9 ms, version:0.9.0,role:TMROLE,channel:[id: 0x17bbaf41, L:/192.168.202.149:50519 - R:/192.168.202.149:8091]
```

测试Demo成功日志如下

TM端日志：
```
2019-11-13 14:48:16.621  INFO 34796 --- [nio-8082-exec-7] i.seata.tm.api.DefaultGlobalTransaction  : Begin new global transaction [192.168.202.149:8091:2027355158]
2019-11-13 14:48:16.800  INFO 34796 --- [nio-8082-exec-7] i.seata.tm.api.DefaultGlobalTransaction  : [192.168.202.149:8091:2027355158] commit status:Committed
```

RM端日志：
```
TccActionOne prepare, xid:192.168.202.149:8091:2027355166
2019-11-13 14:49:21.446  INFO 34790 --- [atch_RMROLE_4_8] i.s.core.rpc.netty.RmMessageListener     : onMessage:xid=192.168.202.149:8091:2027355166,branchId=2027355171,branchType=TCC,resourceId=TccActionTwo,applicationData={"actionContext":{"sys::rollback":"rollback","sys::commit":"commit","action-start-time":1573627761160,"host-name":"192.168.202.149","sys::prepare":"prepare","actionName":"TccActionTwo"}}
2019-11-13 14:49:21.446  INFO 34790 --- [atch_RMROLE_4_8] io.seata.rm.AbstractRMHandler            : Branch committing: 192.168.202.149:8091:2027355166 2027355171 TccActionTwo {"actionContext":{"sys::rollback":"rollback","sys::commit":"commit","action-start-time":1573627761160,"host-name":"192.168.202.149","sys::prepare":"prepare","actionName":"TccActionTwo"}}
TccActionOne commit, xid:192.168.202.149:8091:2027355166
2019-11-13 14:49:21.446  INFO 34790 --- [atch_RMROLE_4_8] io.seata.rm.AbstractResourceManager      : TCC resource commit result :true, xid:192.168.202.149:8091:2027355166, branchId:2027355171, resourceId:TccActionTwo
2019-11-13 14:49:21.446  INFO 34790 --- [atch_RMROLE_4_8] io.seata.rm.AbstractRMHandler            : Branch commit result: PhaseTwo_Committed
```

TC端日志：
```
2019-11-13 14:49:53.138 INFO [batchLoggerPrint_1]io.seata.core.rpc.DefaultServerMessageListenerImpl.run:198 -SeataMergeMessage timeout=60000,transactionName=test()
,clientIp:192.168.202.149,vgroup:my_test_tx_group
2019-11-13 14:49:53.145 INFO [ServerHandlerThread_33_500]io.seata.server.coordinator.DefaultCore.begin:145 -Successfully begin global transaction xid = 192.168.202.149:8091:2027355175
2019-11-13 14:49:53.157 INFO [batchLoggerPrint_1]io.seata.core.rpc.DefaultServerMessageListenerImpl.run:198 -SeataMergeMessage xid=192.168.202.149:8091:2027355175,branchType=TCC,resourceId=TccActionOne,lockKey=null
,clientIp:192.168.202.149,vgroup:my_test_tx_group
2019-11-13 14:49:53.205 INFO [ServerHandlerThread_34_500]io.seata.server.coordinator.DefaultCore.lambda$branchRegister$0:94 -Successfully register branch xid = 192.168.202.149:8091:2027355175, branchId = 2027355178
2019-11-13 14:49:53.226 INFO [batchLoggerPrint_1]io.seata.core.rpc.DefaultServerMessageListenerImpl.run:198 -SeataMergeMessage xid=192.168.202.149:8091:2027355175,branchType=TCC,resourceId=TccActionTwo,lockKey=null
,clientIp:192.168.202.149,vgroup:my_test_tx_group
2019-11-13 14:49:53.252 INFO [ServerHandlerThread_35_500]io.seata.server.coordinator.DefaultCore.lambda$branchRegister$0:94 -Successfully register branch xid = 192.168.202.149:8091:2027355175, branchId = 2027355180
2019-11-13 14:49:53.259 INFO [batchLoggerPrint_1]io.seata.core.rpc.DefaultServerMessageListenerImpl.run:198 -SeataMergeMessage xid=192.168.202.149:8091:2027355175,extraData=null
,clientIp:192.168.202.149,vgroup:my_test_tx_group
2019-11-13 14:49:53.376 INFO [ServerHandlerThread_36_500]io.seata.server.coordinator.DefaultCore.doGlobalCommit:303 -Global[192.168.202.149:8091:2027355175] committing is successfully done.
```