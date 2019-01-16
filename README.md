# ramd
distributed ram

### 介绍

    业界常用的SOA、RPC方案架构下，服务端节点之间割裂，互相没有感知、没有共享；
    一些三方框架，通过数据库传递数据，比如定时更新update_time作为心跳。
    是否有其他优雅的方式，将一个服务集群有机融合?
    
    ramd，distributed ram，希望建立一种机制，将节点的状态快速地同步给集群。
    这里的状态，可以是缓存、定时任务和内存运算的中间结果等。
    内网环境下，网络延迟5ms以内，TPS n万级，所以百万数据量以下，基本不存在性能问题。
    ramd保证，网络通畅情况下，只有一个节点写目标数据，并可以从任意节点读取该数据。
    
### 例子
    
  [putIfAbsent & get](/src/test/java/eastwind/ramd/test/TestRamd.java)
