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

## TCC 服务设计的实践经验
### 一、问题
- 空回滚：Try未执行，Canal执行了
  - 出现原因：
    1. try网络超时(丢包)
    2. 分布式事物回滚，触发Canal
    3. 未收到try，直接收到Canal
  - 解决方案：
    1. 关键就是要识别出这个空回滚。思路很简单就是需要知道一阶段是否执行，如果执行了，那就是正常回滚；如果没执行，那就是空回滚。因此，需要一张额外的事务控制表，其中有分布式事务 ID 和分支事务 ID，第一阶段 Try 方法里会插入一条记录，表示一阶段执行了。Cancel 接口里读取该记录，如果该记录存在，则正常回滚；如果该记录不存在，则是空回滚。
    
- 幂等：对于同一个分布式事务的同一个分支事务，重复去调用该分支事务的第二阶段接口，因此，要求 TCC 的二阶段 Confirm 和 Cancel 接口保证幂等，不会重复使用或者释放资源。如果幂等控制没有做好，很有可能导致资损等严重问题。
  - 出现原因：
    1. 提交或回滚是一次 TC 到参与者的网络调用，网络故障、参与者宕机等都有可能造成参与者 TCC 资源实际执行了二阶段防范，但是 TC 没有收到返回结果的情况，这时，TC 就会重复调用，直至调用成功，整个分布式事务结束。
  - 解决方案：
    1. 一个简单的思路就是记录每个分支事务的执行状态。在执行前状态，如果已执行，那就不再执行；否则，正常执行。前面在讲空回滚的时候，已经有一张事务控制表了，事务控制表的每条记录关联一个分支事务，那我们完全可以在这张事务控制表上加一个状态字段，用来记录每个分支事务的执行状态。
    
- 防悬挂：悬挂就是对于一个分布式事务，其二阶段 Cancel 接口比 Try 接口先执行。因为允许空回滚的原因，Cancel 接口认为 Try 接口没执行，空回滚直接返回成功，对于 Seata 框架来说，认为分布式事务的二阶段接口已经执行成功，整个分布式事务就结束了。但是这之后 Try 方法才真正开始执行，预留业务资源，前面提到事务并发控制的业务加锁，对于一个 Try 方法预留的业务资源，只有该分布式事务才能使用，然而 Seata 框架认为该分布式事务已经结束，也就是说，当出现这种情况时，该分布式事务第一阶段预留的业务资源就再也没有人能够处理了，对于这种情况，我们就称为悬挂，即业务资源预留后没法继续处理。
  - 出现的原因：
    1. 在 RPC 调用时，先注册分支事务，再执行 RPC 调用，如果此时 RPC 调用的网络发生拥堵，通常 RPC 调用是有超时时间的，RPC 超时以后，发起方就会通知 TC 回滚该分布式事务，可能回滚完成后，RPC 请求才到达参与者，真正执行，从而造成悬挂。
   - 解决方案：
    1. 根据悬挂出现的条件先来分析下，悬挂是指二阶段 Cancel 执行完后，一阶段才执行。也就是说，为了避免悬挂，如果二阶段执行完成，那一阶段就不能再继续执行。因此，当一阶段执行时，需要先检查二阶段是否已经执行完成，如果已经执行，则一阶段不再执行；否则可以正常执行。那怎么检查二阶段是否已经执行呢？大家是否想到了刚才解决空回滚和幂等时用到的事务控制表，可以在二阶段执行时插入一条事务控制记录，状态为已回滚，这样当一阶段执行时，先读取该记录，如果记录存在，就认为二阶段已经执行；否则二阶段没执行。
    
### 二、异常控制实现
在分析完空回滚、幂等、悬挂等异常 Case 的成因以及解决方案以后，下面我们就综合起来考虑，一个 TCC 接口如何完整的解决这三个问题。 

1. 首先是 Try 方法。结合前面讲到空回滚和悬挂异常，Try 方法主要需要考虑两个问题，一个是 Try 方法需要能够告诉二阶段接口，已经预留业务资源成功。第二个是需要检查第二阶段是否已经执行完成，如果已完成，则不再执行。
2. 接下来是 Confirm 方法。因为 Confirm 方法不允许空回滚，也就是说，Confirm 方法一定要在 Try 方法之后执行。因此，Confirm 方法只需要关注重复提交的问题。可以先锁定事务记录，如果事务记录为空，则说明是一个空提交，不允许，终止执行。如果事务记录不为空，则继续检查状态是否为初始化，如果是，则说明一阶段正确执行，那二阶段正常执行即可。如果状态是已提交，则认为是重复提交，直接返回成功即可；如果状态是已回滚，也是一个异常，一个已回滚的事务，不能重新提交，需要能够拦截到这种异常情况，并报警。
3. 最后是 Cancel 方法。因为 Cancel 方法允许空回滚，并且要在先执行的情况下，让 Try 方法感知到 Cancel 已经执行，所以和 Confirm 方法略有不同。首先依然是锁定事务记录。如果事务记录为空，则认为 Try 方法还没执行，即是空回滚。空回滚的情况下，应该先插入一条事务记录，确保后续的 Try 方法不会再执行。如果插入成功，则说明 Try 方法还没有执行，空回滚继续执行。如果插入失败，则认为Try 方法正再执行，等待 TC 的重试即可。如果一开始读取事务记录不为空，则说明 Try 方法已经执行完毕，再检查状态是否为初始化，如果是，则还没有执行过其他二阶段方法，正常执行 Cancel 逻辑。如果状态为已回滚，则说明这是重复调用，允许幂等，直接返回成功即可。如果状态为已提交，则同样是一个异常，一个已提交的事务，不能再次回滚。
