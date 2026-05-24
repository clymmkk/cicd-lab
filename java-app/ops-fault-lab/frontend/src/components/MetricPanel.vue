<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  hint: {
    type: String,
    default: ''
  },
  accent: {
    type: String,
    default: 'neutral'
  }
});
</script>

<template>
  <section class="metric-panel" :data-accent="accent">
    <div class="metric-panel__title">{{ title }}</div>
    <div class="metric-panel__value">{{ value }}</div>
    <div class="metric-panel__hint">{{ hint }}</div>
  </section>
</template>
