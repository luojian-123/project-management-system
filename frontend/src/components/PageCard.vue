<template>
  <div
    class="plasmic-card"
    :class="{ 'plasmic-card--draggable': draggable }"
    data-dnd-draggable-card
  >
    <div class="plasmic-card__inner">
      <header v-if="$slots.header || $slots.actions" class="plasmic-card__header">
        <div v-if="draggable" class="plasmic-card__handle" data-dnd-drag-handle aria-label="拖拽排序">
          <slot name="handle">
            <span class="plasmic-card__grip" aria-hidden="true">
              <span class="plasmic-card__grip-dot" /><span class="plasmic-card__grip-dot" /><span class="plasmic-card__grip-dot" />
              <span class="plasmic-card__grip-dot" /><span class="plasmic-card__grip-dot" /><span class="plasmic-card__grip-dot" />
            </span>
          </slot>
        </div>
        <div class="plasmic-card__header-left">
          <slot name="header" />
        </div>
        <div class="plasmic-card__header-right">
          <slot name="actions" />
        </div>
      </header>
      <div class="plasmic-card__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  /** 是否为可拖拽卡片（展示手柄，配合 DraggableCardList 或 handle 选择器使用） */
  draggable: { type: Boolean, default: false }
})
</script>

<style scoped>
.plasmic-card {
  position: relative;
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.35s ease, transform 0.2s ease;
  font-family: var(--font-sans);
}
.plasmic-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  background: linear-gradient(90deg, #06b6d4, #0ea5e9, #8b5cf6, #a855f7, #ec4899, #f59e0b, #06b6d4);
  background-size: 300% 100%;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.4), 0 2px 8px rgba(139, 92, 246, 0.3);
  z-index: 2;
  pointer-events: none;
  animation: plasmic-card-top-shine 5s linear infinite;
}
.plasmic-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
}
.plasmic-card--draggable .plasmic-card__header {
  padding-left: 12px;
}
@keyframes plasmic-card-top-shine {
  0% { background-position: 0% 0; }
  100% { background-position: 300% 0; }
}
.plasmic-card__inner {
  position: relative;
  z-index: 1;
}
.plasmic-card__header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--gap-inline);
  padding: var(--space-header);
  font-weight: 600;
  font-size: 0.9375rem;
  letter-spacing: -0.011em;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-header);
}
.plasmic-card__handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 4px;
  padding: 4px;
  cursor: grab;
  color: var(--neutral-400);
  user-select: none;
  border-radius: var(--radius-sm);
  transition: color 0.15s ease, background 0.15s ease;
}
.plasmic-card__handle:hover {
  color: var(--neutral-600);
  background: var(--neutral-100);
}
.plasmic-card__handle:active {
  cursor: grabbing;
}
.plasmic-card__grip {
  display: grid;
  grid-template-columns: repeat(3, 4px);
  grid-template-rows: repeat(2, 4px);
  gap: 2px;
  width: 14px;
  height: 10px;
}
.plasmic-card__grip-dot {
  width: 4px;
  height: 4px;
  border-radius: 1px;
  background: currentColor;
}
.plasmic-card__header-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--gap-inline);
}
.plasmic-card__header-right {
  display: flex;
  align-items: center;
  gap: var(--gap-inline);
}
.plasmic-card__body {
  padding: var(--space-card);
  font-size: 0.875rem;
  letter-spacing: -0.011em;
  color: var(--text-primary);
}
</style>
