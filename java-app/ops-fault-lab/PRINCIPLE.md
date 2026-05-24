# PRINCIPLE

## JVM 内存模型

```mermaid
graph TD
    A[JVM Runtime] --> B[Heap]
    A --> C[Thread Stack]
    A --> D[Metaspace]
    A --> E[PC Register]
    A --> F[Native Method Stack]
```

## 堆内存溢出

```mermaid
graph TD
    A[GC Roots] --> B[Static List]
    B --> C[1MB Object]
    B --> D[1MB Object]
    B --> E[1MB Object]
```

- 静态集合持有对象会形成强可达链
- 堆无法回收足够对象时会抛出 OOM

## 栈内存溢出

```mermaid
graph TD
    A[Thread] --> B[Frame 1]
    B --> C[Frame 2]
    C --> D[Frame 3]
    D --> E[...]
```

- 每次递归都会消耗新的栈帧
- `-Xss` 越小，触发栈溢出越快

## CPU 飙高

- 忙循环线程长期处于运行态
- CPU 时间片会持续分配给可运行线程

## 死锁

```mermaid
graph LR
    A[Thread A] -->|waits| B[Lock 2]
    B -->|owned by| C[Thread B]
    C -->|waits| D[Lock 1]
    D -->|owned by| A
```

## 线程池耗尽

- 固定线程池大小决定并发执行上限
- 队列堆满后触发拒绝策略

## Full GC

## GC 算法对比

| 算法 | 特点 | 适用场景 |
| --- | --- | --- |
| G1 | 分区回收，兼顾吞吐与停顿 | 通用服务 |
| ZGC | 低停顿，支持大堆 | 超大内存、低延迟 |
| Parallel GC | 吞吐优先 | 批处理、离线任务 |
