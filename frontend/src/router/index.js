import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      // since we haven't made LoginView.vue yet, we'll jsut put a placeholder:
      component: { render: () => h('div', h('h1', 'Login Page Placeholder')) }
    },
    {
      path: '/',
      name: 'dashboard',
      // same for Dashboard:
      component: { render: () => h('div', h('h1', 'Dashboard Placeholder')) },
      meta: { requiresAuth: true }
    }
  ],
})

// navigation-guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // check if route demands login trhough meta-field
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !authStore.isLoggedIn) {
    // not logged in? send to login!
    next('/login')
  } else {
    // logged in or public site? go ahead!
    next()
  }
})

export default router

import { h } from 'vue'