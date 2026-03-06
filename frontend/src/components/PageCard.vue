<template>
  <!-- 单根节点，便于与 dnd-kit useDraggable 的 ref 绑定，保持与拖拽组件一致 -->
  <div
    class="page-card"
    :class="{ 'page-card--draggable': draggable }"
    data-dnd-draggable-card
  >
    <div class="page-card__inner">
      <div v-if="$slots.header" class="page-card__header">
        <div v-if="draggable" class="page-card__handle" data-dnd-drag-handle aria-label="拖拽">
          <slot name="handle">
            <span class="page-card__handle-icon">⋮⋮</span>
          </slot>
        </div>
        <div class="page-card__header-left">
          <slot name="header" />
        </div>
        <div class="page-card__header-right">
          <slot name="actions" />
        </div>
      </div>
      <div class="page-card__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  /** 是否为可拖拽卡片（展示 handle，并预留 dnd-kit 绑定） */
  draggable: { type: Boolean, default: false }
})
</script>

<style scoped>
.page-card {
  position: relative;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  background: #fff;
}
.page-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--tech-cyan), var(--tech-purple), var(--tech-violet));
  opacity: 0.88;
  z-index: 0;
}
.page-card__inner {
  position: relative;
  z-index: 1;
}
.page-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: var(--space-header);
  font-weight: 600;
  font-size: 1rem;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-header);
}
.page-card__handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  cursor: grab;
  color: rgba(0, 212, 255, 0.7);
  user-select: none;
}
.page-card__handle:active {
  cursor: grabbing;
}
.page-card__handle-icon {
  font-size: 1rem;
  letter-spacing: -2px;
}
.page-card__header-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-card__header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-card__body {
  padding: var(--space-card);
}
</style>
