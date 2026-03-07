<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ========== 基础与设计变量（统一字体与排版） ========== */
* { margin: 0; padding: 0; box-sizing: border-box; }
html {
  font-size: 16px;
  font-family: var(--font-sans);
  -webkit-text-size-adjust: 100%;
}
html, body, #app { height: 100%; }
body {
  font-family: var(--font-sans);
  font-weight: var(--font-weight-normal);
  font-size: var(--text-base);
  line-height: var(--leading-normal);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  letter-spacing: var(--tracking-normal);
}

:root {
  /* 字体栈：Noto Sans SC 优先，兼顾中文与数字 */
  --font-sans: "Noto Sans SC", "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", sans-serif;
  --font-mono: "Consolas", "Monaco", "Menlo", monospace;
  /* 字重 */
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;
  /* 字号与行高（类型比例约 1.25） */
  --text-xs: 0.75rem;      /* 12px */
  --text-sm: 0.8125rem;    /* 13px */
  --text-base: 1rem;       /* 16px */
  --text-md: 0.9375rem;    /* 15px */
  --text-lg: 1.125rem;    /* 18px */
  --text-xl: 1.25rem;     /* 20px */
  --leading-tight: 1.35;
  --leading-normal: 1.55;
  --leading-relaxed: 1.65;
  --tracking-tight: -0.01em;
  --tracking-normal: 0.02em;
  /* 中性色 */
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
  /* 主题色 */
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
  --border-card: #e8e8e8;
  --bg-header: #fafafa;
  --bg-main: #f5f6f8;
  /* 圆角与间距（8px 栅格） */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-btn: 6px;
  --space-page: 24px;
  --space-card: 24px;
  --space-header: 16px 24px;
  --gap-section: 24px;
  --gap-block: 12px;
  --gap-inline: 8px;
  /* 阴影 */
  --shadow-card: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-card-hover: 0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04);
  --shadow-btn: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-btn-hover: 0 2px 4px rgba(0,0,0,0.08);
  --shadow-btn-active: 0 0 0 1px rgba(0,0,0,0.06) inset;
  --shadow-dropdown: 0 4px 12px rgba(0,0,0,0.1), 0 2px 4px rgba(0,0,0,0.06);
  --shadow-input-focus: 0 0 0 2px rgba(14, 165, 233, 0.35);
}

/* 全局统一字体与排版 */
#app,
#app .el-card, #app .el-card__header, #app .el-card__body,
#app .el-button, #app .el-input, #app .el-input__inner, #app .el-select, #app .el-form-item, #app .el-form-item__label,
#app .el-table, #app .el-table th, #app .el-table td,
#app .el-dialog, #app .el-dialog__header, #app .el-dialog__body, #app .el-dialog__footer,
#app .el-pagination, #app .el-dropdown, #app .el-menu, #app .el-menu-item,
#app .el-tabs, #app .el-tabs__item, #app .el-tag, #app .el-message, #app .el-message-box,
#app .el-alert, #app .el-drawer__title {
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
  font-weight: var(--font-weight-medium);
  font-size: var(--text-md);
  line-height: var(--leading-normal);
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light) !important;
  background: var(--bg-header) !important;
  position: relative;
  z-index: 1;
}
.el-card__body {
  padding: var(--space-card) !important;
  font-size: var(--text-base);
  line-height: var(--leading-normal);
}
.page .filter { margin-bottom: var(--gap-section); }
/* 同一页面多卡片/多区块时统一间距 */
.page .el-card + .el-card,
.page .el-row + .el-card,
.page .el-card + .el-row { margin-top: var(--gap-section); }
.page .el-descriptions + .el-button,
.page .el-descriptions + div { margin-top: var(--gap-block); }

/* 卡片头部副标题 */
.card-header-desc,
.header-desc {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-normal);
  color: var(--text-secondary);
  margin-left: 10px;
  line-height: var(--leading-normal);
}

/* ========== 表格（统一表头、斑马纹、边框，表头与列表值字号一致） ========== */
.el-table {
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
  --el-table-border-color: var(--border-light) !important;
  --el-table-header-bg-color: rgba(248,250,252,0.95) !important;
  --el-table-row-hover-bg-color: rgba(0,212,255,0.04) !important;
  --el-table-tr-bg-color: #fff !important;
  --el-table-header-text-color: var(--text-primary) !important;
}
.el-table th.el-table__cell {
  font-weight: var(--font-weight-medium);
  font-size: var(--text-sm);
  color: var(--text-primary);
  letter-spacing: var(--tracking-normal);
  white-space: nowrap;
}
.el-table td.el-table__cell {
  font-size: var(--text-sm);
}
.el-table .el-table__inner-wrapper::before { display: none; }
.el-table--striped .el-table__body tr.el-table__row--striped td { background: rgba(241,245,249,0.6) !important; }
.el-table td.el-table__cell, .el-table th.el-table__cell { padding: 12px 16px !important; }

/* ========== 分页（与按钮风格统一） ========== */
.el-pagination {
  margin-top: var(--gap-section) !important;
  font-size: var(--text-sm);
}
.el-pagination .el-pager li,
.el-pagination button {
  border-radius: var(--radius-btn) !important;
  min-width: 28px;
  height: 28px !important;
  font-size: var(--text-sm) !important;
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}
.el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end)) !important;
  color: #fff !important;
  border: none;
}

/* ========== 按钮（统一圆角与字号） ========== */
.el-button {
  border-radius: var(--radius-btn) !important;
  font-weight: var(--font-weight-medium);
  font-size: var(--text-sm) !important;
  letter-spacing: var(--tracking-normal);
  line-height: var(--leading-normal);
  text-shadow: none !important;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  min-height: 32px !important;
  height: 32px !important;
  padding: 0 14px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  vertical-align: middle !important;
}
.el-button--small {
  min-height: 28px !important;
  height: 28px !important;
  padding: 0 10px !important;
  font-size: var(--text-xs) !important;
}
.el-button--large {
  min-height: 36px !important;
  height: 36px !important;
  padding: 0 18px !important;
  font-size: var(--text-base) !important;
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
  font-weight: var(--font-weight-medium);
  font-size: var(--text-md);
  line-height: var(--leading-normal);
  color: var(--text-primary);
}
.el-dialog__body {
  padding: var(--space-card) !important;
  font-size: var(--text-base);
  line-height: var(--leading-normal);
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
  font-weight: var(--font-weight-medium);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
}
.el-form-item { margin-bottom: var(--gap-block) !important; }
.el-form-item:last-child { margin-bottom: 0 !important; }
.el-input__inner, .el-textarea__inner { font-size: var(--text-base); }

/* ========== 描述列表、空状态、下拉菜单 ========== */
.el-descriptions__label {
  font-weight: var(--font-weight-medium);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: rgba(248,250,252,0.8) !important;
}
.el-descriptions__content { color: var(--text-primary); font-weight: var(--font-weight-normal); font-size: var(--text-base); }
/* 空状态 */
.el-empty__image { display: none !important; }
.el-empty__description { color: var(--neutral-800); font-size: var(--text-sm); font-weight: var(--font-weight-normal); line-height: var(--leading-normal); }

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
  font-size: var(--text-sm) !important;
  font-weight: var(--font-weight-medium) !important;
  letter-spacing: var(--tracking-normal);
  color: var(--text-primary) !important;
  padding: 8px 14px !important;
  min-height: 32px !important;
  line-height: var(--leading-normal) !important;
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

/* ========== 消息与提示 ========== */
.el-message, .el-message-box__title, .el-message-box__content {
  font-family: var(--font-sans) !important;
  font-size: var(--text-sm);
}
.el-alert__title { font-size: var(--text-sm); font-weight: var(--font-weight-medium); }
.el-alert__description { font-size: var(--text-sm); line-height: var(--leading-normal); }

/* ========== 标签与 Tab ========== */
.el-tag {
  font-size: var(--text-xs) !important;
  font-weight: var(--font-weight-medium);
  line-height: 1.4;
  border-radius: var(--radius-sm);
}
.el-tabs__item {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}
.el-tabs__content { font-size: var(--text-base); }

/* ========== 表格内「操作」下拉按钮 ========== */
.el-table .el-dropdown .el-button.el-button--primary.is-link {
  font-weight: var(--font-weight-semibold) !important;
  font-size: var(--text-sm) !important;
  color: var(--neutral-800) !important;
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
