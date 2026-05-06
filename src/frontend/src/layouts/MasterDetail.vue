<template>
  <div class="md-layout">
    <div class="md-sidebar">
      <slot name="sidebar-top"></slot>
      <ul class="md-list">
        <slot name="sidebar-list"></slot>
      </ul>
      <div class="md-sidebar-actions">
        <slot name="sidebar-actions"></slot>
      </div>
    </div>
    <div class="md-main">
      <div class="md-header">
        <slot name="header"></slot>
      </div>
      <div class="md-content">
        <slot name="content"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.md-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: var(--space-3);
  margin-top: var(--space-3);
}

.md-sidebar {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius-1);
  padding: var(--space-3);
  min-height: 400px;
}

:deep(h3) {
  margin: 0 0 var(--space-2);
  font-size: 16px;
}

:deep(.md-list) {
  list-style: none;
  margin: 0 0 var(--space-2);
  padding: 0;
}

:deep(.md-list li) {
  padding: var(--space-2);
  border-radius: 6px;
  margin-bottom: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-2);
  transition: background 0.15s ease;
  cursor: pointer;
  font-size: 14px;
}

:deep(.md-list li:hover) {
  background: #fff3e6;
}

:deep(.md-list li.active) {
  background: var(--accent);
  color: white;
  cursor: pointer;
}

:deep(.md-list li.active:hover) {
  opacity: 0.9;
}

:deep(.md-sidebar-actions) {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

:deep(.md-sidebar-actions button) {
  width: 100%;
  max-width: none;
}

:deep(.md-main) {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius-1);
  padding: var(--space-3);
}

:deep(.md-header) {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

:deep(.md-header h3) {
  margin: 0;
  font-size: 16px;
}

:deep(.md-header > button) {
  margin-left: auto;
}

:deep(.md-sidebar-top) {
  display: flex;
  flex-direction: column;
}
</style>
