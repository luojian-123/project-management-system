<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
html { font-size: 16px; }
html, body, #app { height: 100%; }
body {
  font-family: "Noto Sans SC", "Source Han Sans SC", "思源黑体", sans-serif;
  color: #1e293b;
  -webkit-font-smoothing: antialiased;
  letter-spacing: 0.02em;
}
/* 科技风主题色：青-紫 */
:root {
  --tech-cyan: #00d4ff;
  --tech-purple: #7c3aed;
  --tech-violet: #a855f7;
}
/* 内容页通用：卡片带炫彩描边与光晕 */
.page { max-width: 1400px; margin: 0 auto; }
.page .el-card {
  position: relative;
  border-radius: 14px;
  border: 1px solid rgba(0,212,255,0.15);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06), 0 0 0 1px rgba(255,255,255,0.8) inset, 0 0 24px rgba(0,212,255,0.04);
  overflow: hidden;
}
.page .el-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--tech-cyan), var(--tech-purple), var(--tech-violet));
  opacity: 0.85;
}
.page .el-card :deep(.el-card__header) {
  padding: 18px 22px;
  font-weight: 600;
  font-size: 1rem;
  color: #1e293b;
  border-bottom: 1px solid rgba(0,212,255,0.08);
  background: linear-gradient(180deg, rgba(248,250,252,0.9) 0%, rgba(241,245,249,0.5) 100%);
}
.page .el-card :deep(.el-card__body) { padding: 22px; }
.page .filter { margin-bottom: 16px; }
.page .el-table { font-size: 0.875rem; }
.page .el-pagination { margin-top: 16px; }
/* 主按钮科技风高亮 */
.page .el-button--primary {
  background: linear-gradient(135deg, #00b4d8 0%, #7c3aed 100%) !important;
  border: none !important;
  box-shadow: 0 2px 12px rgba(0,212,255,0.25);
}
.page .el-button--primary:hover { box-shadow: 0 4px 20px rgba(0,212,255,0.35), 0 2px 12px rgba(124,58,237,0.2); }
/* 列表名称/标题可点击跳转详情 */
.link-name { color: #00b4d8; cursor: pointer; font-weight: 500; }
.link-name:hover { text-decoration: underline; }
</style>
