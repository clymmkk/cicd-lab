<script setup>
import * as echarts from 'echarts';
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { Cpu, Histogram, List, Monitor } from '@element-plus/icons-vue';
import FaultCard from '../components/FaultCard.vue';
import MetricPanel from '../components/MetricPanel.vue';
import ThemeSwitch from '../components/ThemeSwitch.vue';
import { fetchDashboard, fetchOperationLogs, fetchScenarios } from '../api/faultLab';

const loading = ref(false);
const scenarios = ref([]);
const dashboard = reactive({
  heapUsedMb: 0,
  heapMaxMb: 0,
  nonHeapUsedMb: 0,
  cpuLoadPercent: 0,
  threadCount: 0,
  peakThreadCount: 0,
  youngGcCount: 0,
  fullGcCount: 0,
  activeDbConnections: 0,
  idleDbConnections: 0,
  totalDbConnections: 0,
  redisLatencyMs: 0,
  activeScenario: '',
  notices: []
});
const operationLogs = ref([]);
const activeMenu = ref('ops');
const darkMode = ref(true);
const chartRef = ref(null);
let chartInstance;
let timerId;
const historySeries = reactive({
  time: [],
  heap: [],
  cpu: [],
  threads: []
});

const groupedScenarios = computed(() => {
  return scenarios.value.reduce((accumulator, item) => {
    if (!accumulator[item.level]) {
      accumulator[item.level] = [];
    }
    accumulator[item.level].push(item);
    return accumulator;
  }, {});
});

function applyTheme() {
  document.documentElement.setAttribute('data-theme', darkMode.value ? 'dark' : 'light');
}

function trimSeries(series, value) {
  if (series.length >= 12) {
    series.shift();
  }
  series.push(value);
}

function renderChart() {
  if (!chartRef.value) {
    return;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value, null, { renderer: 'canvas' });
  }
  chartInstance.setOption({
    backgroundColor: 'transparent',
    textStyle: {
      color: darkMode.value ? '#d7dde7' : '#233243'
    },
    grid: {
      top: 30,
      left: 30,
      right: 30,
      bottom: 30
    },
    legend: {
      textStyle: {
        color: darkMode.value ? '#d7dde7' : '#233243'
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: historySeries.time,
      axisLine: {
        lineStyle: {
          color: darkMode.value ? '#55606f' : '#c5ced8'
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: 'Heap/Thread',
        axisLine: { show: false },
        splitLine: {
          lineStyle: {
            color: darkMode.value ? 'rgba(85,96,111,0.35)' : 'rgba(197,206,216,0.5)'
          }
        }
      },
      {
        type: 'value',
        name: 'CPU%',
        min: 0,
        max: 100,
        axisLine: { show: false },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: 'Heap MB',
        type: 'line',
        smooth: true,
        data: historySeries.heap,
        lineStyle: { width: 3, color: '#d66c44' },
        areaStyle: { color: 'rgba(214,108,68,0.18)' }
      },
      {
        name: 'CPU%',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: historySeries.cpu,
        lineStyle: { width: 2, color: '#3f7cac' }
      },
      {
        name: 'Threads',
        type: 'line',
        smooth: true,
        data: historySeries.threads,
        lineStyle: { width: 2, color: '#7d9d68' }
      }
    ]
  });
}

async function refreshData() {
  loading.value = true;
  try {
    const [scenarioResponse, dashboardResponse, operationLogResponse] = await Promise.all([
      fetchScenarios(),
      fetchDashboard(),
      fetchOperationLogs()
    ]);
    scenarios.value = scenarioResponse.data;
    Object.assign(dashboard, dashboardResponse.data);
    operationLogs.value = operationLogResponse.data;

    const timeLabel = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    trimSeries(historySeries.time, timeLabel);
    trimSeries(historySeries.heap, dashboard.heapUsedMb);
    trimSeries(historySeries.cpu, dashboard.cpuLoadPercent);
    trimSeries(historySeries.threads, dashboard.threadCount);
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

function startPolling() {
  timerId = window.setInterval(() => {
    refreshData();
  }, 5000);
}

onMounted(async () => {
  applyTheme();
  await refreshData();
  startPolling();
});

onBeforeUnmount(() => {
  if (timerId) {
    window.clearInterval(timerId);
  }
  if (chartInstance) {
    chartInstance.dispose();
  }
});

watch(darkMode, () => {
  applyTheme();
  renderChart();
});
</script>

<template>
  <div class="shell">
    <aside class="shell__sidebar">
      <div class="brand">
        <span class="brand__eyebrow">Ops Console</span>
        <h1>故障演练平台</h1>
        <p>面向运维排障训练的前后端分离实验台</p>
      </div>

      <el-menu :default-active="activeMenu" class="shell__menu">
        <el-menu-item index="ops">
          <el-icon><Monitor /></el-icon>
          <span>故障控制台</span>
        </el-menu-item>
        <el-menu-item index="history" @click="$router.push('/history')">
          <el-icon><List /></el-icon>
          <span>历史记录</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-section">
        <div class="sidebar-section__title">故障分级</div>
        <div v-for="(items, level) in groupedScenarios" :key="level" class="scenario-list">
          <div class="scenario-list__level">{{ level }}</div>
          <div v-for="item in items" :key="item.code" class="scenario-list__item">
            <span>{{ item.name }}</span>
            <em>{{ item.status.state }}</em>
          </div>
        </div>
      </div>

      <ThemeSwitch v-model="darkMode" />
    </aside>

    <main class="shell__main">
      <header class="topbar">
        <div>
          <span class="topbar__eyebrow">Runtime Overview</span>
          <h2>演练控制台</h2>
        </div>
        <div class="topbar__badge">
          当前场景
          <strong>{{ dashboard.activeScenario || '无' }}</strong>
        </div>
      </header>

      <section class="metrics-grid">
        <MetricPanel title="Heap" :value="`${dashboard.heapUsedMb} / ${dashboard.heapMaxMb} MB`" hint="堆内存使用" accent="warn" />
        <MetricPanel title="CPU" :value="`${dashboard.cpuLoadPercent}%`" hint="进程 CPU 使用率" accent="info" />
        <MetricPanel title="Threads" :value="dashboard.threadCount" :hint="`峰值 ${dashboard.peakThreadCount}`" accent="success" />
        <MetricPanel
          title="DB Pool"
          :value="`${dashboard.activeDbConnections}/${dashboard.totalDbConnections}`"
          :hint="`空闲 ${dashboard.idleDbConnections}`"
          accent="neutral"
        />
      </section>

      <section class="board-grid">
        <div class="panel chart-panel">
          <div class="panel__head">
            <div>
              <span class="panel__eyebrow">实时走势</span>
              <h3>Heap / CPU / Thread</h3>
            </div>
            <el-icon><Histogram /></el-icon>
          </div>
          <div ref="chartRef" class="chart-panel__canvas"></div>
        </div>

        <div class="panel">
          <div class="panel__head">
            <div>
              <span class="panel__eyebrow">JVM 摘要</span>
              <h3>排障提示</h3>
            </div>
            <el-icon><Cpu /></el-icon>
          </div>
          <ul class="notice-list">
            <li v-for="notice in dashboard.notices" :key="notice">{{ notice }}</li>
            <li>Young GC: {{ dashboard.youngGcCount }}</li>
            <li>Full GC: {{ dashboard.fullGcCount }}</li>
            <li>Redis Latency: {{ dashboard.redisLatencyMs }} ms</li>
          </ul>
        </div>
      </section>

      <section class="fault-grid">
        <FaultCard
          v-for="scenario in scenarios"
          :key="scenario.code"
          :scenario="scenario"
          :loading="loading"
          @refresh="refreshData"
        />
      </section>

      <section class="panel">
        <div class="panel__head">
          <div>
            <span class="panel__eyebrow">审计</span>
            <h3>最近操作记录</h3>
          </div>
        </div>
        <el-table :data="operationLogs" stripe>
          <el-table-column prop="user" label="用户" width="120" />
          <el-table-column prop="action" label="动作" />
          <el-table-column prop="ip" label="来源 IP" width="160" />
          <el-table-column prop="createTime" label="时间" width="180" />
        </el-table>
      </section>
    </main>
  </div>
</template>
