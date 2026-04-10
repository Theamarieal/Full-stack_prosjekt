<template>
  <div class="history-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Temperature History</h1>
        <p class="subtitle">Filter and review all quality control logs</p>
      </div>
      <button type="button" class="back-btn-minimal" @click="goToTemperature">← Registration</button>
    </header>

    <section class="stat-card filter-section">
      <div class="card-header">
        <h3>Filter Records</h3>
        <button type="button" class="reset-link-btn" @click="resetFilters">Reset filters</button>
      </div>

      <div class="filters-grid">
        <div class="form-group">
          <label for="equipment">Equipment</label>
          <select id="equipment" v-model="filters.equipmentId">
            <option value="">All equipment</option>
            <option v-for="equipment in equipmentList" :key="equipment.id" :value="String(equipment.id)">
              {{ equipment.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="fromDate">From date</label>
          <input id="fromDate" v-model="filters.fromDate" type="date" />
        </div>

        <div class="form-group">
          <label for="toDate">To date</label>
          <input id="toDate" v-model="filters.toDate" type="date" />
        </div>

        <div class="form-group">
          <label for="status">Status</label>
          <select id="status" v-model="filters.status">
            <option value="">All statuses</option>
            <option value="OK">OK</option>
            <option value="DEVIATION">Deviation</option>
          </select>
        </div>
      </div>

      <button type="button" class="apply-btn" @click="fetchHistory">Apply Filters</button>
    </section>

    <section class="stat-card history-card">
      <div class="card-header">
        <h3>Result Log</h3>
        <span class="results-count">{{ history.length }} entries found</span>
      </div>

      <div class="table-container">
        <table class="history-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Equipment</th>
              <th>Limits</th>
              <th>Reading</th>
              <th>Status</th>
              <th>User</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in history" :key="log.id" :class="{ 'row-dev': isLogDeviation(log) }">
              <td class="time-cell">{{ formatDate(log.timestamp) }}</td>
              <td class="equip-cell">{{ log.equipment?.name || 'Unknown' }}</td>
              <td class="limit-cell">{{ log.equipment?.minTemp }} / {{ log.equipment?.maxTemp }}°C</td>
              <td class="val-cell"><strong>{{ log.value }}°C</strong></td>
              <td>
                <span :class="isLogDeviation(log) ? 'tag-danger' : 'tag-success'">
                  {{ isLogDeviation(log) ? 'Deviation' : 'OK' }}
                </span>
              </td>
              <td class="user-cell">{{ log.loggedBy?.email?.split('@')[0] }}</td>
            </tr>
            <tr v-if="history.length === 0">
              <td colspan="6" class="empty-row">No records match your filters.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
/**
 * TemperatureHistoryView
 *
 * Displays historical temperature log entries with optional filtering
 * by equipment and date range. Part of the IK-Mat food safety module.
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import temperatureApi from '@/api/temperature'
import equipmentApi from '@/api/equipment'

const equipmentList = ref([])
const history = ref([])
const filters = ref({
  equipmentId: '',
  fromDate: '',
  toDate: '',
  status: '',
})

const router = useRouter()
const goToTemperature = () => router.push('/temperature')
const isLogDeviation = (log) => log?.isDeviation ?? log?.deviation ?? false

function formatDate(dateTime) {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleDateString('en-GB') + ' ' + date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
}

async function fetchEquipment() {
  try {
    const response = await equipmentApi.getAllEquipment()
    equipmentList.value = response.data
  } catch (err) { console.error(err) }
}

async function fetchHistory() {
  try {
    const params = Object.fromEntries(
      Object.entries(filters.value).map(([k, v]) => [k, v || undefined])
    )
    const response = await temperatureApi.getTemperatureHistory(params)
    history.value = response.data
  } catch (err) { console.error(err) }
}

async function resetFilters() {
  filters.value = { equipmentId: '', fromDate: '', toDate: '', status: '' }
  await fetchHistory()
}

onMounted(() => {
  fetchEquipment()
  fetchHistory()
})
</script>

<style scoped>
.history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
  min-height: 100vh;
  background-color: #f7f6f2;
}

.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-main h1 {
  font-size: 2rem;
  font-weight: 800;
  color: #3C3489;
  margin-bottom: 4px;
}

.subtitle {
  color: #5a529f;
  font-weight: 600;
  font-size: 0.95rem;
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

.stat-card {
  background: white;
  border: 1px solid #e0dfd8;
  border-left: 6px solid #534AB7;
  border-radius: 14px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  color: #3C3489;
  font-weight: 800;
  font-size: 1.2rem;
  margin: 0;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 700;
  margin-bottom: 8px;
  color: #3C3489;
  font-size: 0.85rem;
}

input, select {
  padding: 12px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  font-size: 1rem;
  background-color: #fafaf8;
  color: #2c2c2a;
}

.apply-btn {
  background: #534AB7;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;
}

.apply-btn:hover { opacity: 0.9; }

.reset-link-btn {
  background: none;
  border: none;
  color: #7F77DD;
  font-weight: 700;
  font-size: 0.85rem;
  text-decoration: underline;
  cursor: pointer;
}

.table-container {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

th {
  text-align: left;
  padding: 12px;
  color: #4b5563;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #f0f0f0;
}

td { padding: 16px 12px; border-bottom: 1px solid #f0f0f0; font-size: 0.95rem; }

.results-count { font-size: 0.85rem; color: #999; font-weight: 600; }
.time-cell { color: #666; font-size: 0.85rem; white-space: nowrap; }
.equip-cell { font-weight: 700; color: #3C3489; }
.limit-cell { color: #999; font-size: 0.85rem; white-space: nowrap; }

.tag-success { color: #166534; font-weight: 800; }
.tag-danger { color: #991b1b; font-weight: 800; }
.row-dev { background-color: #fffafa; }

.empty-row { text-align: center; color: #999; padding: 40px; font-weight: 600; }

button:focus-visible,
input:focus-visible,
select:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

@media (max-width: 350px) {
  .page-header-section { flex-direction: column; gap: 16px; }
  .back-btn-minimal { width: 100%; text-align: center; }
}
</style>
