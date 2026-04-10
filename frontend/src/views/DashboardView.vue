<template>
  <div class="dashboard-page">
    <main class="dashboard-content">
      <LoadingSpinner v-if="loading" message="Loading dashboard..." />

      <div v-else-if="error" class="error-banner" role="alert">
        <span aria-hidden="true">⚠ </span>{{ error }}
      </div>

      <div v-else class="dashboard-sections">
        <section class="welcome-card">
          <h2>Welcome to Checkd</h2>
          <p>
            This is your overview for
            <strong>{{ authStore.user?.organization?.name || 'your restaurant' }}</strong>.
          </p>
        </section>

        <section v-if="canViewReports" class="manager-tools-section">
          <h2>Management</h2>
          <div class="stats-grid">
            <button
              type="button"
              class="stat-card clickable-card"
              @click="goToReports"
              aria-label="Open reports section"
            >
              <div class="card-header">
                <h3>Reports</h3>
                <span class="card-link">Open</span>
              </div>

              <p class="card-description">
                Generate compliance reports for deviations, temperature logs, and alcohol logs.
              </p>

              <p class="ok-text">Available to managers and administrators</p>
            </button>
          </div>
        </section>

        <section class="training-section">
          <div class="stats-grid">
            <button
              type="button"
              class="stat-card clickable-card"
              @click="goToTraining"
              aria-label="Open training section"
            >
              <div class="card-header">
                <h3>Training</h3>
                <span class="card-link">Open</span>
              </div>

              <p class="card-description">
                View policies, complete training quizzes, and track certifications.
              </p>

              <p class="ok-text">Policies, materials, and certifications in one place</p>
            </button>
          </div>
        </section>

        <div class="sections-grid">
          <section class="module-section">
            <h2>IK-Mat</h2>
            <div class="stats-grid">
              <button
                type="button"
                class="stat-card clickable-card"
                :class="hasTemperatureDeviations ? 'status-danger' : (temperatureSummary?.measurementsToday > 0 ? 'status-success' : '')"
                @click="goToTemperature"
                :aria-label="hasTemperatureDeviations
                  ? `Temperature. ${temperatureSummary?.deviationsToday || 0} deviations today. Open temperature page.`
                  : `Temperature. ${temperatureSummary?.measurementsToday || 0} measurements today. Open temperature page.`"
              >
                <div class="card-header">
                  <h3>Temperature</h3>
                  <div class="card-status-icon">
                    <svg
                      v-if="!hasTemperatureDeviations && temperatureSummary?.measurementsToday > 0"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="3"
                      aria-hidden="true"
                    >
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span v-else-if="hasTemperatureDeviations" class="count-badge">!</span>
                  </div>
                </div>

                <p class="card-description">
                  View measurements and deviations for fridges, freezers, and other equipment.
                </p>

                <div v-if="temperatureSummary" class="mini-stats">
                  <div class="mini-stat">
                    <span>Today</span>
                    <strong>{{ temperatureSummary.measurementsToday ?? 0 }}</strong>
                  </div>
                  <div class="mini-stat">
                    <span>Deviations</span>
                    <strong>{{ temperatureSummary.deviationsToday ?? 0 }}</strong>
                  </div>
                </div>

                <p class="status-label" :class="hasTemperatureDeviations ? 'text-danger' : 'text-success'">
                  {{ hasTemperatureDeviations ? 'Deviation detected' : 'No deviations today' }}
                </p>

                <div v-if="latestTemperatureDeviation && hasTemperatureDeviations" class="latest-deviation-box">
                  <p class="latest-deviation-title">Latest deviation</p>
                  <p class="small-text">
                    {{ latestTemperatureDeviation.equipment?.name || 'Unknown equipment' }}
                  </p>
                  <p class="small-text">{{ formatDate(latestTemperatureDeviation.timestamp) }}</p>
                </div>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="matDeviations > 0 ? 'status-danger' : 'status-success'"
                @click="router.push('/deviations?module=IK_MAT')"
                :aria-label="matDeviations > 0
                  ? `Open deviations for food safety. ${matDeviations} open deviations.`
                  : 'Open deviations for food safety. No open deviations.'"
              >
                <div class="card-header">
                  <h3>Deviations</h3>
                  <span v-if="matDeviations > 0" class="count-badge">{{ matDeviations }}</span>
                  <span v-else class="card-link">Open</span>
                </div>

                <p class="card-description">View and follow up open food safety deviations.</p>

                <p :class="matDeviations > 0 ? 'text-danger' : 'text-success'" class="status-label">
                  {{ matDeviations > 0 ? 'Action required' : 'No open deviations' }}
                </p>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="matStats.remaining > 0 ? 'status-warning' : 'status-success'"
                @click="router.push('/checklists')"
                :aria-label="`Food safety checklists. ${matStats.completed} of ${matStats.total} tasks completed.`"
              >
                <div class="card-header">
                  <h3>Checklists</h3>
                  <span class="card-link">Open</span>
                </div>

                <p class="card-description">Track task completion for food safety routines.</p>

                <div v-if="matStats.total > 0" class="progress-row">
                  <span class="big-num">{{ matStats.completed }}</span>
                  <span class="total-num">/ {{ matStats.total }} completed</span>
                </div>

                <p v-if="matStats.total === 0" class="small-text">No checklists found.</p>
                <p v-else class="status-label" :class="matStats.remaining > 0 ? 'text-warning' : 'text-success'">
                  {{ matStats.remaining > 0 ? `${matStats.remaining} tasks remaining` : 'All done' }}
                </p>
              </button>
            </div>
          </section>

          <section class="module-section">
            <h2>IK-Alkohol</h2>
            <div class="stats-grid">
              <button
                type="button"
                class="stat-card clickable-card"
                :class="(!alcoholStatus || alcoholWarning) ? 'status-danger' : 'status-success'"
                @click="goToAlcohol"
                :aria-label="alcoholStatus
                  ? 'Alcohol log registered today. Open alcohol page.'
                  : 'Alcohol log missing today. Open alcohol page.'"
              >
                <div class="card-header">
                  <h3>Alcohol Log</h3>
                  <div class="card-status-icon">
                    <svg
                      v-if="alcoholStatus && !alcoholWarning"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="3"
                      aria-hidden="true"
                    >
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span v-else class="count-badge">!</span>
                  </div>
                </div>

                <p class="card-description">Age checks and shift incidents.</p>

                <div class="status-row">
                  <span :class="alcoholStatus ? 'dot-success' : 'dot-danger'" aria-hidden="true"></span>
                  <span class="small-text">{{ alcoholStatus ? 'Registered' : 'Missing today' }}</span>
                </div>

                <p v-if="alcoholWarning" class="text-danger xsmall">{{ alcoholWarning }}</p>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="alcoholDeviations > 0 ? 'status-danger' : 'status-success'"
                @click="router.push('/deviations?module=IK_ALKOHOL')"
                :aria-label="alcoholDeviations > 0
                  ? `Open alcohol deviations. ${alcoholDeviations} open deviations.`
                  : 'Open alcohol deviations. No open issues.'"
              >
                <div class="card-header">
                  <h3>Deviations</h3>
                  <span v-if="alcoholDeviations > 0" class="count-badge">{{ alcoholDeviations }}</span>
                  <span v-else class="card-link">Open</span>
                </div>

                <p class="card-description">View and follow up open alcohol-related deviations.</p>

                <p :class="alcoholDeviations > 0 ? 'text-danger' : 'text-success'" class="status-label">
                  {{ alcoholDeviations > 0 ? 'Follow up needed' : 'No open issues' }}
                </p>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="alcoholStats.remaining > 0 ? 'status-warning' : 'status-success'"
                @click="router.push('/checklists')"
                :aria-label="`Alcohol checklists. ${alcoholStats.completed} of ${alcoholStats.total} tasks completed.`"
              >
                <div class="card-header">
                  <h3>Checklists</h3>
                  <span class="card-link">Open</span>
                </div>

                <p class="card-description">
                  Track checklist completion for bar and serving routines.
                </p>

                <div v-if="alcoholStats.total > 0" class="progress-row">
                  <span class="big-num">{{ alcoholStats.completed }}</span>
                  <span class="total-num">/ {{ alcoholStats.total }} completed</span>
                </div>

                <p v-if="alcoholStats.total === 0" class="small-text">No checklists found.</p>
                <p v-else class="status-label" :class="alcoholStats.remaining > 0 ? 'text-warning' : 'text-success'">
                  {{ alcoholStats.remaining > 0 ? `${alcoholStats.remaining} tasks remaining` : 'All done' }}
                </p>
              </button>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
/**
 * DashboardView
 *
 * Main landing page shown after login.
 * Provides an overview of compliance status across all modules, including:
 * alcohol registration status, temperature deviation warnings,
 * checklist completion progress, and open deviations.
 * Accessible to all authenticated users.
 */
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
    deviationApi.getLargePage(),
    checklistApi.getLargePage(),
  ])

  deviations.value = Array.isArray(deviationRes.data?.content)
    ? deviationRes.data.content
    : (Array.isArray(deviationRes.data) ? deviationRes.data : [])
  checklists.value = Array.isArray(checklistRes.data?.content)
    ? checklistRes.data.content
    : (Array.isArray(checklistRes.data) ? checklistRes.data : [])
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
.dashboard-page {
  min-height: 100vh;
  background-color: #f7f6f2;
  padding: 24px 16px;
}

.dashboard-content {
  max-width: 1100px;
  margin: 0 auto;
}

.dashboard-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
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
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-left: 6px solid #d1d5db;
  border-radius: 14px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  min-height: 220px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  text-align: left;
  width: 100%;
}

button.stat-card {
  appearance: none;
  -webkit-appearance: none;
}

.status-success { border-left-color: #10b981; }
.status-warning { border-left-color: #f59e0b; }
.status-danger { border-left-color: #ef4444; }

.clickable-card {
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease,
    background 0.18s ease;
}

.clickable-card:hover {
  transform: translateY(-3px);
  background: #fcfcfb;
  border-color: #7F77DD;
  box-shadow: 0 12px 24px rgba(60, 52, 137, 0.08);
}

.clickable-card:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.card-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
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

.card-status-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.card-status-icon svg {
  width: 20px;
  height: 20px;
  color: #166534;
}

.count-badge {
  background: #dc2626;
  color: white;
  font-size: 0.8rem;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 6px;
  min-width: 24px;
  text-align: center;
}

.mini-stats {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.mini-stat {
  display: flex;
  flex-direction: column;
  font-size: 0.8rem;
}

.mini-stat span {
  color: #4b5563;
  font-weight: 600;
}

.mini-stat strong {
  font-size: 1.1rem;
  color: #3C3489;
}

.progress-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.big-num {
  font-size: 1.75rem;
  font-weight: 800;
  color: #3C3489;
}

.total-num {
  font-size: 0.95rem;
  color: #4b5563;
  font-weight: 600;
}

.status-label {
  font-size: 0.85rem;
  font-weight: 700;
  margin-top: 8px;
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

.status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.dot-success,
.dot-danger {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex-shrink: 0;
}

.dot-success { background: #166534; }
.dot-danger { background: #991b1b; }

.small-text {
  color: #374151;
  font-weight: 600;
  font-size: 0.9rem;
}

.xsmall {
  margin-top: 8px;
  font-size: 0.8rem;
}

.text-danger {
  color: #991b1b;
  font-weight: 700;
}

.text-success {
  color: #166534;
  font-weight: 700;
}

.text-warning {
  color: #92400e;
  font-weight: 700;
}

.ok-text {
  color: #15803d;
  font-weight: 600;
  margin-top: auto;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 24px;
}

@media (max-width: 350px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    min-height: unset;
  }
}
</style>
