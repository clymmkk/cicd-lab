<script setup>
import { ElMessage } from 'element-plus';

const props = defineProps({
  content: {
    type: String,
    required: true
  }
});

async function copyContent() {
  await navigator.clipboard.writeText(props.content);
  ElMessage.success('排查命令已复制');
}
</script>

<template>
  <section class="command-block">
    <div class="command-block__head">
      <h3>排查命令</h3>
      <el-button type="primary" plain size="small" @click="copyContent">
        复制命令
      </el-button>
    </div>
    <pre>{{ content }}</pre>
  </section>
</template>
