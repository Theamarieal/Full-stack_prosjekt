import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import TemperatureView from '@/views/TemperatureView.vue'
import TemperatureHistoryView from '@/views/TemperatureHistoryView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/temperature',
      name: 'temperature',
      component: TemperatureView,
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/temperature-history',
      name: 'temperature-history',
      component: TemperatureHistoryView,
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('@/views/ReportView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/training',
      name: 'training',
      component: () => import('@/views/TrainingView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/checklists',
      name: 'checklists',
      component: () => import('@/views/ChecklistView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/alcohol',
      name: 'alcohol',
      component: () => import('@/views/AlcoholView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/deviations',
      name: 'deviations',
      component: () => import('@/views/DeviationListView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/deviations/new',
      name: 'deviation-new',
      component: () => import('@/views/DeviationNewView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/manage-checklists',
      name: 'manage-checklists',
      component: () => import('@/views/ManageChecklistsView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['ADMIN'],
      },
    },
    {
      path: '/dashboard',
      redirect: '/',
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const userRole = authStore.user?.role
  const allowedRoles = Array.isArray(to.meta.roles) ? to.meta.roles : null

  if (requiresAuth && !authStore.isLoggedIn) {
    return next('/login')
  }

  if ((to.path === '/login' || to.path === '/register') && authStore.isLoggedIn) {
    return next('/')
  }

  if (allowedRoles && !allowedRoles.includes(userRole)) {
    return next('/')
  }

  return next()
})

export default router
