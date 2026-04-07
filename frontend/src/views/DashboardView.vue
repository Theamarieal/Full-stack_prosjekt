<template>
  <div class="dashboard">
    <main class="dashboard-content">
      <div class="welcome-card">
        <h2>Welcome to Checkd</h2>
        <p>
          This is your overview for
          <strong>{{ authStore.user?.organization?.name || 'your restaurant' }}</strong
          >.
        </p>
      </div>

      <div v-if="canViewReports" class="manager-tools-section">
        <h2>Management</h2>

        <div class="stats-grid">
          <div class="stat-card clickable-card" @click="goToReports">
            <div class="card-header">
              <h3>Reports</h3>
              <span class="card-link">Open</span>
            </div>

            <p class="card-description">
              Generate compliance reports for deviations, temperature logs, and alcohol logs.
            </p>

            <p class="ok-text">Available to managers and administrators</p>
          </div>
        </div>
      </div>

      <div class="stat-card clickable-card" @click="router.push('/training')">
        <div class="card-header">
          <h3>Training</h3>
          <span class="card-link">Open</span>
        </div>

        <p class="card-description">
          View policies, complete training quizzes, and track certifications.
        </p>

        <p class="ok-text">Policies, materials, and certifications in one place</p>
      </div>

      <div class="sections-grid">
        <section class="module-section">
          <h2>IK-Mat</h2>

          <div class="stats-grid">
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
                  <strong>
                    {{ latestTemperatureDeviation.equipment?.name || 'Unknown equipment' }}
                  </strong>
                </p>
                <p>{{ latestTemperatureDeviation.value }} °C</p>
                <p class="small-text">
                  {{ formatDate(latestTemperatureDeviation.timestamp) }}
                </p>
              </div>

              <p
                v-if="temperatureSummary && temperatureSummary.deviationsToday === 0"
                class="ok-text"
              >
                No temperature deviations registered today
              </p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/deviations?module=IK_MAT')">
              <h3>Deviations</h3>
              <div class="deviation-header">
                <span v-if="matDeviations > 0" class="badge">{{ matDeviations }}</span>
              </div>
              <p v-if="matDeviations > 0" class="warning-text">Open deviations</p>
              <p v-else class="ok-text">No open deviations ✓</p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/checklists')">
              <h3>Checklists</h3>
              <p v-if="matStats.total === 0">No checklists found.</p>
              <p v-else>
                <span class="stat-number">{{ matStats.completed }}</span> / {{ matStats.total }}
                completed
              </p>
              <p v-if="matStats.remaining > 0" class="warning-text">
                {{ matStats.remaining }} tasks remaining ⚠️
              </p>
              <p v-else-if="matStats.total > 0" class="ok-text">All done ✓</p>
            </div>
          </div>
        </section>

        <section class="module-section">
          <h2>IK-Alcohol</h2>

          <div class="stats-grid">
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

              <p v-else-if="alcoholStatus === true" class="ok-text">
                Registrations found for today
              </p>

              <p v-if="alcoholWarning" class="warning-text">
                {{ alcoholWarning }}
              </p>
            </div>

            <div
              class="stat-card clickable-card"
              @click="router.push('/deviations?module=IK_ALKOHOL')"
            >
              <h3>Deviations</h3>
              <div class="deviation-header">
                <span v-if="alcoholDeviations > 0" class="badge">{{ alcoholDeviations }}</span>
              </div>
              <p v-if="alcoholDeviations > 0" class="warning-text">Open deviations</p>
              <p v-else class="ok-text">No open deviations ✓</p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/checklists')">
              <h3>Checklists</h3>
              <p v-if="alcoholStats.total === 0">No checklists found.</p>
              <p v-else>
                <span class="stat-number">{{ alcoholStats.completed }}</span> /
                {{ alcoholStats.total }} completed
              </p>
              <p v-if="alcoholStats.remaining > 0" class="warning-text">
                {{ alcoholStats.remaining }} tasks remaining ⚠️
              </p>
              <p v-else-if="alcoholStats.total > 0" class="ok-text">All done ✓</p>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import alcoholApi from '@/api/alcohol'
import temperatureApi from '@/api/temperature'
import checklistApi from '@/api/checklist'
import deviationApi from '@/api/deviation'

const authStore = useAuthStore()
const router = useRouter()

const alcoholStatus = ref(null)
const alcoholWarning = ref('')

const temperatureSummary = ref(null)
const latestTemperatureDeviations = ref([])

const checklists = ref([])
const deviations = ref([])

const latestTemperatureDeviation = computed(() => {
  return latestTemperatureDeviations.value.length > 0 ? latestTemperatureDeviations.value[0] : null
})

const hasTemperatureDeviations = computed(() => {
  return temperatureSummary.value && temperatureSummary.value.deviationsToday > 0
})

const canViewReports = computed(() => {
  return ['MANAGER', 'ADMIN'].includes(authStore.user?.role)
})

const matDeviations = computed(
  () => deviations.value.filter((d) => d.status === 'OPEN' && d.module === 'IK_MAT').length,
)

const alcoholDeviations = computed(
  () => deviations.value.filter((d) => d.status === 'OPEN' && d.module === 'IK_ALKOHOL').length,
)

const matChecklists = computed(() => checklists.value.filter((c) => c.module !== 'BAR'))

const alcoholChecklists = computed(() => checklists.value.filter((c) => c.module === 'BAR'))

const matStats = computed(() => {
  const allItems = matChecklists.value.flatMap((c) => c.items || [])
  const completed = allItems.filter((i) => i.completed).length
  const total = allItems.length
  return { completed, total, remaining: total - completed }
})

const alcoholStats = computed(() => {
  const allItems = alcoholChecklists.value.flatMap((c) => c.items || [])
  const completed = allItems.filter((i) => i.completed).length
  const total = allItems.length
  return { completed, total, remaining: total - completed }
})

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

async function goToReports() {
  await router.push('/reports')
}

onMounted(async () => {
  await Promise.all([loadAlcoholStatus(), loadAlcoholWarning(), loadTemperatureData()])

  try {
    const res = await deviationApi.getAll()
    deviations.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Failed to load deviations', e)
  }

  try {
    const res = await checklistApi.getAll()
    checklists.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Failed to load checklists', e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
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
  margin-bottom: 8px;
}

.sections-grid {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.module-section {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
}

.module-section h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #2c3e50;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
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

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2c3e50;
}

.warning-text {
  color: #dc2626;
  font-weight: 600;
}

.ok-text {
  color: #15803d;
  font-weight: 600;
}

.deviation-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.badge {
  background: #dc2626;
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: bold;
}

.manager-tools-section {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
}

.manager-tools-section h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
