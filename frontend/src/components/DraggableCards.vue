<template>
  <Draggable
    v-model="innerList"
    :handle="handleSelector"
    item-key="id"
    tag="div"
    class="draggable-cards"
    :animation="200"
    ghost-class="card-ghost"
    @end="onEnd"
  >
    <template #item="{ element }">
      <div class="draggable-cards__item">
        <slot name="card" :id="element.id" :index="element._index" />
      </div>
    </template>
  </Draggable>
</template>

<script setup>
import { computed, watch } from 'vue'
import { VueDraggableNext as Draggable } from 'vue-draggable-next'

const props = defineProps({
  /** 卡片 id 数组，顺序即展示顺序，如 ['info', 'tasks', 'gantt'] */
  list: { type: Array, default: () => [] },
  /** 拖拽手柄选择器，卡片头部需加 class="card-drag-handle" */
  handleSelector: { type: String, default: '.card-drag-handle' }
})

const emit = defineEmits(['update:list', 'drag-end'])

const innerList = computed({
  get: () => props.list.map((id, i) => ({ id: typeof id === 'string' ? id : (id?.id ?? id), _index: i })),
  set: (val) => emit('update:list', (val || []).map((x) => (x && typeof x === 'object' && 'id' in x ? x.id : x)))
})

function onEnd() {
  emit('drag-end', innerList.value.map((x) => x.id))
}
</script>

<style scoped>
.draggable-cards {
  display: flex;
  flex-direction: column;
  gap: var(--gap-section);
}
.draggable-cards__item {
  min-height: 0;
}
:deep(.card-ghost) {
  opacity: 0.5;
}
</style>
