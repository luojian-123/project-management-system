<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ========== 基础与设计变量（方正字体 + 炫彩布局） ========== */
* { margin: 0; padding: 0; box-sizing: border-box; }
html {
  font-size: 16px;
  font-family: var(--font-sans);
}
html, body, #app { height: 100%; }
body {
  font-family: var(--font-sans);
  font-weight: 400;
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  letter-spacing: 0.02em;
  line-height: 1.5;
}

:root {
  /* 方正字体优先，符合页面炫彩布局 */
  --font-sans: "方正兰亭黑", "方正黑体", "FZLanTingHei-R-GBK", "思源黑体", "Source Han Sans SC", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
  --font-mono: "方正书宋", "Consolas", "Monaco", monospace;
  /* 中性色（Plasmic 常用 token） */
  --neutral-50: #fafafa;
  --neutral-100: #f5f5f5;
  --neutral-200: #e5e5e5;
  --neutral-300: #d4d4d4;
  --neutral-400: #a3a3a3;
  --neutral-500: #737373;
  --neutral-600: #525252;
  --neutral-700: #404040;
  --neutral-800: #262626;
  --neutral-900: #171717;
  /* 主题色（保留品牌，略收敛） */
  --tech-cyan: #0ea5e9;
  --tech-purple: #7c3aed;
  --tech-violet: #8b5cf6;
  --primary-start: #0ea5e9;
  --primary-mid: #0284c7;
  --primary-end: #7c3aed;
  /* 文字 */
  --text-primary: #171717;
  --text-secondary: #525252;
  --text-placeholder: #737373;
  /* 边框与背景 */
  --border-light: #e5e5e5;
  --border-card: #e5e5e5;
  --bg-header: #fafafa;
  --bg-main: #fafafa;
  /* 圆角与间距（8px 栅格，Plasmic 常用） */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-btn: 4px;
  --space-page: 24px;
  --space-card: 24px;
  --space-header: 16px 24px;
  --gap-section: 24px;
  --gap-block: 12px;
  --gap-inline: 8px;
  /* 阴影（柔和、少高光，Plasmic 风格） */
  --shadow-card: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-card-hover: 0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04);
  --shadow-btn: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-btn-hover: 0 2px 4px rgba(0,0,0,0.08);
  --shadow-btn-active: 0 0 0 1px rgba(0,0,0,0.06) inset;
  --shadow-dropdown: 0 4px 12px rgba(0,0,0,0.1), 0 2px 4px rgba(0,0,0,0.06);
  --shadow-input-focus: 0 0 0 2px rgba(14, 165, 233, 0.35);
}

/* 全局统一方正字体，符合炫彩布局 */
#app,
#app .el-card, #app .el-card__header, #app .el-card__body,
#app .el-button, #app .el-input, #app .el-select, #app .el-form-item, #app .el-form-item__label,
#app .el-table, #app .el-table th, #app .el-table td,
#app .el-dialog, #app .el-dialog__header, #app .el-dialog__body, #app .el-dialog__footer,
#app .el-pagination, #app .el-dropdown, #app .el-menu,
#app .el-tabs, #app .el-tag, #app .el-message, #app .el-message-box {
  font-family: var(--font-sans) !important;
}

/* 表格操作列防穿模：按钮不换行、不重叠 */
.table-actions-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  flex-wrap: nowrap;
}
.table-actions-cell .el-button {
  flex-shrink: 0;
}

/* 抽屉页四边一点点圆角（全局生效，抽屉可能挂载到 body） */
#app .org-lib-drawer.el-drawer,
.org-lib-drawer.el-drawer {
  border-radius: 8px !important;
  overflow: hidden;
}

/* ========== 内容区与卡片（统一页面宽度与组件距离） ========== */
.page {
  max-width: 100%;
  margin: 0;
  padding: 0;
}
.page > .el-row { margin-bottom: 0; }
.page > .el-row + .el-row,
.page > .el-row + .el-card,
.page > .el-card + .el-row { margin-top: var(--gap-section); }
.el-card {
  position: relative;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-card) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04) !important;
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.2s ease;
}
.el-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04) !important;
}
.el-card::before {
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
  z-index: 10;
  pointer-events: none;
  animation: el-card-top-shine 5s linear infinite;
}
@keyframes el-card-top-shine {
  0% { background-position: 0% 0; }
  100% { background-position: 300% 0; }
}
.el-card__header {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  flex-wrap: wrap;
  gap: var(--gap-inline);
  padding: var(--space-header) !important;
  font-weight: 500;
  font-size: 0.9375rem;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light) !important;
  background: var(--bg-header) !important;
  position: relative;
  z-index: 1;
}
.el-card__body { padding: var(--space-card) !important; }
.page .filter { margin-bottom: var(--gap-section); }
/* 同一页面多卡片/多区块时统一间距 */
.page .el-card + .el-card,
.page .el-row + .el-card,
.page .el-card + .el-row { margin-top: var(--gap-section); }
.page .el-descriptions + .el-button,
.page .el-descriptions + div { margin-top: var(--gap-block); }

/* 卡片头部副标题（规整描述文案） */
.card-header-desc,
.header-desc {
  font-size: 0.8125rem;
  font-weight: 400;
  color: var(--text-secondary);
  margin-left: 10px;
}

/* ========== 表格（统一表头、斑马纹、边框） ========== */
.el-table {
  font-size: 0.875rem;
  --el-table-border-color: var(--border-light) !important;
  --el-table-header-bg-color: rgba(248,250,252,0.95) !important;
  --el-table-row-hover-bg-color: rgba(0,212,255,0.04) !important;
  --el-table-tr-bg-color: #fff !important;
  --el-table-header-text-color: var(--text-primary) !important;
}
.el-table th.el-table__cell {
  font-weight: 500;
  font-size: 0.8125rem;
  color: var(--text-primary);
}
.el-table .el-table__inner-wrapper::before { display: none; }
.el-table--striped .el-table__body tr.el-table__row--striped td { background: rgba(241,245,249,0.6) !important; }
.el-table td.el-table__cell, .el-table th.el-table__cell { padding: 12px 0 !important; }

/* ========== 分页（与按钮风格统一） ========== */
.el-pagination {
  margin-top: var(--gap-section) !important;
  font-size: 0.875rem;
}
.el-pagination .el-pager li,
.el-pagination button {
  border-radius: var(--radius-btn) !important;
  min-width: 26px;
  height: 26px !important;
  font-size: 0.8125rem !important;
  font-weight: 500;
  color: var(--text-primary);
}
.el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end)) !important;
  color: #fff !important;
  border: none;
}

/* ========== 按钮（更小、立体感，与布局严格对齐） ========== */
.el-button {
  border-radius: var(--radius-btn) !important;
  font-weight: 500;
  font-size: 0.75rem !important;
  letter-spacing: 0.02em;
  text-shadow: none !important;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  min-height: 26px !important;
  height: 26px !important;
  padding: 0 11px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  vertical-align: middle !important;
}
.el-button--small {
  min-height: 22px !important;
  height: 22px !important;
  padding: 0 8px !important;
  font-size: 0.6875rem !important;
}
.el-button.is-link { min-height: auto !important; height: auto !important; padding: 2px 6px !important; text-shadow: none !important; }
/* 卡片头部 / 弹窗底部按钮组：严格右对齐、等间距 */
.el-card__header .el-button,
.el-dialog__footer .el-button {
  flex-shrink: 0;
}
.el-card__header > *:last-child {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-end !important;
  gap: var(--gap-inline) !important;
  flex-wrap: wrap !important;
}
/* 卡片内按钮与全局一致，圆角一点点 */
#app .el-card .el-button,
#app .plasmic-card .el-button {
  border-radius: var(--radius-btn) !important;
}
.el-button--primary:not(.is-link) {
  background: linear-gradient(90deg, var(--primary-start) 0%, var(--primary-mid) 42%, var(--primary-end) 100%) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: none !important;
}
.el-button--primary:not(.is-link):hover {
  color: #fff !important;
  box-shadow: none !important;
}
.el-button--primary:not(.is-link):active {
  transform: translateY(2px);
  box-shadow: var(--shadow-btn-active) !important;
}
.el-button--default {
  box-shadow: 0 1px 0 rgba(0,0,0,0.06) !important;
}
.el-button--default:hover {
  box-shadow: 0 1px 0 rgba(0,0,0,0.08) !important;
}
.el-button--default:active {
  transform: translateY(1px);
  box-shadow: 0 0 0 1px rgba(0,0,0,0.06) inset !important;
}
.el-button--danger {
  box-shadow: 0 1px 0 rgba(0,0,0,0.08) !important;
}
.el-button--danger:active {
  transform: translateY(1px);
  box-shadow: 0 0 0 1px rgba(0,0,0,0.06) inset !important;
}
.el-button.is-link { box-shadow: none; }

/* ========== 输入框、选择器（统一圆角与聚焦态） ========== */
.el-input__wrapper,
.el-textarea__inner {
  border-radius: var(--radius-md) !important;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05), 0 0 0 1px rgba(0,0,0,0.06) !important;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}
.el-input__wrapper:hover,
.el-textarea__inner:hover {
  box-shadow: 0 1px 2px rgba(0,0,0,0.06), 0 0 0 1px var(--border-card) !important;
}
.el-input__wrapper.is-focus,
.el-textarea__inner:focus {
  box-shadow: var(--shadow-input-focus) !important;
}
.el-select .el-input.is-focus .el-input__wrapper { box-shadow: var(--shadow-input-focus) !important; }

/* ========== 弹窗（统一圆角、标题、底部） ========== */
.el-dialog {
  border-radius: var(--radius-lg) !important;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0,0,0,0.12), 0 0 0 1px rgba(0,212,255,0.1) !important;
}
.el-dialog__header {
  padding: var(--space-header) !important;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-header);
  font-weight: 500;
  font-size: 0.9375rem;
  color: var(--text-primary);
}
.el-dialog__body {
  padding: var(--space-card) !important;
  color: var(--text-primary);
}
.el-dialog__footer {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-end !important;
  gap: var(--gap-inline) !important;
  flex-wrap: wrap !important;
  padding: var(--gap-block) var(--space-card) !important;
  border-top: 1px solid var(--border-light);
  background: rgba(248,250,252,0.6);
}

/* ========== 表单（标签与间距规整） ========== */
.el-form-item__label {
  color: var(--text-primary) !important;
  font-weight: 500;
  font-size: 0.8125rem;
}
.el-form-item { margin-bottom: var(--gap-block) !important; }
.el-form-item:last-child { margin-bottom: 0 !important; }

/* ========== 描述列表、空状态、下拉菜单 ========== */
.el-descriptions__label {
  font-weight: 500;
  color: var(--text-primary);
  background: rgba(248,250,252,0.8) !important;
}
.el-descriptions__content { color: var(--text-primary); font-weight: 400; }
/* 去掉 el-empty 默认的空盒子插图，只保留文字说明 */
.el-empty__image { display: none !important; }
.el-empty__description { color: #262626; font-size: 0.8125rem; font-weight: 400; }

/* ========== 下拉菜单（与按钮一致：更小、立体、方正字体） ========== */
.el-dropdown-menu,
.el-select-dropdown {
  border-radius: var(--radius-sm) !important;
  box-shadow: var(--shadow-dropdown) !important;
  padding: 4px 0 !important;
  border: 1px solid var(--border-light) !important;
}
.el-dropdown-menu__item,
.el-select-dropdown__item {
  font-size: 0.8125rem !important;
  font-weight: 500 !important;
  letter-spacing: 0.02em;
  color: var(--text-primary) !important;
  padding: 6px 12px !important;
  min-height: 28px !important;
  line-height: 1.4 !important;
}
.el-dropdown-menu__item:hover,
.el-select-dropdown__item.hover {
  background: rgba(0,212,255,0.08) !important;
}
.el-popper.is-light {
  border-radius: var(--radius-sm) !important;
  box-shadow: var(--shadow-dropdown) !important;
  border: 1px solid var(--border-light) !important;
}
.el-popper.is-light .el-popper__arrow::before {
  border: 1px solid var(--border-light);
  background: #fff;
}

/* ========== 表格内「操作」下拉按钮：加粗、浅黑、适配布局 ========== */
.el-table .el-dropdown .el-button.el-button--primary.is-link {
  font-weight: 600 !important;
  color: #262626 !important;
  padding: 4px 8px !important;
  min-height: auto !important;
}
.el-table .el-dropdown .el-button.el-button--primary.is-link:hover {
  color: var(--text-primary) !important;
}
.el-table .el-dropdown .el-button.el-button--primary.is-link .el-icon--right {
  margin-left: 2px;
  color: inherit;
}

/* ========== 链接与可点击名称 ========== */
.link-name {
  color: var(--primary-start);
  cursor: pointer;
  font-weight: 400;
  transition: color 0.2s ease;
}
.link-name:hover { color: var(--tech-purple); text-decoration: underline; }
a.link-name { text-decoration: none; }
a.link-name:hover { text-decoration: underline; }
</style>
