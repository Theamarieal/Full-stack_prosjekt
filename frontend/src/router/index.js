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
    },
    {
      path: '/temperature-history',
      name: 'temperature-history',
      component: TemperatureHistoryView,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
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
      path: '/reports',
      name: 'reports',
      component: () => import('@/views/ReportView.vue'),
      meta: { requiresAuth: true, roles: ['MANAGER', 'ADMIN'] },
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },

    {
      path: '/checklists',
      name: 'checklists',
      component: () => import('../views/ChecklistView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    /**
     * Route definition for the alcohol compliance module.
     *
     * <p>This view is available to authenticated users with the roles
     * EMPLOYEE, MANAGER, or ADMIN.
     */
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
      component: () => import('../views/DeviationListView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/manage-checklists',
      name: 'manage-checklists',
      component: () => import('../views/ManageChecklistsView.vue'),
      meta: { requiresAuth: true, role: 'MANAGER' }
    },
    {
      path: '/deviations/new',
      name: 'deviation-new',
      component: () => import('../views/DeviationNewView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'],
      },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['ADMIN'],
      },
    },
    // for later if we want a specific dashboard path pointing to the same place
    {
      path: '/dashboard',
      redirect: '/',
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

<<<<<<< HEAD
  if (to.meta.requiresAuth && !authStore.token) {
    return next('/login')
=======
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isLoggedIn) {
    next('/')
  } else if (to.meta.roles && !to.meta.roles.includes(authStore.getUserRole)) {
    next('/')
  } else {
    next()
>>>>>>> 335734ccb015cfd6ba6b2821ccafa8bde791bc9e
  }

  if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    return next('/')
  }

  next()
})

export default router
