<template>
  <li>
    <div class="tree-node" @click="expanded = !expanded">
      <span class="tree-toggle">{{ expanded ? '▼' : '▶' }}</span>
      {{ node.name }}
    </div>
    <ul v-if="expanded && node.children && node.children.length" class="tree-children">
      <OrganizationTreeNode v-for="child in node.children" :key="child.id" :node="child" />
    </ul>
  </li>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  node: { type: Object, required: true }
})

const expanded = ref(true)
</script>
