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

      <LoadingSpinner v-if="loading" />

      <div v-else class="dashboard-sections">
        <div v-if="error" class="error-banner">
          {{ error }}
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

        <div class="training-section">
          <div class="stats-grid">
            <div class="stat-card clickable-card" @click="goToTraining">
              <div class="card-header">
                <h3>Training</h3>
                <span class="card-link">Open</span>
              </div>

              <p class="card-description">
                View policies, complete training quizzes, and track certifications.
              </p>

              <p class="ok-text">Policies, materials, and certifications in one place</p>
            </div>
          </div>
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
                    <strong>{{ temperatureSummary.measurementsToday ?? 0 }}</strong>
                  </div>

                  <div class="status-item">
                    <span>Deviations today</span>
                    <strong
                      :class="{ 'warning-text': (temperatureSummary.deviationsToday ?? 0) > 0 }"
                    >
                      {{ temperatureSummary.deviationsToday ?? 0 }}
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
                  v-if="temperatureSummary && (temperatureSummary.deviationsToday ?? 0) === 0"
                  class="ok-text"
                >
                  No temperature deviations registered today
                </p>
              </div>

              <div
                class="stat-card clickable-card"
                @click="router.push('/deviations?module=IK_MAT')"
              >
                <div class="card-header">
                  <h3>Deviations</h3>
                  <span v-if="matDeviations > 0" class="badge">{{ matDeviations }}</span>
                </div>

                <p class="card-description">View and follow up open food safety deviations.</p>

                <p v-if="matDeviations > 0" class="warning-text">Open deviations</p>
                <p v-else class="ok-text">No open deviations ✓</p>
              </div>

              <div class="stat-card clickable-card" @click="router.push('/checklists')">
                <div class="card-header">
                  <h3>Checklists</h3>
                  <span class="card-link">Open</span>
                </div>

                <p class="card-description">Track task completion for food safety routines.</p>

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
                :class="{ warning: alcoholStatus === false || !!alcoholWarning }"
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
                <div class="card-header">
                  <h3>Deviations</h3>
                  <span v-if="alcoholDeviations > 0" class="badge">{{ alcoholDeviations }}</span>
                </div>

                <p class="card-description">View and follow up open alcohol-related deviations.</p>

                <p v-if="alcoholDeviations > 0" class="warning-text">Open deviations</p>
                <p v-else class="ok-text">No open deviations ✓</p>
              </div>

              <div class="stat-card clickable-card" @click="router.push('/checklists')">
                <div class="card-header">
                  <h3>Checklists</h3>
                  <span class="card-link">Open</span>
                </div>

                <p class="card-description">
                  Track checklist completion for bar and serving routines.
                </p>

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
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const authStore = useAuthStore()
const router = useRouter()

const loading = ref(true)
const error = ref('')

const alcoholStatus = ref(null)
const alcoholWarning = ref('')
const temperatureSummary = ref(null)
const latestTemperatureDeviations = ref([])
const checklists = ref([])
const deviations = ref([])

const currentShiftId = computed(() => {
  return (
    authStore.user?.activeShift?.id ||
    authStore.user?.currentShift?.id ||
    authStore.user?.shift?.id ||
    null
  )
})

const latestTemperatureDeviation = computed(() => {
  return latestTemperatureDeviations.value.length > 0 ? latestTemperatureDeviations.value[0] : null
})

const hasTemperatureDeviations = computed(() => {
  return (temperatureSummary.value?.deviationsToday ?? 0) > 0
})

const canViewReports = computed(() => {
  return ['MANAGER', 'ADMIN'].includes(authStore.user?.role)
})

const matDeviations = computed(() => {
  return deviations.value.filter(
    (d) => d.status === 'OPEN' && (d.module === 'IK_MAT' || d.module === 'IK_MATVARE'),
  ).length
})

const alcoholDeviations = computed(() => {
  return deviations.value.filter(
    (d) => d.status === 'OPEN' && (d.module === 'IK_ALKOHOL' || d.module === 'BAR'),
  ).length
})

const matChecklists = computed(() =>
  checklists.value.filter((c) => c.module !== 'BAR' && c.module !== 'IK_ALKOHOL'),
)

const alcoholChecklists = computed(() =>
  checklists.value.filter((c) => c.module === 'BAR' || c.module === 'IK_ALKOHOL'),
)

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

  try {
    return new Date(dateTime).toLocaleString('en-GB', {
      dateStyle: 'short',
      timeStyle: 'short',
    })
  } catch {
    return dateTime
  }
}

async function loadAlcoholStatus() {
  try {
    const res = await alcoholApi.getAlcoholStatus()
    alcoholStatus.value = res.data?.hasLogs ?? false
  } catch (e) {
    console.error('Failed to load alcohol status:', e)
    alcoholStatus.value = null
  }
}

async function loadAlcoholWarning() {
  try {
    if (!currentShiftId.value || !alcoholApi.getMissingRegistrationWarning) {
      alcoholWarning.value = ''
      return
    }

    const response = await alcoholApi.getMissingRegistrationWarning(currentShiftId.value)
    alcoholWarning.value = response.data?.missing ? response.data.message : ''
  } catch (e) {
    console.error('Failed to load alcohol warning:', e)
    alcoholWarning.value = ''
  }
}

async function loadTemperatureData() {
  try {
    const summaryResponse = await temperatureApi.getTemperatureSummary()
    temperatureSummary.value = summaryResponse.data || null
  } catch (e) {
    console.error('Failed to load temperature summary:', e)
    temperatureSummary.value = null
  }

  try {
    const deviationsResponse = await temperatureApi.getLatestDeviations(5)
    latestTemperatureDeviations.value = Array.isArray(deviationsResponse.data)
      ? deviationsResponse.data
      : []
  } catch (e) {
    console.error('Failed to load latest temperature deviations:', e)
    latestTemperatureDeviations.value = []
  }
}

async function loadDashboardLists() {
  const [deviationRes, checklistRes] = await Promise.all([
    deviationApi.getAll(),
    checklistApi.getAll(),
  ])

  deviations.value = Array.isArray(deviationRes.data) ? deviationRes.data : []
  checklists.value = Array.isArray(checklistRes.data) ? checklistRes.data : []
}

function goToAlcohol() {
  router.push('/alcohol')
}

function goToTemperature() {
  router.push('/temperature')
}

function goToReports() {
  router.push('/reports')
}

function goToTraining() {
  router.push('/training')
}

onMounted(async () => {
  loading.value = true
  error.value = ''

  try {
    await Promise.all([
      loadAlcoholStatus(),
      loadAlcoholWarning(),
      loadTemperatureData(),
      loadDashboardLists(),
    ])
  } catch (e) {
    console.error('Failed to load dashboard data:', e)
    error.value = 'Failed to load dashboard data.'
  } finally {
    loading.value = false
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

.dashboard-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.error-banner {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 16px;
  border-radius: 12px;
  font-weight: 600;
  text-align: center;
}

.welcome-card,
.manager-tools-section,
.training-section,
.module-section {
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.welcome-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.welcome-card h2 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1f2937;
}

.sections-grid {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.module-section h2,
.manager-tools-section h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #1f2937;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
  min-height: 220px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.card-header h3 {
  margin: 0;
  color: #111827;
}

.card-link {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2563eb;
  white-space: nowrap;
}

.card-description {
  margin-bottom: 16px;
  color: #4b5563;
  line-height: 1.5;
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
  border-radius: 10px;
  padding: 10px 12px;
}

.latest-deviation-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
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
  color: #6b7280;
}

.clickable-card {
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.clickable-card:hover {
  transform: translateY(-3px);
  border-color: #2563eb;
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.12);
}

.warning {
  border: 2px solid #ef4444;
  background: #fffafa;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
}

.warning-text {
  color: #dc2626;
  font-weight: 600;
  margin-top: auto;
}

.ok-text {
  color: #15803d;
  font-weight: 600;
  margin-top: auto;
}

.badge {
  background: #dc2626;
  color: white;
  border-radius: 999px;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    min-height: unset;
  }
}
</style>
