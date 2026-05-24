<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

const label = computed(() => (props.modelValue ? '深色模式' : '浅色模式'));

function handleChange(value) {
  emit('update:modelValue', value);
}
</script>

<template>
  <div class="theme-switch">
    <span>{{ label }}</span>
    <el-switch
      :model-value="modelValue"
      inline-prompt
      active-text="Dark"
      inactive-text="Light"
      @change="handleChange"
    />
  </div>
</template>
