<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <div>
        <h1>Dashboard</h1>
        <p class="dashboard-subtitle">
          Overview for
          <strong>{{ authStore.user?.organization?.name || 'your organization' }}</strong>
        </p>
      </div>

      <div class="user-info" v-if="authStore.user">
        <span class="user-email">
          Logged in as: <strong>{{ authStore.user.email }}</strong>
        </span>
        <span class="role-badge">{{ authStore.user.role }}</span>
        <button @click="handleLogout" class="logout-btn">Log out</button>
      </div>
    </header>

    <main class="dashboard-content">
      <section class="welcome-card">
        <h2>Welcome</h2>
        <p>Here you get a quick overview of food and alcohol compliance activity.</p>
      </section>

      <section class="stats-grid">
        <div class="stat-card">
          <h3>Checklists</h3>
          <p>Active checklists will appear here.</p>
        </div>

        <div
          class="stat-card clickable-card"
          :class="{ warning: hasTemperatureDeviations }"
          @click="goToTemperature"
        >
          <div class="card-header">
            <h3>Temperature</h3>
            <span class="card-link">Open</span>
          </div>

          <p class="card-description">
            View measurements and deviations for fridges, freezers, and other equipment.
          </p>

          <div v-if="temperatureSummary" class="status-list">
            <div class="status-item">
              <span>Measurements today</span>
              <strong>{{ temperatureSummary.measurementsToday }}</strong>
            </div>

            <div class="status-item">
              <span>Deviations today</span>
              <strong :class="{ 'warning-text': temperatureSummary.deviationsToday > 0 }">
                {{ temperatureSummary.deviationsToday }}
              </strong>
            </div>
          </div>

          <div v-if="latestTemperatureDeviation" class="latest-deviation-box">
            <p class="latest-deviation-title">Latest deviation</p>
            <p>
              <strong>{{
                latestTemperatureDeviation.equipment?.name || 'Unknown equipment'
              }}</strong>
            </p>
            <p>{{ latestTemperatureDeviation.value }} °C</p>
            <p class="small-text">
              {{ formatDate(latestTemperatureDeviation.timestamp) }}
            </p>
          </div>

          <p v-if="temperatureSummary && temperatureSummary.deviationsToday === 0" class="ok-text">
            No temperature deviations registered today
          </p>
        </div>

        <div
          class="stat-card clickable-card"
          :class="{ warning: alcoholStatus === false || alcoholWarning }"
          @click="goToAlcohol"
        >
          <div class="card-header">
            <h3>Alcohol</h3>
            <span class="card-link">Open</span>
          </div>

          <p class="card-description">
            Register age checks, serving events, and incidents for the current shift.
          </p>

          <p v-if="alcoholStatus === false" class="warning-text">
            No alcohol registration for today
          </p>

          <p v-else-if="alcoholStatus === true" class="ok-text">Registrations found for today</p>

          <p v-if="alcoholWarning" class="warning-text">
            {{ alcoholWarning }}
          </p>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import alcoholApi from '@/api/alcohol'
import temperatureApi from '@/api/temperature'

const authStore = useAuthStore()
const router = useRouter()

const alcoholStatus = ref(null)
const alcoholWarning = ref('')

const temperatureSummary = ref(null)
const latestTemperatureDeviations = ref([])

const latestTemperatureDeviation = computed(() => {
  return latestTemperatureDeviations.value.length > 0 ? latestTemperatureDeviations.value[0] : null
})

const hasTemperatureDeviations = computed(() => {
  return temperatureSummary.value && temperatureSummary.value.deviationsToday > 0
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

function formatDate(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('en-GB')
}

async function loadAlcoholStatus() {
  try {
    const res = await alcoholApi.getAlcoholStatus()
    alcoholStatus.value = res.data.hasLogs
  } catch (error) {
    console.error('Failed to load alcohol status:', error)
  }
}

async function loadAlcoholWarning() {
  try {
    const shiftId = 1
    const response = await alcoholApi.getMissingRegistrationWarning(shiftId)

    if (response.data.missing) {
      alcoholWarning.value = response.data.message
    } else {
      alcoholWarning.value = ''
    }
  } catch (error) {
    console.error('Failed to load alcohol warning:', error)
  }
}

async function loadTemperatureData() {
  try {
    const summaryResponse = await temperatureApi.getTemperatureSummary()
    temperatureSummary.value = summaryResponse.data
  } catch (error) {
    console.error('Failed to load temperature summary:', error)
  }

  try {
    const deviationsResponse = await temperatureApi.getLatestDeviations(5)
    latestTemperatureDeviations.value = deviationsResponse.data
  } catch (error) {
    console.error('Failed to load latest temperature deviations:', error)
  }
}

async function goToAlcohol() {
  await router.push('/alcohol')
}

async function goToTemperature() {
  await router.push('/temperature')
}

onMounted(async () => {
  await Promise.all([loadAlcoholStatus(), loadAlcoholWarning(), loadTemperatureData()])
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
  margin-bottom: 30px;
}

.dashboard-header h1 {
  margin: 0 0 8px;
}

.dashboard-subtitle {
  margin: 0;
  color: #555;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.user-email {
  color: #333;
}

.role-badge {
  background: #2c3e50;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.logout-btn:hover {
  background: #c0392b;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #ececec;
}

.welcome-card h2 {
  margin-top: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.stat-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  min-height: 220px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-header h3 {
  margin: 0;
}

.card-link {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2563eb;
}

.card-description {
  margin-bottom: 16px;
  color: #555;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px 12px;
}

.latest-deviation-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.latest-deviation-title {
  margin: 0 0 8px;
  font-weight: 700;
  color: #991b1b;
}

.small-text {
  font-size: 0.9rem;
  color: #666;
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
  background: #fffafa;
}

.warning-text {
  color: #dc2626;
  font-weight: 600;
}

.ok-text {
  color: #15803d;
  font-weight: 600;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-info {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
