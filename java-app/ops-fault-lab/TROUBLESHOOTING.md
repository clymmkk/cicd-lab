# TROUBLESHOOTING

## 通用命令

```bash
jps -l
jstat -gcutil <pid> 1000
jstack <pid>
jmap -histo <pid> | head -n 30
jcmd <pid> VM.flags
```

## OOM

```bash
jmap -histo:live <pid> | head -n 50
jcmd <pid> GC.heap_dump heap.hprof
```

- 用 Eclipse MAT 分析大对象和引用链
- 重点关注静态集合、缓存对象、线程本地变量

## CPU

```bash
top -Hp <pid>
printf "%x\n" <tid>
jstack <pid> | grep -A 30 <nid>
```

## GC

```bash
jstat -gcutil <pid> 1000
jcmd <pid> GC.heap_info
```

- 可用 GCeasy 分析 GC 日志
- 重点关注老年代占用、对象晋升速率、Full GC 频次

## 死锁

```bash
jstack <pid>
jcmd <pid> Thread.print
```

- 搜索 `Found one Java-level deadlock`
- 对照线程名与锁对象地址

## 数据库连接池

```bash
mysql -uroot -p -e "show processlist;"
jstack <pid>
```

- 关注慢 SQL、连接泄漏、Hikari 超时日志

## Redis

```bash
redis-cli slowlog get 10
redis-cli info commandstats
redis-cli latency latest
```

## Arthas

```bash
dashboard
thread -n 5
watch com.faultlab..* '*' -x 2
trace com.faultlab.controller.FaultScenarioController triggerScenario
```
