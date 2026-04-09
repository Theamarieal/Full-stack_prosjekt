<template>
  <div class="report-view">
    <div class="page-header">
      <div class="page-header-content">
        <div class="page-header-text">
          <h1>Reports</h1>
          <p>Generate a compliance report for a selected date range.</p>
        </div>
      
        <button type="button" class="back-btn-minimal" @click="router.push('/')">
          ← Dashboard
        </button>
      </div>
    </div>

    <section class="card filter-card">
      <div class="filter-grid">
        <div class="form-group">
          <label for="from">From</label>
          <input id="from" v-model="from" type="date" />
        </div>

        <div class="form-group">
          <label for="to">To</label>
          <input id="to" v-model="to" type="date" />
        </div>
      </div>

      <div class="action-row">
        <button class="primary-button" @click="loadReport" :disabled="loading">
          {{ loading ? 'Loading...' : 'Preview report' }}
        </button>

        <button class="secondary-button" @click="downloadJson" :disabled="!report">
          Download JSON
        </button>
      </div>

      <p v-if="error" class="error-text">{{ error }}</p>
    </section>

    <template v-if="report">
      <section class="card overview-card">
        <div class="overview-header">
          <div>
            <h2>Report overview</h2>
            <p class="muted-text">
              {{ report.organizationName }} · {{ formatDisplayDate(report.from) }} to
              {{ formatDisplayDate(report.to) }}
            </p>
          </div>
        </div>

        <div class="summary-grid">
          <div class="summary-card">
            <p class="summary-label">Total deviations</p>
            <p class="summary-value">{{ report.summary.totalDeviations }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Open deviations</p>
            <p class="summary-value warning-text">{{ report.summary.openDeviations }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Resolved deviations</p>
            <p class="summary-value ok-text">{{ report.summary.resolvedDeviations }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Temperature logs</p>
            <p class="summary-value">{{ report.summary.totalTemperatureLogs }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Temperature deviations</p>
            <p class="summary-value">{{ report.summary.temperatureDeviations }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Alcohol logs</p>
            <p class="summary-value">{{ report.summary.totalAlcoholLogs }}</p>
          </div>

          <div class="summary-card">
            <p class="summary-label">Alcohol deviation events</p>
            <p class="summary-value">{{ report.summary.alcoholDeviationEvents }}</p>
          </div>
        </div>
      </section>

      <div class="report-sections">
        <section class="card report-section">
          <div class="section-header">
            <h2>Deviations</h2>
            <span class="section-count">{{ report.deviations?.length || 0 }}</span>
          </div>

          <div v-if="!report.deviations || report.deviations.length === 0" class="empty-state">
            No deviations found for this period.
          </div>

          <div v-else class="report-list">
            <article
              v-for="deviation in report.deviations"
              :key="deviation.id"
              class="report-list-item"
            >
              <div class="item-top-row">
                <div>
                  <h3>{{ deviation.title }}</h3>
                  <p class="item-meta">
                    {{ deviation.module }} · {{ formatDateTime(deviation.createdAt) }}
                  </p>
                </div>

                <span
                  class="status-badge"
                  :class="deviation.status === 'OPEN' ? 'badge-open' : 'badge-resolved'"
                >
                  {{ deviation.status }}
                </span>
              </div>

              <p class="item-description">
                {{ deviation.description || 'No description provided.' }}
              </p>
            </article>
          </div>
        </section>

        <section class="card report-section">
          <div class="section-header">
            <h2>Temperature deviation logs</h2>
            <span class="section-count">{{ report.temperatureDeviationLogs?.length || 0 }}</span>
          </div>

          <div
            v-if="!report.temperatureDeviationLogs || report.temperatureDeviationLogs.length === 0"
            class="empty-state"
          >
            No temperature deviations found for this period.
          </div>

          <div v-else class="report-list">
            <article
              v-for="log in report.temperatureDeviationLogs"
              :key="log.id"
              class="report-list-item"
            >
              <div class="item-top-row">
                <div>
                  <h3>{{ log.equipment?.name || 'Unknown equipment' }}</h3>
                  <p class="item-meta">
                    {{ formatDateTime(log.timestamp) }}
                  </p>
                </div>

                <span class="metric-pill">{{ log.value }} °C</span>
              </div>

              <p class="item-description">Logged by {{ log.loggedBy?.email || 'Unknown user' }}</p>
            </article>
          </div>
        </section>

        <section class="card report-section">
          <div class="section-header">
            <h2>Alcohol deviation events</h2>
            <span class="section-count">{{ report.alcoholDeviationEvents?.length || 0 }}</span>
          </div>

          <div
            v-if="!report.alcoholDeviationEvents || report.alcoholDeviationEvents.length === 0"
            class="empty-state"
          >
            No alcohol deviation events found for this period.
          </div>

          <div v-else class="report-list">
            <article
              v-for="log in report.alcoholDeviationEvents"
              :key="log.id"
              class="report-list-item"
            >
              <div class="item-top-row">
                <div>
                  <h3>{{ formatType(log.type) }}</h3>
                  <p class="item-meta">
                    {{ formatDateTime(log.recordedAt) }}
                  </p>
                </div>

                <span class="metric-pill">
                  {{ log.guestAge ?? '-' }} yrs · {{ log.alcoholPercentage ?? '-' }}%
                </span>
              </div>

              <p class="item-description">
                ID checked: {{ formatBoolean(log.idChecked) }} · Service denied:
                {{ formatBoolean(log.serviceDenied) }}
              </p>

              <p v-if="log.notes" class="item-notes">
                {{ log.notes }}
              </p>
            </article>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { fetchReport } from '@/api/report'
import { useRouter } from 'vue-router'

const router = useRouter()
const today = new Date().toISOString().split('T')[0]

const from = ref(today)
const to = ref(today)
const report = ref(null)
const loading = ref(false)
const error = ref('')

async function loadReport() {
  error.value = ''
  loading.value = true

  try {
    report.value = await fetchReport(from.value, to.value)
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Failed to load report.'
  } finally {
    loading.value = false
  }
}

function downloadJson() {
  if (!report.value) return

  const blob = new Blob([JSON.stringify(report.value, null, 2)], {
    type: 'application/json',
  })

  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `report-${from.value}-to-${to.value}.json`
  link.click()
  window.URL.revokeObjectURL(url)
}

function formatDisplayDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('en-GB')
}

function formatDateTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('en-GB')
}

function formatBoolean(value) {
  if (value === true) return 'Yes'
  if (value === false) return 'No'
  return '-'
}

function formatType(type) {
  switch (type) {
    case 'AGE_CHECK':
      return 'Age check'
    case 'SERVING_START':
      return 'Serving start'
    case 'SERVING_END':
      return 'Serving end'
    case 'BREAK':
      return 'Break'
    case 'INCIDENT':
      return 'Incident'
    default:
      return type || 'Unknown'
  }
}
</script>

<style scoped>
.report-view {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px 40px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-header-text {
  flex: 1;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 2.4rem;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 1rem;
}

.back-btn-minimal {
  background: transparent;
  color: #534AB7;
  border: 1.5px solid #e0dfd8;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.back-btn-minimal:hover {
  background: #fafaf8;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 1rem;
}

.card {
  background: #ffffff;
  border: 1px solid #dbe1e7;
  border-radius: 18px;
  padding: 24px;
}

.filter-card {
  margin-bottom: 24px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 280px));
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1f2937;
}

.form-group input {
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font: inherit;
  background: #ffffff;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary-button,
.secondary-button {
  border: none;
  border-radius: 10px;
  padding: 11px 18px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  min-width: 160px;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    background 0.15s ease;
}

.primary-button {
  background: #2563eb;
  color: #ffffff;
}

.primary-button:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.secondary-button {
  background: #e5e7eb;
  color: #111827;
}

.secondary-button:hover:not(:disabled) {
  background: #d1d5db;
  transform: translateY(-1px);
}

.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

.error-text {
  margin: 14px 0 0;
  color: #b91c1c;
  font-weight: 600;
}

.overview-card {
  margin-bottom: 24px;
}

.overview-header {
  margin-bottom: 20px;
}

.overview-header h2 {
  margin: 0 0 6px;
  font-size: 1.4rem;
}

.muted-text {
  margin: 0;
  color: #6b7280;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 14px;
}

.summary-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
  background: #f8fafc;
}

.summary-label {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 0.9rem;
}

.summary-value {
  margin: 0;
  font-size: 1.9rem;
  font-weight: 700;
  color: #111827;
}

.report-sections {
  display: grid;
  gap: 24px;
}

.report-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.section-header h2 {
  margin: 0;
  font-size: 1.3rem;
}

.section-count {
  min-width: 36px;
  height: 36px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.report-list-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
  background: #fcfcfd;
}

.item-top-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 10px;
}

.item-top-row h3 {
  margin: 0 0 4px;
  font-size: 1.05rem;
}

.item-meta {
  margin: 0;
  color: #6b7280;
  font-size: 0.92rem;
}

.item-description {
  margin: 0;
  color: #374151;
  line-height: 1.45;
}

.item-notes {
  margin: 10px 0 0;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  color: #374151;
}

.status-badge,
.metric-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.85rem;
  font-weight: 700;
}

.badge-open {
  background: #fee2e2;
  color: #b91c1c;
}

.badge-resolved {
  background: #dcfce7;
  color: #166534;
}

.metric-pill {
  background: #eff6ff;
  color: #1d4ed8;
}

.empty-state {
  color: #6b7280;
  padding: 8px 0;
}

.warning-text {
  color: #b91c1c;
}

.ok-text {
  color: #166534;
}

@media (max-width: 768px) {
  .report-view {
    padding: 24px 16px 32px;
  }

  .page-header-content {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .action-row {
    flex-direction: column;
  }

  .primary-button,
  .secondary-button,
  .back-btn-minimal {
    width: 100%;
  }

  .back-btn-minimal {
    text-align: center;
  }

  .item-top-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
