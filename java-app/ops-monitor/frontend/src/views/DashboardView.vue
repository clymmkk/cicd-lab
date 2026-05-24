<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-brand">运维监控平台</div>
      <div class="nav-links">
        <router-link to="/dashboard" class="nav-link active">仪表盘</router-link>
        <router-link to="/servers" class="nav-link">服务器</router-link>
        <button class="nav-link btn-logout" @click="logout">退出</button>
      </div>
    </nav>

    <div class="content">
      <h1>监控仪表盘</h1>
      <div class="metrics-grid">
        <div class="metric-card">
          <h3>CPU 使用率</h3>
          <div class="metric-value" :style="{color: cpuColor}">{{ current.cpuUsage }}%</div>
          <div class="metric-chart" ref="cpuChart"></div>
        </div>
        <div class="metric-card">
          <h3>内存使用</h3>
          <div class="metric-value">{{ current.memoryUsed }} / {{ current.memoryTotal }} MB</div>
          <div class="metric-chart" ref="memoryChart"></div>
        </div>
        <div class="metric-card">
          <h3>磁盘使用</h3>
          <div class="metric-value">{{ current.diskUsed }} / {{ current.diskTotal }} MB</div>
          <div class="metric-chart" ref="diskChart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { getCurrentMetrics } from '../api/metric.js'
import * as echarts from 'echarts'

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const current = reactive({
      cpuUsage: 0,
      memoryUsed: 0,
      memoryTotal: 0,
      diskUsed: 0,
      diskTotal: 0
    })
    const cpuColor = ref('#3498db')
    const cpuChart = ref(null)
    const memoryChart = ref(null)
    const diskChart = ref(null)
    let charts = []
    let timer = null

    const logout = () => {
      authStore.logout()
      router.push('/login')
    }

    const initGaugeChart = (el, value, color, title) => {
      if (!el) return null
      const chart = echarts.init(el)
      chart.setOption({
        series: [{
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          itemStyle: { color: color },
          progress: { show: true, width: 20 },
          pointer: { show: false },
          axisLine: { lineStyle: { width: 20 } },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          detail: { show: false },
          data: [{ value: value }]
        }]
      })
      return chart
    }

    const loadMetrics = async () => {
      try {
        const data = await getCurrentMetrics()
        current.cpuUsage = data.cpuUsage || 0
        current.memoryUsed = data.memoryUsed || 0
        current.memoryTotal = data.memoryTotal || 0
        current.diskUsed = data.diskUsed || 0
        current.diskTotal = data.diskTotal || 0

        cpuColor.value = current.cpuUsage > 80 ? '#e74c3c' : current.cpuUsage > 60 ? '#f39c12' : '#3498db'

        charts.forEach(c => c.dispose())
        charts = []
        if (cpuChart.value) charts.push(initGaugeChart(cpuChart.value, current.cpuUsage, cpuColor.value))
        if (memoryChart.value) {
          const memPct = current.memoryTotal > 0 ? (current.memoryUsed / current.memoryTotal * 100).toFixed(1) : 0
          charts.push(initGaugeChart(memoryChart.value, memPct, '#2ecc71'))
        }
        if (diskChart.value) {
          const diskPct = current.diskTotal > 0 ? (current.diskUsed / current.diskTotal * 100).toFixed(1) : 0
          charts.push(initGaugeChart(diskChart.value, diskPct, '#9b59b6'))
        }
      } catch (err) {
        console.error('获取监控数据失败', err)
      }
    }

    onMounted(() => {
      loadMetrics()
      timer = setInterval(loadMetrics, 5000)
    })

    onBeforeUnmount(() => {
      clearInterval(timer)
      charts.forEach(c => c.dispose())
    })

    return { current, cpuColor, cpuChart, memoryChart, diskChart, logout }
  }
}
</script>

<style scoped>
.dashboard { min-height: 100vh; }

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 60px;
  background: #2c3e50;
  color: #fff;
}

.nav-brand { font-size: 20px; font-weight: bold; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  background: none;
  border: none;
  cursor: pointer;
}

.nav-link:hover, .nav-link.active { color: #fff; }

.btn-logout { color: #e74c3c; }

.content { padding: 30px 40px; }

.content h1 { margin-bottom: 24px; font-size: 24px; }

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.metric-card h3 { font-size: 16px; color: #7f8c8d; margin-bottom: 12px; }

.metric-value { font-size: 36px; font-weight: bold; margin-bottom: 16px; }

.metric-chart { height: 160px; }
</style>
