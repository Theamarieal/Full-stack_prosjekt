<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <h1>Dashboard</h1>
      <div class="user-info" v-if="authStore.user">
        <span
          >Logged in as: <strong>{{ authStore.user.email }}</strong></span
        >
        <span class="role-badge">{{ authStore.user.role }}</span>
        <button @click="handleLogout" class="logout-btn">Log out</button>
      </div>
    </header>

    <main class="dashboard-content">
      <div class="welcome-card">
        <h2>Welcome to Placeholder-navn</h2>
        <p>
          This is your overview for
          <strong>{{ authStore.user?.organization?.name || 'your restaurant' }}</strong
          >.
        </p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <h3>Checklists</h3>
          <p>Here active checklists will appear.</p>
        </div>
        <div class="stat-card">
          <h3>Temperature</h3>
          <p>Last meassurements from your fridges and freezers.</p>
        </div>
        <div class="stat-card clickable-card" @click="goToAlcohol">
          <h3>Alcohol</h3>
          <p>Register age checks, serving times, and current shift history.</p>
          <p v-if="alcoholStatus === false" class="warning-text">
            No alcohol registration for today ⚠️
          </p>

          <p v-else>OK</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import alcoholApi from '@/api/alcohol'

const alcoholWarning = ref('')

const authStore = useAuthStore()
const router = useRouter()
const alcoholStatus = ref(null)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

/**
 * Loads alcohol registration warning information for the dashboard.
 *
 * @returns {Promise<void>}
 */
async function loadAlcoholWarning() {
  try {
    const shiftId = 1
    const response = await alcoholApi.getMissingRegistrationWarning(shiftId)

    if (response.data.missing) {
      alcoholWarning.value = response.data.message
    }
  } catch (error) {
    console.error('Failed to load alcohol warning:', error)
  }
}

/**
 * Navigates to the alcohol compliance view.
 *
 * @returns {Promise<void>}
 */
async function goToAlcohol() {
  await router.push('/alcohol')
}

onMounted(async () => {
  try {
    const res = await alcoholApi.getAlcoholStatus()
    alcoholStatus.value = res.data.hasLogs
  } catch (e) {
    console.error('Failed to load alcohol status', e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 2px solid #eee;
  margin-bottom: 30px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.role-badge {
  background: #2c3e50;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}
.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.welcome-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}
.stat-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.clickable-card {
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    border-color 0.15s ease;
}

.clickable-card:hover {
  transform: translateY(-2px);
  border-color: #2563eb;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.12);
}

.warning {
  border: 2px solid #ef4444;
  background: #fef2f2;
}

.warning-text {
  color: #dc2626;
  font-weight: 600;
}
</style>
