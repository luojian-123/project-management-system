import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { guest: true } },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { auth: true },
    redirect: '/todo',
    children: [
      { path: 'todo', name: 'Todo', component: () => import('@/views/todo/TodoList.vue') },
      { path: 'project', name: 'Project', component: () => import('@/views/project/ProjectList.vue') },
      { path: 'project/:id', name: 'ProjectDetail', component: () => import('@/views/project/ProjectDetail.vue') },
      { path: 'task/:id', name: 'TaskDetail', component: () => import('@/views/task/TaskDetail.vue') },
      { path: 'cost', name: 'Cost', component: () => import('@/views/cost/CostList.vue') },
      { path: 'issue', name: 'Issue', component: () => import('@/views/issue/IssueList.vue') },
      { path: 'change', name: 'Change', component: () => import('@/views/change/ChangeList.vue') },
      { path: 'risk', name: 'Risk', component: () => import('@/views/risk/RiskList.vue') },
      {
        path: 'org',
        name: 'Org',
        component: () =>
          import('@/views/org/OrgManage.vue').catch((err) => {
            console.error('[router] OrgManage load failed', err)
            return {
              template: '<div class="page" style="padding:24px;text-align:center"><p style="margin-bottom:12px">组织管理加载失败，请刷新重试</p><el-button type="primary" @click="reload">刷新页面</el-button></div>',
              setup() {
                const reload = () => window.location.reload()
                return { reload }
              }
            }
          })
      },
      { path: 'model/:formCode', name: 'DynamicModel', component: () => import('@/views/model/DynamicModelPage.vue'), meta: { titleFromConfig: true } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) return next('/login')
  if (to.meta.guest && token) return next('/')
  next()
})

export default router
