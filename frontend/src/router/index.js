import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
      path: '/',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    // for later if we want a specific dashboard path pointing to the same place
    {
      path: '/dashboard',
      redirect: '/'
    }
  ],
})

// navigation-guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // check if route demands login trhough meta-field
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (requiresAuth && !authStore.isLoggedIn) {
    // not logged in? send to login!
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isLoggedIn) {
    // already logged in, but trying to see login/register -> send to dashboard
    next('/')
  } else {
    // logged in or public site? go ahead!
    next()
  }
})

export default router