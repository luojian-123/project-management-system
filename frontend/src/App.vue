<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ========== 基础与设计变量（统一美观与规整） ========== */
* { margin: 0; padding: 0; box-sizing: border-box; }
html {
  font-size: 16px;
  font-family: "方正兰亭黑", "方正黑体", "思源黑体", "Source Han Sans SC", "Noto Sans SC", sans-serif;
}
html, body, #app { height: 100%; }
body {
  font-family: "方正兰亭黑", "方正黑体", "思源黑体", "Source Han Sans SC", "Noto Sans SC", sans-serif;
  font-weight: 400;
  color: #000;
  -webkit-font-smoothing: antialiased;
  letter-spacing: 0.02em;
  line-height: 1.5;
}

:root {
  /* 主题色 */
  --tech-cyan: #00d4ff;
  --tech-purple: #7c3aed;
  --tech-violet: #a855f7;
  --primary-start: #00b4d8;
  --primary-mid: #0096c7;
  --primary-end: #7c3aed;
  /* 文字（全局黑色，按钮内文字除外） */
  --text-primary: #000000;
  --text-secondary: #262626;
  --text-placeholder: #595959;
  /* 边框与背景 */
  --border-light: rgba(0, 212, 255, 0.12);
  --border-card: rgba(0, 212, 255, 0.15);
  --bg-header: linear-gradient(180deg, rgba(248,250,252,0.98) 0%, rgba(241,245,249,0.6) 100%);
  --bg-main: linear-gradient(160deg, #f0f9ff 0%, #f8fafc 42%, #f1f5f9 100%);
  /* 圆角与间距（8px 栅格） */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 14px;
  --space-page: 24px;
  --space-card: 20px;
  --space-header: 16px 20px;
  --gap-section: 20px;
  --gap-block: 12px;
  --gap-inline: 8px;
  /* 阴影（立体感加强） */
  --shadow-card: 0 2px 12px rgba(0,0,0,0.06), 0 0 0 1px rgba(255,255,255,0.85) inset, 0 0 24px rgba(0,212,255,0.05);
  --shadow-btn: 0 2px 0 rgba(0,0,0,0.08), 0 3px 8px rgba(0,180,216,0.3), 0 1px 0 rgba(255,255,255,0.6) inset;
  --shadow-btn-hover: 0 3px 0 rgba(0,0,0,0.08), 0 5px 14px rgba(0,180,216,0.38), 0 1px 0 rgba(255,255,255,0.7) inset;
  --shadow-btn-active: 0 0 0 rgba(0,0,0,0.1), 0 1px 4px rgba(0,180,216,0.25), 0 -1px 0 rgba(0,0,0,0.06) inset;
  --shadow-dropdown: 0 3px 0 rgba(0,0,0,0.06), 0 6px 20px rgba(0,0,0,0.1), 0 1px 0 rgba(255,255,255,0.9) inset;
  --shadow-input-focus: 0 0 0 2px rgba(0, 212, 255, 0.25);
}

/* ========== 内容区与卡片（统一页面宽度与组件距离） ========== */
.page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--space-page);
}
.page > .el-row { margin-bottom: 0; }
.page > .el-row + .el-row,
.page > .el-row + .el-card,
.page > .el-card + .el-row { margin-top: var(--gap-section); }
.el-card {
  position: relative;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-card) !important;
  box-shadow: var(--shadow-card) !important;
  overflow: hidden;
  background: #fff;
}
.el-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--tech-cyan), var(--tech-purple), var(--tech-violet));
  opacity: 0.88;
  z-index: 0;
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
  color: #000;
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
  --el-table-header-text-color: #000 !important;
}
.el-table th.el-table__cell {
  font-weight: 500;
  font-size: 0.8125rem;
  color: #000;
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
  border-radius: var(--radius-sm) !important;
  min-width: 26px;
  height: 26px !important;
  font-size: 0.8125rem !important;
  font-weight: 500;
  color: #000;
}
.el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end)) !important;
  color: #fff !important;
  border: none;
}

/* ========== 按钮（更小、立体感，与布局严格对齐） ========== */
.el-button {
  border-radius: var(--radius-sm) !important;
  font-weight: 500;
  font-size: 0.75rem !important;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 0 rgba(255,255,255,0.35);
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
.el-button--primary:not(.is-link) {
  background: linear-gradient(90deg, var(--primary-start) 0%, var(--primary-mid) 42%, var(--primary-end) 100%) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: var(--shadow-btn);
}
.el-button--primary:not(.is-link):hover {
  color: #fff !important;
  box-shadow: var(--shadow-btn-hover);
}
.el-button--primary:not(.is-link):active {
  transform: translateY(2px);
  box-shadow: var(--shadow-btn-active) !important;
}
.el-button--default {
  box-shadow: 0 2px 0 rgba(0,0,0,0.06), 0 2px 6px rgba(0,0,0,0.06), 0 1px 0 rgba(255,255,255,0.8) inset !important;
}
.el-button--default:hover {
  box-shadow: 0 3px 0 rgba(0,0,0,0.07), 0 4px 10px rgba(0,0,0,0.08), 0 1px 0 rgba(255,255,255,0.85) inset !important;
}
.el-button--default:active {
  transform: translateY(1px);
  box-shadow: 0 0 0 rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.06), 0 -1px 0 rgba(0,0,0,0.05) inset !important;
}
.el-button--danger {
  box-shadow: 0 2px 0 rgba(0,0,0,0.08), 0 2px 6px rgba(220,38,38,0.2), 0 1px 0 rgba(255,255,255,0.5) inset !important;
}
.el-button--danger:active {
  transform: translateY(1px);
  box-shadow: 0 0 0 rgba(0,0,0,0.06), 0 1px 3px rgba(220,38,38,0.15) inset !important;
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
  color: #000;
}
.el-dialog__body {
  padding: var(--space-card) !important;
  color: #000;
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
  color: #000 !important;
  font-weight: 500;
  font-size: 0.8125rem;
}
.el-form-item { margin-bottom: var(--gap-block) !important; }
.el-form-item:last-child { margin-bottom: 0 !important; }

/* ========== 描述列表、空状态、下拉菜单 ========== */
.el-descriptions__label {
  font-weight: 500;
  color: #000;
  background: rgba(248,250,252,0.8) !important;
}
.el-descriptions__content { color: #000; font-weight: 400; }
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
  color: #000 !important;
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
