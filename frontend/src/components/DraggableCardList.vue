<template>
  <Draggable
    v-model="innerList"
    :handle="handleSelector"
    :item-key="itemKey"
    tag="div"
    class="draggable-card-list"
    v-bind="draggableOptions"
    @end="onDragEnd"
  >
    <template #item="{ element, index }">
      <slot name="item" :item="element" :index="index" />
    </template>
  </Draggable>
</template>

<script setup>
import { computed } from 'vue'
import { VueDraggableNext as Draggable } from 'vue-draggable-next'

const props = defineProps({
  /** 绑定的列表（与 PageCard 配合时每项需有唯一 id） */
  list: { type: Array, default: () => [] },
  /** 列表项唯一键，用于拖拽稳定 */
  itemKey: { type: String, default: 'id' },
  /** 拖拽手柄选择器，与 PageCard 的 handle 一致 */
  handleSelector: { type: String, default: '.plasmic-card__handle' },
  /** 传给 Sortable 的其它选项 */
  draggableOptions: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:list', 'drag-end'])

const innerList = computed({
  get: () => props.list,
  set: (val) => emit('update:list', val)
})

function onDragEnd() {
  emit('drag-end', innerList.value)
}
</script>

<style scoped>
.draggable-card-list {
  display: flex;
  flex-direction: column;
  gap: var(--gap-section);
}
</style>
