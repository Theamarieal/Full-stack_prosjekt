<template>
  <div class="history-page">
    <div class="top-actions">
      <button type="button" class="back-btn" @click="goToTemperature">
        ← Back to temperature registration
      </button>
    </div>
    <div class="page-header">
      <h1>Temperature History</h1>
      <p>Filter and review all registered temperature readings.</p>
    </div>

    <section class="card filter-card">
      <h2>Filters</h2>

      <div class="filters">
        <div class="form-group">
          <label for="equipment">Equipment</label>
          <select id="equipment" v-model="filters.equipmentId">
            <option value="">All equipment</option>
            <option
              v-for="equipment in equipmentList"
              :key="equipment.id"
              :value="String(equipment.id)"
            >
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
            <option value="">All</option>
            <option value="OK">OK</option>
            <option value="DEVIATION">Deviation</option>
          </select>
        </div>
      </div>

      <div class="filter-actions">
        <button class="primary-btn" @click="fetchHistory">Apply Filters</button>
        <button class="secondary-btn" @click="resetFilters">Reset</button>
      </div>
    </section>

    <section class="card history-card">
      <div class="table-header">
        <h2>All Temperature Logs</h2>
        <span class="results-count">{{ history.length }} result(s)</span>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Equipment</th>
              <th>Min Temp</th>
              <th>Max Temp</th>
              <th>Reading</th>
              <th>Status</th>
              <th>Logged By</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="log in history"
              :key="log.id"
              :class="{ 'deviation-row': isLogDeviation(log) }"
            >
              <td>{{ formatDate(log.timestamp) }}</td>
              <td>{{ log.equipment?.name || 'Unknown equipment' }}</td>
              <td>{{ log.equipment?.minTemp ?? '-' }} °C</td>
              <td>{{ log.equipment?.maxTemp ?? '-' }} °C</td>
              <td>{{ log.value }} °C</td>
              <td>{{ isLogDeviation(log) ? 'Deviation' : 'OK' }}</td>
              <td>{{ log.loggedBy?.email || 'Unknown user' }}</td>
            </tr>

            <tr v-if="history.length === 0">
              <td colspan="7">No temperature logs found.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
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

function goToTemperature() {
  router.push('/temperature')
}

function isLogDeviation(log) {
  return log?.isDeviation ?? log?.deviation ?? false
}

function formatDate(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('en-GB')
}

async function fetchEquipment() {
  try {
    const response = await equipmentApi.getAllEquipment()
    equipmentList.value = response.data
  } catch (error) {
    console.error('Failed to load equipment:', error)
  }
}

async function fetchHistory() {
  try {
    const params = {
      equipmentId: filters.value.equipmentId || undefined,
      fromDate: filters.value.fromDate || undefined,
      toDate: filters.value.toDate || undefined,
      status: filters.value.status || undefined,
    }

    const response = await temperatureApi.getTemperatureHistory(params)
    history.value = response.data
  } catch (error) {
    console.error('Failed to load temperature history:', error)
  }
}

async function resetFilters() {
  filters.value = {
    equipmentId: '',
    fromDate: '',
    toDate: '',
    status: '',
  }

  await fetchHistory()
}

onMounted(async () => {
  await Promise.all([fetchEquipment(), fetchHistory()])
})
</script>

<style scoped>
.history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #555;
}

.card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 24px;
}

.filter-card h2,
.history-card h2 {
  margin-top: 0;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 6px;
}

.form-group input,
.form-group select {
  padding: 10px 12px;
  border: 1px solid #d8dce2;
  border-radius: 8px;
  font-size: 14px;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.primary-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.secondary-btn {
  background: #f3f4f6;
  color: #111827;
  border: 1px solid #d1d5db;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.results-count {
  color: #666;
  font-size: 0.95rem;
}

.table-wrapper {
  overflow-x: auto;
}

.top-actions {
  margin-bottom: 16px;
}

.back-btn {
  background: transparent;
  color: #1f2937;
  border: 1px solid #d1d5db;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.back-btn:hover {
  background: #f3f4f6;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #eceff3;
  vertical-align: top;
}

.deviation-row {
  background: #fff5f5;
}

@media (max-width: 900px) {
  .filters {
    grid-template-columns: 1fr;
  }

  .history-page {
    padding: 16px;
  }
}
</style>
