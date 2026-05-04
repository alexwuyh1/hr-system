<template>
  <div class="modal-overlay active" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h3>{{ title }}</h3>
        <button class="modal-close" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body">
        <slot></slot>
      </div>
      <div class="modal-footer">
        <button class="ghost" @click="$emit('close')">取消</button>
        <button @click="$emit('submit')">{{ submitText || '保存' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  submitText: String
})

defineEmits(['submit', 'close'])
</script>
