<template>
  <div class="temperature-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Temperature Registration</h1>
        <p class="subtitle">Register and monitor equipment readings</p>
      </div>
      <button type="button" class="back-btn-minimal" @click="goToDashboard">← Dashboard</button>
    </header>

    <LoadingSpinner v-if="initialLoading" message="Loading equipment..." />

    <div v-else class="temperature-grid">
      <section class="stat-card form-card" :class="{ 'status-danger': isDeviation && temperatureValue !== null }">
        <div class="card-header">
          <h3>New Reading</h3>
        </div>

        <div class="form-group">
          <label for="equipment">Equipment</label>
          <select id="equipment" v-model="selectedEquipmentId" :aria-invalid="errorMessage && !selectedEquipmentId ? 'true' : 'false'" :aria-describedby="errorMessage && !selectedEquipmentId ? 'temperature-form-error' : undefined">
            <option disabled value="">Select equipment</option>
            <option v-for="equipment in equipmentList" :key="equipment.id" :value="equipment.id">
              {{ equipment.name }}
            </option>
          </select>
        </div>

        <div v-if="selectedEquipment" class="info-tile">
          <span class="info-label">Allowed range:</span>
          <span class="info-value">{{ selectedEquipment.minTemp }}°C to {{ selectedEquipment.maxTemp }}°C</span>
        </div>

        <div class="form-group">
          <label for="temperature">Temperature (°C)</label>
          <input
            id="temperature"
            v-model.number="temperatureValue"
            type="number"
            step="0.1"
            placeholder="e.g. 3.5"
            :aria-invalid="errorMessage && (temperatureValue === null || temperatureValue === '' || temperatureValue < -50 || temperatureValue > 100) ? 'true' : 'false'"
            :aria-describedby="errorMessage ? 'temperature-form-error' : undefined"
          />
        </div>

        <div
          v-if="selectedEquipment && temperatureValue !== null && temperatureValue !== ''"
          :class="['alert-indicator', isDeviation ? 'danger' : 'success']"
        >
          <span class="icon">{{ isDeviation ? '⚠' : '✓' }}</span>
          <span>{{ isDeviation ? 'This reading is a deviation.' : 'Reading is within range.' }}</span>
        </div>

        <p v-if="errorMessage" id="temperature-form-error" class="error-inline" role="alert"> <span aria-hidden="true">⚠ </span>{{ errorMessage }} </p>
        <p v-if="successMessage" class="success-inline" role="status"> {{ successMessage }} </p>

        <button type="button" class="save-btn" @click="submitReading" :disabled="loading">
          {{ loading ? 'Saving...' : 'Save Reading' }}
        </button>
      </section>

      <section class="stat-card table-card">
        <div class="card-header">
          <h3>Latest Readings</h3>
          <button type="button" class="text-link-btn" @click="goToHistory">Full history →</button>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th scope="col">Time</th>
                <th scope="col">Equipment</th>
                <th scope="col">Value</th>
                <th scope="col">Status</th>
                <th scope="col">User</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in latestLogs" :key="log.id" :class="{ 'row-dev': isLogDeviation(log) }">
                <td class="time-cell">{{ formatDate(log.timestamp).split(',')[1] }}</td>
                <td class="equip-cell">{{ log.equipment?.name }}</td>
                <td><strong>{{ log.value }}°C</strong></td>
                <td>
                  <span :class="isLogDeviation(log) ? 'tag-danger' : 'tag-success'">
                    {{ isLogDeviation(log) ? 'Deviation' : 'OK' }}
                  </span>
                </td>
                <td class="user-cell">{{ log.loggedBy?.email?.split('@')[0] || '-' }}</td>
              </tr>
              <tr v-if="latestLogs.length === 0">
                <td colspan="5" class="empty-row">No readings yet.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import temperatureApi from '@/api/temperature'
import equipmentApi from '@/api/equipment'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()

const equipmentList = ref([])
const latestLogs = ref([])

const selectedEquipmentId = ref('')
const temperatureValue = ref(null)

const initialLoading = ref(true)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const selectedEquipment = computed(() => {
  return equipmentList.value.find((equipment) => equipment.id === selectedEquipmentId.value)
})

const isDeviation = computed(() => {
  if (!selectedEquipment.value) return false
  if (temperatureValue.value === null || temperatureValue.value === '') return false

  return (
    temperatureValue.value < selectedEquipment.value.minTemp ||
    temperatureValue.value > selectedEquipment.value.maxTemp
  )
})

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
    equipmentList.value = response.data || []
  } catch (error) {
    console.error('Failed to load equipment:', error)
    errorMessage.value = 'Failed to load equipment list.'
  }
}

async function fetchLatestLogs() {
  try {
    const response = await temperatureApi.getLatestTemperatureLogs(10)
    latestLogs.value = response.data || []
  } catch (error) {
    console.error('Failed to load latest temperature logs:', error)
  }
}

function resetForm() {
  selectedEquipmentId.value = ''
  temperatureValue.value = null
}

function goToDashboard() {
  router.push('/')
}

function validateForm() {
  if (!selectedEquipmentId.value) {
    errorMessage.value = 'Please select equipment.'
    return false
  }

  if (temperatureValue.value === null || temperatureValue.value === '') {
    errorMessage.value = 'Please enter a temperature value.'
    return false
  }

  if (temperatureValue.value < -50 || temperatureValue.value > 100) {
    errorMessage.value = 'Temperature must be between -50 and 100 °C.'
    return false
  }

  return true
}

async function submitReading() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateForm()) return

  loading.value = true

  try {
    await temperatureApi.createTemperatureLog(
      selectedEquipmentId.value,
      Number(temperatureValue.value),
    )

    successMessage.value = isDeviation.value
      ? 'Temperature reading saved. A deviation was automatically detected.'
      : 'Temperature reading saved successfully.'

    resetForm()
    await fetchLatestLogs()
  } catch (error) {
    console.error('Failed to save temperature reading:', error)
    errorMessage.value = error.response?.data?.message || 'Failed to save temperature reading.'
  } finally {
    loading.value = false
  }
}

function goToHistory() {
  router.push('/temperature-history')
}

onMounted(async () => {
  await Promise.all([fetchEquipment(), fetchLatestLogs()])
  initialLoading.value = false
})
</script>

<style scoped>
.temperature-page {
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

.header-main h1 { font-size: 2rem; font-weight: 800; color: #3C3489; margin-bottom: 4px; }
.subtitle { color: #5a529f; font-weight: 600; font-size: 0.95rem; }

button:focus-visible,
input:focus-visible,
select:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

input:focus,
select:focus {
  outline: none;
  border-color: #534AB7;
  background: white;
}

.back-btn-minimal {
  background: transparent; color: #534AB7; border: 1.5px solid #e0dfd8;
  padding: 10px 16px; border-radius: 10px; font-weight: 700; cursor: pointer;
}

.temperature-grid {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 24px;
  align-items: start;
}

.form-card,
.table-card,
.stat-card {
  min-width: 0;
}

.stat-card {
  background: white; border: 1px solid #e0dfd8; border-left: 6px solid #534AB7;
  border-radius: 14px; padding: 24px; box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
}

.stat-card.status-danger { border-left-color: #ef4444; }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-header h3 { color: #3C3489; font-weight: 800; font-size: 1.2rem; }

.form-group { display: flex; flex-direction: column; margin-bottom: 20px; }
.form-group label { font-weight: 700; color: #3C3489; font-size: 0.9rem; margin-bottom: 8px; }

input, select {
  padding: 12px; border: 1.5px solid #e0dfd8; border-radius: 10px;
  font-size: 16px; background-color: #fafaf8; width: 100%;
}

.info-tile {
  background: #f5f4ff; padding: 12px; border-radius: 10px; margin-bottom: 20px;
  display: flex; gap: 8px; font-size: 0.9rem;
}

.alert-indicator { padding: 12px; border-radius: 10px; font-weight: 700; font-size: 0.9rem; margin-bottom: 20px; }
.alert-indicator.success { background: #ecfdf5; color: #166534; }
.alert-indicator.danger { background: #fff5f5; color: #991b1b; }

.save-btn {
  background: #534AB7; color: white; border: none; padding: 14px;
  border-radius: 10px; font-weight: 700; width: 100%; cursor: pointer;
}

.user-cell {
  color: #4b5563;
  font-size: 0.9rem;
}

.text-link-btn { background: none; border: none; color: #534AB7; font-weight: 700; cursor: pointer; }

.table-container { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; }
table { width: 100%; border-collapse: collapse; min-width: 400px; }
th { text-align: left; padding: 12px; color: #4b5563; font-size: 0.8rem; text-transform: uppercase; border-bottom: 2px solid #e5e7eb; }
td { padding: 14px 12px; border-bottom: 1px solid #f0f0f0; font-size: 0.95rem; }

.tag-success { color: #166534; font-weight: 800; }
.tag-danger { color: #991b1b; font-weight: 800; }
.row-dev { background-color: #fffafa; }
.equip-cell { font-weight: 700; color: #3C3489; }
.time-cell { color: #4b5563; font-size: 0.85rem; }

.error-inline { color: #b91c1c; font-weight: 700; font-size: 0.9rem; margin-bottom: 12px; }
.success-inline { color: #166534; font-weight: 700; font-size: 0.9rem; margin-bottom: 12px; }

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 768px) {
  .temperature-page {
    padding: 16px 12px;
  }

  .page-header-section {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-main h1 {
    font-size: 1.6rem;
    line-height: 1.2;
  }

  .back-btn-minimal {
    width: 100%;
    text-align: center;
  }

  .temperature-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    padding: 18px 14px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-tile {
    flex-direction: column;
    gap: 4px;
  }

  table {
    min-width: 520px;
  }
}
</style>