<template>
  <div id="app">
    <nav class="global-nav" v-if="authStore.isLoggedIn" aria-label="Main navigation">
      
      <router-link to="/" class="nav-logo">
        <div class="logo-icon-nav" aria-hidden="true">
          <div class="logo-line line-1"></div>
          <div class="logo-line line-2"></div>
          <div class="logo-line line-3"></div>
          <div class="logo-line line-4"></div>
          <div class="logo-check-nav">
            <svg viewBox="0 0 24 24" fill="none">
              <polyline points="5,12 10,17 19,7" stroke="#CECBF6" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </div>
        <span class="app-name-nav">Checkd</span>
      </router-link>

      <div class="nav-links">
        <router-link to="/">Dashboard</router-link>
        <router-link to="/checklists">Checklists</router-link>
        <router-link to="/temperature">Temperature</router-link>
        <router-link to="/alcohol">Alcohol</router-link>
        <router-link to="/deviations">Deviations</router-link>

        <router-link 
          v-if="authStore.getUserRole === 'MANAGER' || authStore.getUserRole === 'ADMIN'"
          to="/manage-checklists" 
          role="menuitem" 
          class="manager-link"
        >Manage</router-link>
        
        <router-link 
          v-if="authStore.getUserRole === 'ADMIN'"
          to="/admin" 
          role="menuitem" 
          class="admin-link"
        >Admin</router-link>
      </div>

      <div class="nav-right">
        <span class="nav-user">{{ authStore.user?.email.split('@')[0] }}</span>
        <button @click="handleLogout" class="logout-btn">Log out</button>
      </div>

      <button
        type="button"
        class="hamburger"
        @click="menuOpen = !menuOpen"
        :aria-expanded="menuOpen.toString()"
        aria-controls="mobile-menu"
        aria-label="Toggle navigation menu"
      >
        <span class="hamburger-line" :class="{ open: menuOpen }"></span>
        <span class="hamburger-line" :class="{ open: menuOpen }"></span>
        <span class="hamburger-line" :class="{ open: menuOpen }"></span>
      </button>
    </nav>

    <div
      v-if="authStore.isLoggedIn && menuOpen"
      id="mobile-menu"
      class="mobile-menu"
    >   
      <router-link to="/" @click="menuOpen = false">Dashboard</router-link>
      <router-link to="/checklists" @click="menuOpen = false">Checklists</router-link>
      <router-link to="/temperature" @click="menuOpen = false">Temperature</router-link>
      <router-link to="/alcohol" @click="menuOpen = false">Alcohol</router-link>
      <router-link to="/deviations" @click="menuOpen = false">Deviations</router-link>
      <router-link
        v-if="authStore.getUserRole === 'MANAGER' || authStore.getUserRole === 'ADMIN'"
        to="/manage-checklists"
        @click="menuOpen = false"
        class="manager-link-mobile"
      >Manage checklists</router-link>
      <router-link
        v-if="authStore.getUserRole === 'ADMIN'"
        to="/admin"
        @click="menuOpen = false"
        class="admin-link-mobile"
      >
        Admin
      </router-link>
      <div class="mobile-menu-footer">
        <span class="mobile-user">{{ authStore.user?.email }}</span>
        <button @click="handleLogout" class="logout-btn-mobile">Log out</button>
      </div>
    </div>

    <router-view />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)

function handleLogout() {
  menuOpen.value = false
  authStore.logout()
  router.push('/login')
}

function navigate(path) {
  menuOpen.value = false
  router.push(path)
}
</script>

<style>
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

#app {
  min-height: 100vh;
}

.global-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e0dfd8;
  min-height: 72px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(60, 52, 137, 0.05);
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-icon-nav {
  position: relative;
  width: 42px;
  height: 42px;
  background: #534AB7;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 3px;
  flex-shrink: 0;
}

.logo-line { height: 3.5px; border-radius: 2px; }
.line-1 { width: 20px; background: #FAC775; opacity: 0.9; }
.line-2 { width: 15px; background: #9FE1CB; opacity: 0.85; }
.line-3 { width: 18px; background: #F5C4B3; opacity: 0.8; }
.line-4 { width: 12px; background: #B5D4F4; opacity: 0.7; }

.logo-check-nav {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  background: #26215C;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #ffffff;
}
.logo-check-nav svg { width: 10px; height: 10px; }

.app-name-nav {
  font-weight: 900;
  font-size: 1.4rem;
  color: #3C3489;
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: 4px;
  flex: 1;
  margin-left: 20px;
}

.nav-links a {
  color: #4b5563;
  padding: 0 14px;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  min-height: 72px;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.nav-links a:hover {
  color: #534AB7;
  background: #f8fafc;
}

.nav-links .router-link-active {
  color: #534AB7;
  border-bottom-color: #534AB7;
}

.manager-link { color: #d97706 !important; }
.admin-link { color: #e74c3c !important; font-weight: bold; }

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-user {
  font-size: 0.85rem;
  color: #4b5563;
  font-weight: 600;
  background: #f5f4ff;
  padding: 6px 12px;
  border-radius: 20px;
}

.logout-btn {
  background: transparent;
  color: #e74c3c;
  border: 1px solid #fecaca;
  padding: 8px 14px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.logout-btn:hover {
  background: #fef2f2;
  border-color: #e74c3c;
}

.hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.hamburger-line {
  display: block;
  width: 24px;
  height: 2.5px;
  background: #534AB7;
  border-radius: 2px;
  transition: 0.3s;
}

.hamburger-line.open:nth-child(1) { transform: translateY(7.5px) rotate(45deg); }
.hamburger-line.open:nth-child(2) { opacity: 0; }
.hamburger-line.open:nth-child(3) { transform: translateY(-7.5px) rotate(-45deg); }

.mobile-menu {
  display: flex;
  flex-direction: column;
  background: #3C3489;
  position: sticky;
  top: 72px;
  z-index: 999;
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.2);
}

.mobile-menu a {
  padding: 18px 24px;
  text-decoration: none;
  color: #ffffff;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-menu a:hover {
  background: rgba(255, 255, 255, 0.1);
}

.mobile-menu .manager-link-mobile { color: #FAC775; }

.mobile-menu .admin-link-mobile { color: #fca5a5; }

.mobile-menu-footer {
  padding: 16px 24px;
  background: rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mobile-user { font-size: 0.8rem; color: #CECBF6; font-weight: 600; }

.logout-btn-mobile {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 700;
}

.nav-logo:focus-visible,
.nav-links a:focus-visible,
.logout-btn:focus-visible,
.hamburger:focus-visible,
.mobile-menu a:focus-visible,
.logout-btn-mobile:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

@media (max-width: 900px) {
  .nav-links, .nav-right { display: none; }
  .hamburger { display: flex; }
}
</style>