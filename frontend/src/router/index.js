import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import TemperatureView from '@/views/TemperatureView.vue'
import TemperatureHistoryView from '@/views/TemperatureHistoryView.vue'

/**
 * Vue Router configuration for the Checkd application.
 *
 * All protected routes require authentication via the `requiresAuth` meta field.
 * Role-based access control is enforced through the `roles` meta field, which
 * specifies which user roles are permitted to access a given route.
 *
 * Available roles: 'EMPLOYEE', 'MANAGER', 'ADMIN'
 *
 * Route access summary:
 * - Public:             /login, /register
 * - All authenticated:  /, /temperature, /temperature-history, /training, /checklists, /alcohol, /deviations, /deviations/new
 * - Manager + Admin:    /reports, /manage-checklists
 * - Admin only:         /admin
 *
 * A global navigation guard enforces authentication and role checks before
 * each route transition. Unauthenticated users are redirected to /login,
 * and users without the required role are redirected to the dashboard.
 *
 * @module router
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/temperature',
      name: 'temperature',
      component: TemperatureView,
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/temperature-history',
      name: 'temperature-history',
      component: TemperatureHistoryView,
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
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
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] },
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/training',
      name: 'training',
      component: () => import('@/views/TrainingView.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/checklists',
      name: 'checklists',
      component: () => import('@/views/ChecklistView.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/alcohol',
      name: 'alcohol',
      component: () => import('@/views/AlcoholView.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/deviations',
      name: 'deviations',
      component: () => import('@/views/DeviationListView.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/deviations/new',
      name: 'deviation-new',
      component: () => import('@/views/DeviationNewView.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
    },
    {
      path: '/manage-checklists',
      name: 'manage-checklists',
      component: () => import('@/views/ManageChecklistsView.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/dashboard',
      redirect: '/',
    },
  ],
})

/**
 * Global navigation guard that enforces authentication and role-based access control.
 *
 * Redirect rules:
 * - Unauthenticated users attempting to access a protected route are redirected to /login.
 * - Authenticated users attempting to access /login or /register are redirected to the dashboard.
 * - Authenticated users without the required role for a route are redirected to the dashboard.
 *
 * @param {Object} to - The target route.
 * @param {Object} from - The current route.
 * @param {Function} next - Function to resolve the navigation.
 */
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
