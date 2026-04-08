<template>
  <div class="dashboard-page">
    <main class="dashboard-content">
      <LoadingSpinner v-if="loading" message="Loading dashboard..." />

      <div v-else-if="error" class="error-banner" role="alert">
        <span aria-hidden="true">⚠ </span>{{ error }}
      </div>

      <template v-else>
        <header class="welcome-section">
          <div class="welcome-text">
            <h1>Welcome back, {{ authStore.user?.email?.split('@')[0] }}</h1>
            <p>Everest Sushi & Fusion AS</p>
          </div>
          <div class="date-display">
            {{ new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long' }) }}
          </div>
        </header>

        <div class="sections-container">
          <section class="module-group">
            <h2 class="module-title">IK-Mat (Food Safety)</h2>
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
                <p class="card-info">Monitor fridges and freezers.</p>
                <div v-if="temperatureSummary" class="mini-stats">
                  <div class="mini-stat"><span>Today</span><strong>{{ temperatureSummary.measurementsToday }}</strong></div>
                  <div class="mini-stat"><span>Deviations</span><strong>{{ temperatureSummary.deviationsToday }}</strong></div>
                </div>
                <p class="status-label" :class="hasTemperatureDeviations ? 'text-danger' : 'text-success'">
                  {{ hasTemperatureDeviations ? 'Deviation detected' : 'No deviations today' }}
                </p>
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
                  <h3>Open Deviations</h3>
                  <div class="card-status-icon">
                    <span v-if="matDeviations > 0" class="count-badge">{{ matDeviations }}</span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
                <p class="card-info">Unresolved food safety issues.</p>
                <p :class="matDeviations > 0 ? 'text-danger' : 'text-success'" class="status-label">
                  {{ matDeviations > 0 ? 'Action required' : 'All clear' }}
                </p>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="matStats.remaining > 0 ? 'status-warning' : 'status-success'"
                @click="router.push('/checklists')"
                :aria-label="`Daily routines. ${matStats.completed} of ${matStats.total} tasks completed. Open checklists.`"
              >
                <div class="card-header">
                  <h3>Daily Routines</h3>
                  <div class="card-status-icon">
                    <span v-if="matStats.remaining > 0" class="count-badge warn">{{ matStats.remaining }}</span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
                <div class="progress-row">
                  <span class="big-num">{{ matStats.completed }}</span><span class="total-num">/ {{ matStats.total }} tasks</span>
                </div>
                <p class="status-label" :class="matStats.remaining > 0 ? 'text-warning' : 'text-success'">
                  {{ matStats.remaining > 0 ? 'Tasks remaining' : 'All tasks completed' }}
                </p>
              </button>
            </div>
          </section>

          <section class="module-group">
            <h2 class="module-title">IK-Alcohol (Compliance)</h2>
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
                    <svg v-if="alcoholStatus && !alcoholWarning" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span v-else class="count-badge">!</span>
                  </div>
                </div>
                <p class="card-info">Age checks and shift incidents.</p>
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
                  <h3>Open Deviations</h3>
                  <div class="card-status-icon">
                    <span v-if="alcoholDeviations > 0" class="count-badge">{{ alcoholDeviations }}</span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
                <p class="card-info">Compliance and serving issues.</p>
                <p :class="alcoholDeviations > 0 ? 'text-danger' : 'text-success'" class="status-label">
                  {{ alcoholDeviations > 0 ? 'Follow up needed' : 'No open issues' }}
                </p>
              </button>

              <button
                type="button"
                class="stat-card clickable-card"
                :class="alcoholStats.remaining > 0 ? 'status-warning' : 'status-success'"
                @click="router.push('/checklists')"
                :aria-label="`Bar routines. ${alcoholStats.completed} of ${alcoholStats.total} tasks completed. Open checklists.`"
              >
                <div class="card-header">
                  <h3>Bar Routines</h3>
                  <div class="card-status-icon">
                    <span v-if="alcoholStats.remaining > 0" class="count-badge warn">{{ alcoholStats.remaining }}</span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
                <div class="progress-row">
                  <span class="big-num">{{ alcoholStats.completed }}</span><span class="total-num">/ {{ alcoholStats.total }} tasks</span>
                </div>
                <p class="status-label" :class="alcoholStats.remaining > 0 ? 'text-warning' : 'text-success'">
                  {{ alcoholStats.remaining > 0 ? 'Tasks remaining' : 'All tasks completed' }}
                </p>
              </button>
            </div>
          </section>
        </div>
      </template>
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

const hasTemperatureDeviations = computed(() => {
  return temperatureSummary.value && temperatureSummary.value.deviationsToday > 0
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

async function loadAlcoholStatus() {
  try {
    const res = await alcoholApi.getAlcoholStatus()
    alcoholStatus.value = res.data.hasLogs
  } catch (e) {
    console.error('Failed to load alcohol status:', e)
  }
}

async function loadAlcoholWarning() {
  try {
    const shiftId = 1
    const response = await alcoholApi.getMissingRegistrationWarning(shiftId)
    alcoholWarning.value = response.data.missing ? response.data.message : ''
  } catch (e) {
    console.error('Failed to load alcohol warning:', e)
  }
}

async function loadTemperatureData() {
  try {
    const summaryResponse = await temperatureApi.getTemperatureSummary()
    temperatureSummary.value = summaryResponse.data
  } catch (e) {
    console.error('Failed to load temperature summary:', e)
  }

  try {
    const deviationsResponse = await temperatureApi.getLatestDeviations(5)
    latestTemperatureDeviations.value = deviationsResponse.data
  } catch (e) {
    console.error('Failed to load latest temperature deviations:', e)
  }
}

async function goToAlcohol() {
  await router.push('/alcohol')
}

async function goToTemperature() {
  await router.push('/temperature')
}

onMounted(async () => {
  try {
    await Promise.all([
      loadAlcoholStatus(),
      loadAlcoholWarning(),
      loadTemperatureData(),
    ])

    const [deviationRes, checklistRes] = await Promise.all([
      deviationApi.getAll(),
      checklistApi.getAll(),
    ])

    deviations.value = Array.isArray(deviationRes.data) ? deviationRes.data : []
    checklists.value = Array.isArray(checklistRes.data) ? checklistRes.data : []
  } catch (e) {
    error.value = 'Failed to load dashboard data.'
    console.error(e)
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

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
  padding: 0 8px;
}

.welcome-text h1 {
  font-size: 1.75rem;
  color: #3C3489;
  margin-bottom: 4px;
  font-weight: 800;
}

.welcome-text p {
  color: #4b5563;
  font-size: 1rem;
}

.date-display {
  color: #5a529f;
  font-weight: 600;
  font-size: 0.95rem;
  text-transform: capitalize;
}

.module-group {
  margin-bottom: 48px;
}

.module-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #534AB7;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 20px;
  padding-left: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e0dfd8;
  border-left: 6px solid #d1d5db;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  min-height: 160px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  text-align: left;
  width: 100%;
}

button.stat-card {
  appearance: none;
  -webkit-appearance: none;
}

.status-success { border-left-color: #10b981; }
.status-warning { border-left-color: #f59e0b; }
.status-danger  { border-left-color: #ef4444; }

.clickable-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.clickable-card:hover {
  transform: translateY(-4px);
  background: #fcfcfb;
  box-shadow: 0 12px 24px rgba(60, 52, 137, 0.08);
  border-color: #7F77DD;
}

.clickable-card:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #3C3489;
  margin: 0;
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

.count-badge.warn {
  background: #b45309;
  color: white;
}

.card-info {
  font-size: 0.95rem;
  color: #4b5563;
  margin-bottom: auto;
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

.dot-success {
  background: #166534;
}

.dot-danger {
  background: #991b1b;
}

.small-text {
  color: #374151;
  font-weight: 600;
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
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .welcome-text h1 {
    font-size: 1.5rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>