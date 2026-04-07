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
        <table class="desktop-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Equipment</th>
              <th>Min/Max</th>
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
              <td>{{ log.equipment?.name || 'Unknown' }}</td>
              <td>{{ log.equipment?.minTemp ?? '-' }} / {{ log.equipment?.maxTemp ?? '-' }} °C</td>
              <td><strong>{{ log.value }} °C</strong></td>
              <td>
                <span :class="isLogDeviation(log) ? 'status-tag dev' : 'status-tag ok'">
                  {{ isLogDeviation(log) ? 'Deviation' : 'OK' }}
                </span>
              </td>
              <td>{{ log.loggedBy?.email?.split('@')[0] || 'User' }}</td>
            </tr>
            <tr v-if="history.length === 0">
              <td colspan="6">No temperature logs found.</td>
            </tr>
          </tbody>
        </table>

        <div class="mobile-log-cards">
          <div 
            v-for="log in history" 
            :key="log.id" 
            class="mobile-log-card" 
            :class="{ 'deviation-card-mobile': isLogDeviation(log) }"
          >
            <div class="mlc-row">
              <span class="mlc-label">Timestamp</span>
              <span>{{ formatDate(log.timestamp) }}</span>
            </div>
            <div class="mlc-row">
              <span class="mlc-label">Equipment</span>
              <strong>{{ log.equipment?.name }}</strong>
            </div>
            <div class="mlc-row">
              <span class="mlc-label">Reading</span>
              <span :class="{ 'text-error': isLogDeviation(log) }">
                {{ log.value }} °C (Limits: {{ log.equipment?.minTemp }}/{{ log.equipment?.maxTemp }})
              </span>
            </div>
            <div class="mlc-row">
              <span class="mlc-label">Status</span>
              <span :class="isLogDeviation(log) ? 'text-error' : 'text-success'">
                {{ isLogDeviation(log) ? '⚠ Deviation' : '✓ OK' }}
              </span>
            </div>
            <div class="mlc-row">
              <span class="mlc-label">Logged By</span>
              <span>{{ log.loggedBy?.email }}</span>
            </div>
          </div>
          <div v-if="history.length === 0" class="empty-state">
            No temperature logs found.
          </div>
        </div>
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
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

/* Responsiv Grid for Filter */
.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin: 20px 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
}

input, select {
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.95rem;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

/* Tabell-styling */
.table-wrapper { width: 100%; }

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #f9fafb;
  padding: 12px;
  text-align: left;
  font-size: 0.85rem;
  color: #6b7280;
  border-bottom: 2px solid #edf2f7;
}

td {
  padding: 14px 12px;
  border-bottom: 1px solid #edf2f7;
  font-size: 0.9rem;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}
.status-tag.ok { background: #ecfdf5; color: #059669; }
.status-tag.dev { background: #fef2f2; color: #dc2626; }

.text-error { color: #dc2626; font-weight: 600; }
.text-success { color: #059669; }

/* Mobil-kort logikk */
.mobile-log-cards { display: none; }

@media (max-width: 900px) {
  .filters {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .filters {
    grid-template-columns: 1fr;
  }
  
  .filter-actions {
    flex-direction: column;
  }

  .primary-btn, .secondary-btn, .back-btn {
    width: 100%;
    justify-content: center;
  }

  /* Skjul tabell, vis kort */
  .desktop-table { display: none; }
  
  .mobile-log-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-log-card {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    padding: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  }

  .deviation-card-mobile {
    border-left: 4px solid #dc2626;
  }

  .mlc-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f9fafb;
  }
  
  .mlc-row:last-child { border-bottom: none; }
}

.primary-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.secondary-btn {
  background: white;
  border: 1px solid #d1d5db;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
</style>