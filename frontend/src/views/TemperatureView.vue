<template>
  <div class="temperature-page">
    <div class="top-actions">
      <button type="button" class="back-btn" @click="goToDashboard">← Back to dashboard</button>
    </div>
    <div class="page-header">
      <h1>Temperature Registration</h1>
      <p>Register temperature readings for your equipment.</p>
    </div>

    <div class="temperature-grid">
      <section class="card form-card">
        <h2>New Reading</h2>

        <div class="form-group">
          <label for="equipment">Equipment</label>
          <select id="equipment" v-model="selectedEquipmentId">
            <option disabled value="">Select equipment</option>
            <option v-for="equipment in equipmentList" :key="equipment.id" :value="equipment.id">
              {{ equipment.name }}
            </option>
          </select>
        </div>

        <div v-if="selectedEquipment" class="rule-box">
          <p><strong>Allowed range:</strong></p>
          <p>
            Min: {{ selectedEquipment.minTemp }} °C
            <span class="separator">|</span>
            Max: {{ selectedEquipment.maxTemp }} °C
          </p>
        </div>

        <div class="form-group">
          <label for="temperature">Temperature (°C)</label>
          <input
            id="temperature"
            v-model.number="temperatureValue"
            type="number"
            step="0.1"
            placeholder="Enter measured temperature"
          />
        </div>

        <div
          v-if="selectedEquipment && temperatureValue !== null && temperatureValue !== ''"
          :class="['status-box', isDeviation ? 'deviation' : 'ok']"
        >
          {{
            isDeviation
              ? 'This reading is a deviation.'
              : 'This reading is within the allowed range.'
          }}
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>

        <button type="button" class="primary-btn" @click="submitReading" :disabled="loading">
          {{ loading ? 'Saving...' : 'Save Reading' }}
        </button>
      </section>

      <section class="card table-card">
        <div class="table-header">
          <h2>Latest Readings</h2>
          <button type="button" class="secondary-btn" @click="goToHistory">
            View full history
          </button>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Equipment</th>
                <th>Value</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="log in latestLogs"
                :key="log.id"
                :class="{ 'deviation-row': isLogDeviation(log) }"
              >
                <td>{{ formatDate(log.timestamp) }}</td>
                <td>{{ log.equipment?.name || 'Unknown equipment' }}</td>
                <td>{{ log.value }} °C</td>
                <td>{{ isLogDeviation(log) ? 'Deviation' : 'OK' }}</td>
              </tr>

              <tr v-if="latestLogs.length === 0">
                <td colspan="4">No temperature readings found.</td>
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

const router = useRouter()

const equipmentList = ref([])
const latestLogs = ref([])

const selectedEquipmentId = ref('')
const temperatureValue = ref(null)

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
    console.log('Equipment response:', response.data)
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
  await fetchEquipment()
  await fetchLatestLogs()
})
</script>

<style scoped>
.temperature-page {
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

.temperature-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(420px, 1.15fr);
  gap: 24px;
  align-items: start;
}

.card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 20px;
}

.form-card {
  align-self: start;
}

.table-card {
  align-self: start;
}

.form-card h2,
.table-card h2 {
  margin-top: 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
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

.rule-box {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.rule-box p {
  margin: 0;
}

.separator {
  margin: 0 8px;
  color: #999;
}

.status-box {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 8px;
  font-weight: 600;
}

.status-box.ok {
  background: #ecfdf3;
  color: #067647;
  border: 1px solid #abefc6;
}

.status-box.deviation {
  background: #fef3f2;
  color: #b42318;
  border: 1px solid #fecdca;
}

.error-message {
  color: #b42318;
  margin-bottom: 12px;
  font-weight: 600;
}

.success-message {
  color: #067647;
  margin-bottom: 12px;
  font-weight: 600;
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

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  background: #f3f4f6;
  color: #111827;
  border: 1px solid #d1d5db;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.secondary-btn:hover {
  background: #e5e7eb;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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
}

.deviation-row {
  background: #fff5f5;
}

@media (max-width: 900px) {
  .temperature-grid {
    grid-template-columns: 1fr;
  }

  .temperature-page {
    padding: 16px;
  }
}
</style>
