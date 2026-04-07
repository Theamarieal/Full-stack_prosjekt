<template>
  <div id="app">
    <nav class="global-nav" v-if="authStore.isLoggedIn">
      <span class="nav-logo">Checkd</span>
      <div class="nav-links">
        <a @click="router.push('/')">Dashboard</a>
        <a @click="router.push('/checklists')">Checklists</a>
        <a @click="router.push('/temperature')">Temperature</a>
        <a @click="router.push('/alcohol')">Alcohol</a>
        <a @click="router.push('/deviations')">Deviations</a>

        <a
          v-if="authStore.getUserRole === 'MANAGER' || authStore.getUserRole === 'ADMIN'"
          @click="router.push('/manage-checklists')"
          style="color: #f1c40f; font-weight: bold;"
        >
          Manage Checklists
        </a>
        <a
          v-if="authStore.getUserRole === 'ADMIN'"
          @click="router.push('/admin')"
          style="color: #e74c3c; font-weight: bold;"
        >
          Admin
        </a>
      </div>

      <div class="nav-right">
        <span class="nav-user">{{ authStore.user?.email }} ({{ authStore.user?.role }})</span>
        <button @click="handleLogout" class="logout-btn">Log out</button>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: Arial, sans-serif;
  background-color: #f7f6f2;
  color: #2c2c2a;
}

#app {
  min-height: 100vh;
}

.global-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #2c3e50;
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-logo {
  font-weight: bold;
  font-size: 1.2rem;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  color: white;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.95rem;
  opacity: 0.85;
}

.nav-links a:hover {
  opacity: 1;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-user {
  font-size: 0.85rem;
  opacity: 0.75;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
