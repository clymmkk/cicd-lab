package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 连接池耗尽通过持有慢查询连接模拟连接泄漏和池满现象。
 */
@Slf4j
@Component
public class DbPoolExhaustionFaultScenario extends AbstractFaultScenario {

    private final DataSource dataSource;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<Connection> heldConnections = new CopyOnWriteArrayList<>();
    private final List<Thread> workerThreads = new CopyOnWriteArrayList<>();

    public DbPoolExhaustionFaultScenario(JvmMetricsUtil jvmMetricsUtil, DataSource dataSource) {
        super(
            "db-pool-exhaustion",
            "数据库连接池耗尽",
            "高级",
            "通过多个慢查询占住连接并暂不释放，模拟连接池打满。",
            "需要数据库可用，触发后其他数据库请求可能明显变慢或超时。",
            true,
            true,
            jvmMetricsUtil
        );
        this.dataSource = dataSource;
    }

    @Override
    public synchronized void trigger(FaultParam param) {
        if (running.get()) {
            return;
        }
        running.set(true);
        int connections = Math.max(3, param.getIntValue("connections", 5));
        int sleepSeconds = Math.max(5, param.getIntValue("sleepSeconds", 10));
        for (int index = 0; index < connections; index++) {
            Thread worker = new Thread(() -> holdConnectionWithSlowQuery(sleepSeconds), "fault-db-pool-" + index);
            workerThreads.add(worker);
            worker.start();
        }
        waitForWorkersReady(500L);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("heldConnections", heldConnections.size());
        metrics.putAll(jvmMetricsUtil.getDbPoolSnapshot());
        markRunning("已持有 " + heldConnections.size() + " 个数据库连接", snapshotWith(metrics));
    }

    private void holdConnectionWithSlowQuery(int sleepSeconds) {
        try {
            Connection connection = dataSource.getConnection();
            heldConnections.add(connection);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT SLEEP(?)");
            preparedStatement.setInt(1, sleepSeconds);
            preparedStatement.execute();
            while (running.get()) {
                TimeUnit.SECONDS.sleep(1L);
            }
            preparedStatement.close();
        } catch (Exception exception) {
            log.warn("DB pool exhaustion trigger error", exception);
        }
    }

    private void waitForWorkersReady(long waitMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(waitMillis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        for (Thread workerThread : workerThreads) {
            workerThread.interrupt();
        }
        for (Connection connection : heldConnections) {
            try {
                connection.close();
            } catch (Exception exception) {
                log.debug("Ignore connection close failure", exception);
            }
        }
        heldConnections.clear();
        workerThreads.clear();
        try {
            TimeUnit.MILLISECONDS.sleep(300L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("heldConnections", heldConnections.size());
        metrics.putAll(jvmMetricsUtil.getDbPoolSnapshot());
        markStopped("已主动关闭持有连接", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - HikariCP 连接池容量有限。
            - 慢查询或连接泄漏会长时间占用连接。
            - 活跃连接达到上限后，新请求只能等待或超时失败。

            ```mermaid
            graph LR
                A[Incoming SQL] --> B[Hikari Pool max=5]
                B --> C[Slow Query]
                C --> D[Connection Held]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jstack <pid>
            jcmd <pid> Thread.print
            mysql -uroot -p -e "show processlist;"
            arthas dashboard
            ```
            """;
    }
}
