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

      <!-- Hamburger button (mobile only) -->
      <button
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

    <!-- Mobile dropdown menu -->
    <div
      v-if="authStore.isLoggedIn && menuOpen"
      id="mobile-menu"
      class="mobile-menu"
      role="menu"
    >
      <a @click="navigate('/')" role="menuitem">Dashboard</a>
      <a @click="navigate('/checklists')" role="menuitem">Checklists</a>
      <a @click="navigate('/temperature')" role="menuitem">Temperature</a>
      <a @click="navigate('/alcohol')" role="menuitem">Alcohol</a>
      <a @click="navigate('/deviations')" role="menuitem">Deviations</a>
      <a
        v-if="authStore.getUserRole === 'MANAGER' || authStore.getUserRole === 'ADMIN'"
        @click="navigate('/manage-checklists')"
        role="menuitem"
        class="manager-link"
      >Manage checklists</a>
      <div class="mobile-menu-footer">
        <span class="mobile-user">{{ authStore.user?.email }}</span>
        <button @click="handleLogout" class="logout-btn">Log out</button>
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

// Close menu after navigating on mobile
function navigate(path) {
  menuOpen.value = false
  router.push(path)
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

/* ── Navbar ─────────────────────────────────────────── */
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
  gap: 16px;
}

.nav-logo {
  font-weight: bold;
  font-size: 1.2rem;
  cursor: pointer;
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav-links a {
  color: white;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.95rem;
  opacity: 0.85;
  white-space: nowrap;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
}

.nav-links a:hover,
.nav-links a:focus-visible {
  opacity: 1;
  outline: 2px solid rgba(255, 255, 255, 0.5);
  outline-offset: 2px;
  border-radius: 2px;
}

.manager-link {
  color: #f1c40f !important;
  font-weight: bold;
}

.admin-link {
  color: #e74c3c !important; /* Den røde fargen fra dev-branchen */
  font-weight: bold;
}

.mobile-menu .admin-link {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(231, 76, 60, 0.1); /* En svak rød nyanse i bakgrunnen på mobil */
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.nav-user {
  font-size: 0.85rem;
  opacity: 0.75;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 4px;
  cursor: pointer;
  min-height: 44px;
  font-size: 0.9rem;
}

.logout-btn:focus-visible {
  outline: 2px solid white;
  outline-offset: 2px;
}

/* ── Hamburger button (hidden on desktop) ────────────── */
.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  min-width: 44px;
  min-height: 44px;
}

.hamburger:focus-visible {
  outline: 2px solid white;
  outline-offset: 2px;
  border-radius: 4px;
}

.hamburger-line {
  display: block;
  width: 22px;
  height: 2px;
  background: white;
  border-radius: 2px;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

/* Animate hamburger to X when menu is open */
.hamburger-line:nth-child(1).open { transform: translateY(7px) rotate(45deg); }
.hamburger-line:nth-child(2).open { opacity: 0; }
.hamburger-line:nth-child(3).open { transform: translateY(-7px) rotate(-45deg); }

/* ── Mobile dropdown (hidden on desktop) ─────────────── */
.mobile-menu {
  display: none;
  flex-direction: column;
  background: #34495e;
  position: sticky;
  top: 57px;
  z-index: 99;
  padding: 4px 0 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.mobile-menu a {
  color: white;
  cursor: pointer;
  text-decoration: none;
  padding: 0 24px;
  font-size: 1rem;
  opacity: 0.9;
  min-height: 52px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.mobile-menu a:hover,
.mobile-menu a:focus-visible {
  background: rgba(255, 255, 255, 0.1);
  opacity: 1;
}

.mobile-menu-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  margin-top: 4px;
  gap: 12px;
}

.mobile-user {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Responsive breakpoints ──────────────────────────── */
@media (max-width: 768px) {
  .nav-links,
  .nav-right {
    display: none;
  }

  .hamburger {
    display: flex;
  }

  .mobile-menu {
    display: flex;
  }

  .global-nav {
    padding: 12px 16px;
  }
}
</style>
