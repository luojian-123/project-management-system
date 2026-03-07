import Sortable from 'sortablejs'

/**
 * 使容器内的直接子卡片（.el-card）可上下拖拽排序。
 * 用法：在包裹卡片的 div 上使用 v-draggable-cards，拖拽卡片头部即可排序。
 */
export function setupDraggableCards(app) {
  app.directive('draggable-cards', {
    mounted(el) {
      if (!el || !el.querySelector) return
      el._sortable = Sortable.create(el, {
        handle: '.el-card__header, .plasmic-card__header',
        animation: 200,
        ghostClass: 'card-drag-ghost',
        dragClass: 'card-drag-drag',
        onEnd: () => {}
      })
    },
    unmounted(el) {
      if (el._sortable) {
        el._sortable.destroy()
        el._sortable = null
      }
    }
  })
}
