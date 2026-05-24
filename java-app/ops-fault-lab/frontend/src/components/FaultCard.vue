<script setup>
import { computed, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import CommandBlock from './CommandBlock.vue';
import { stopScenario, triggerScenario } from '../api/faultLab';

const props = defineProps({
  scenario: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['refresh']);

const form = reactive({
  operator: 'ops_user',
  durationSeconds: undefined,
  intensity: undefined
});

const extraFields = computed(() => {
  if (props.scenario.code === 'heap-oom') {
    return [{ key: 'blocks', label: '分配块数', value: 32 }];
  }
  if (props.scenario.code === 'thread-pool-exhaustion') {
    return [
      { key: 'tasks', label: '任务数', value: 20 },
      { key: 'sleepSeconds', label: '任务耗时(s)', value: 30 }
    ];
  }
  if (props.scenario.code === 'full-gc') {
    return [{ key: 'retainLimit', label: '保留对象数', value: 8 }];
  }
  if (props.scenario.code === 'db-pool-exhaustion') {
    return [
      { key: 'connections', label: '连接数', value: 5 },
      { key: 'sleepSeconds', label: '慢查询耗时(s)', value: 10 }
    ];
  }
  if (props.scenario.code === 'redis-latency') {
    return [{ key: 'sleepSeconds', label: '阻塞秒数', value: 5 }];
  }
  return [];
});

const extras = reactive({});

watch(
  extraFields,
  (fields) => {
    fields.forEach((field) => {
      if (extras[field.key] === undefined) {
        extras[field.key] = field.value;
      }
    });
  },
  { immediate: true }
);

const statusTag = computed(() => {
  if (props.scenario.status.state === 'running') {
    return 'danger';
  }
  if (props.scenario.status.state === 'crashed') {
    return 'warning';
  }
  return 'info';
});

async function handleTrigger() {
  try {
    await triggerScenario(props.scenario.code, {
      ...form,
      extras: { ...extras }
    });
    ElMessage.success('故障场景已触发');
    emit('refresh');
  } catch (error) {
    ElMessage.error(error.message || '触发失败');
  }
}

async function handleStop() {
  try {
    await stopScenario(props.scenario.code, {
      operator: form.operator
    });
    ElMessage.success('停止命令已下发');
    emit('refresh');
  } catch (error) {
    ElMessage.error(error.message || '停止失败');
  }
}
</script>

<template>
  <article class="fault-card">
    <header class="fault-card__header">
      <div>
        <div class="fault-card__title">
          <h2>{{ scenario.name }}</h2>
          <el-tag size="small" effect="plain">{{ scenario.level }}</el-tag>
        </div>
        <p>{{ scenario.description }}</p>
      </div>
      <el-tag :type="statusTag">{{ scenario.status.state }}</el-tag>
    </header>

    <div class="fault-card__warning" v-if="scenario.warning">
      {{ scenario.warning }}
    </div>

    <el-form label-position="top" class="fault-card__form">
      <el-form-item label="操作人">
        <el-input v-model="form.operator" />
      </el-form-item>
      <el-form-item
        v-for="field in extraFields"
        :key="field.key"
        :label="field.label"
      >
        <el-input-number v-model="extras[field.key]" :min="1" />
      </el-form-item>
    </el-form>

    <div class="fault-card__actions">
      <el-button
        type="danger"
        size="large"
        :loading="loading"
        :disabled="scenario.status.running || !scenario.enabled"
        @click="handleTrigger"
      >
        触发故障
      </el-button>
      <el-button
        size="large"
        :loading="loading"
        :disabled="!scenario.status.running || !scenario.stoppable"
        @click="handleStop"
      >
        停止故障
      </el-button>
      <span class="fault-card__status">{{ scenario.status.message }}</span>
    </div>

    <section class="fault-card__metrics">
      <div
        v-for="(value, key) in scenario.status.metrics"
        :key="key"
        class="fault-card__metric"
      >
        <span>{{ key }}</span>
        <strong>{{ value }}</strong>
      </div>
    </section>

    <el-collapse>
      <el-collapse-item title="原理与图解" name="principle">
        <pre class="fault-card__markdown">{{ scenario.principle }}</pre>
      </el-collapse-item>
    </el-collapse>

    <CommandBlock :content="scenario.commands" />
  </article>
</template>
