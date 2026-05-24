<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import ThemeSwitch from '../components/ThemeSwitch.vue';
import { fetchFaultLogs } from '../api/faultLab';

const darkMode = ref(true);
const loading = ref(false);
const logs = ref([]);
const filters = reactive({
  faultType: ''
});

function applyTheme() {
  document.documentElement.setAttribute('data-theme', darkMode.value ? 'dark' : 'light');
}

async function loadLogs() {
  loading.value = true;
  try {
    const response = await fetchFaultLogs({
      faultType: filters.faultType || undefined
    });
    logs.value = response.data;
  } catch (error) {
    ElMessage.error(error.message || '加载历史记录失败');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  applyTheme();
  await loadLogs();
});

watch(darkMode, () => {
  applyTheme();
});
</script>

<template>
  <div class="history-shell">
    <header class="history-header">
      <div>
        <span class="topbar__eyebrow">History</span>
        <h1>故障历史记录</h1>
      </div>
      <div class="history-header__actions">
        <ThemeSwitch v-model="darkMode" />
        <el-button @click="$router.push('/ops')">返回控制台</el-button>
      </div>
    </header>

    <section class="panel">
      <div class="filter-row">
        <el-input v-model="filters.faultType" placeholder="按故障类型过滤，例如 heap-oom" clearable />
        <el-button type="primary" :loading="loading" @click="loadLogs">查询</el-button>
      </div>

      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="faultType" label="故障类型" width="200" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="triggerTime" label="触发时间" width="180" />
        <el-table-column prop="recoveryTime" label="恢复时间" width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="JVM 快照">
          <template #default="{ row }">
            <pre class="history-json">{{ row.jvmMetrics }}</pre>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>
