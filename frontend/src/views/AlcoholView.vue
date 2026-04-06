<template>
  <div class="alcohol-view">
    <div class="page-header">
      <div>
        <h1>Alcohol</h1>
        <p>Register alcohol-related routines for the current shift.</p>
      </div>
    </div>

    <div class="tab-bar">
      <button
        class="tab-button"
        :class="{ active: activeTab === 'register' }"
        @click="activeTab = 'register'"
      >
        Registration
      </button>

      <button
        class="tab-button"
        :class="{ active: activeTab === 'history' }"
        @click="activeTab = 'history'"
      >
        Current Shift History
      </button>
    </div>

    <div v-if="errorMessage" class="alert error">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="alert success">
      {{ successMessage }}
    </div>

    <section v-if="activeTab === 'register'" class="card">
      <h2>New Alcohol Registration</h2>

      <form class="form-grid" @submit.prevent="submitLog">
        <div class="form-group">
          <label for="type">Log type</label>
          <select id="type" v-model="form.type" required>
            <option value="AGE_CHECK">Age check</option>
            <option value="SERVING_START">Serving start</option>
            <option value="SERVING_END">Serving end</option>
            <option value="BREAK">Break</option>
            <option value="INCIDENT">Incident</option>
          </select>
        </div>

        <div class="form-group">
          <label for="recordedTime">Time</label>
          <input id="recordedTime" v-model="form.recordedTime" type="time" required />
          <p v-if="fieldErrors.recordedTime" class="field-error">
            {{ fieldErrors.recordedTime }}
          </p>
        </div>

        <div v-if="form.type === 'AGE_CHECK'" class="full-width conditional-section">
          <div class="form-grid">
            <div class="form-group">
              <label for="guestAge">Guest age</label>
              <input id="guestAge" v-model.number="form.guestAge" type="number" min="0" max="90" />
              <p v-if="fieldErrors.guestAge" class="field-error">
                {{ fieldErrors.guestAge }}
              </p>
            </div>

            <div class="form-group">
              <label for="alcoholPercentage">Alcohol percentage</label>
              <input
                id="alcoholPercentage"
                v-model.number="form.alcoholPercentage"
                type="number"
                min="0"
                max="60"
                step="0.1"
              />
              <p v-if="fieldErrors.alcoholPercentage" class="field-error">
                {{ fieldErrors.alcoholPercentage }}
              </p>
            </div>

            <div class="form-group checkbox-field">
              <label class="checkbox-row" for="idChecked">
                <span>ID checked</span>
                <input id="idChecked" type="checkbox" v-model="form.idChecked" />
              </label>
            </div>

            <div class="form-group checkbox-field">
              <label class="checkbox-row" for="serviceDenied">
                <span>Service denied</span>
                <input id="serviceDenied" type="checkbox" v-model="form.serviceDenied" />
              </label>
            </div>

            <p v-if="fieldErrors.idChecked" class="field-error">
              {{ fieldErrors.idChecked }}
            </p>

            <p v-if="fieldErrors.serviceDenied" class="field-error">
              {{ fieldErrors.serviceDenied }}
            </p>
          </div>

          <div v-if="requiresDenialNotes()" class="form-group full-width">
            <label for="denialNotes">Reason for denied service</label>
            <textarea
              id="denialNotes"
              v-model="form.notes"
              rows="4"
              placeholder="Explain why service was denied even though the age requirement was met."
            />
          </div>

          <div class="law-box">
            <strong>Age control rules</strong>
            <p>
              Guests must be at least 18 for alcohol below 22% and at least 20 for alcohol at 22% or
              above. If service is not legally allowed, it must be denied.
            </p>
          </div>
        </div>

        <div v-if="requiresDenialNotes()" class="form-group full-width">
          <label for="denialNotes">Reason for denied service</label>
          <textarea
            id="denialNotes"
            v-model="form.notes"
            rows="4"
            placeholder="Explain why service was denied even though the age requirement was met."
          />
          <p v-if="fieldErrors.notes" class="field-error">
            {{ fieldErrors.notes }}
          </p>
        </div>

        <div v-if="form.type === 'INCIDENT'" class="form-group full-width">
          <label for="notes">Incident notes</label>
          <textarea
            id="notes"
            v-model="form.notes"
            rows="5"
            placeholder="Describe the incident clearly."
          />
          <p v-if="fieldErrors.notes" class="field-error">
            {{ fieldErrors.notes }}
          </p>
        </div>

        <div class="info-box full-width">
          <strong>Recommended usage</strong>
          <p>
            Use <em>Age check</em> for ID control routines, <em>Serving start</em> and
            <em>Serving end</em> for serving hours, <em>Break</em> for pauses in serving, and
            <em>Incident</em> for alcohol-related deviations or events.
          </p>
        </div>

        <div class="actions full-width">
          <button type="submit" :disabled="isSaving">
            {{ isSaving ? 'Saving...' : 'Save registration' }}
          </button>
        </div>
      </form>
    </section>

    <section v-else class="card">
      <div class="history-header">
        <div>
          <h2>Current Shift History</h2>
          <p class="muted-text">Employees can only view alcohol logs for the active shift.</p>
        </div>
      </div>

      <div v-if="canSearchHistory" class="manager-tools">
        <div class="form-group">
          <label for="historyDate">Search by date</label>
          <input id="historyDate" v-model="historyDate" type="date" />
        </div>

        <div class="manager-actions">
          <button
            type="button"
            @click="loadHistoryByDate"
            :disabled="isLoadingHistory || !historyDate"
          >
            Load selected date
          </button>
          <button type="button" class="secondary-button" @click="loadHistory">
            Load current shift
          </button>
        </div>
      </div>

      <div v-if="isLoadingHistory" class="loading-state">Loading history...</div>

      <div v-else-if="history.length === 0" class="empty-state">No alcohol logs found.</div>

      <table v-else class="history-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>Type</th>
            <th>Notes</th>
            <th>Recorded by</th>
            <th v-if="canSearchHistory">Date</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in history" :key="log.id">
            <td>{{ formatTime(log.recordedAt) }}</td>
            <td>
              <div class="type-badge-list">
                <span
                  v-for="displayType in log.displayTypes"
                  :key="displayType"
                  class="type-badge"
                  :class="badgeClass(displayType)"
                >
                  {{ formatType(displayType) }}
                </span>
              </div>
            </td>
            <td>{{ log.notes || '-' }}</td>
            <td>{{ log.recordedBy || '-' }}</td>
            <td v-if="canSearchHistory">{{ formatDate(log.recordedAt) }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import alcoholApi from '@/api/alcohol'

const authStore = useAuthStore()

const activeTab = ref('register')
const isSaving = ref(false)
const isLoadingHistory = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const history = ref([])
const historyDate = ref('')

const form = ref({
  type: 'AGE_CHECK',
  recordedTime: getNowLocalTime(),
  notes: '',
  guestAge: null,
  alcoholPercentage: null,
  idChecked: false,
  serviceDenied: false,
})

const fieldErrors = ref({
  guestAge: '',
  alcoholPercentage: '',
  idChecked: '',
  serviceDenied: '',
  notes: '',
  recordedTime: '',
})

/**
 * Clears all field-specific validation messages.
 */
function clearFieldErrors() {
  fieldErrors.value = {
    guestAge: '',
    alcoholPercentage: '',
    idChecked: '',
    serviceDenied: '',
    notes: '',
    recordedTime: '',
  }
}

/**
 * Indicates whether the current user can search historical dates.
 *
 * @type {import('vue').ComputedRef<boolean>}
 */
const canSearchHistory = computed(() => {
  const role = authStore.user?.role
  return role === 'MANAGER' || role === 'ADMIN'
})

/**
 * Returns the current local time formatted for a time input.
 *
 * @returns {string} Current local time in HH:mm format.
 */
function getNowLocalTime() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${pad(now.getHours())}:${pad(now.getMinutes())}`
}

/**
 * Formats an ISO date-time string to a date string.
 *
 * @param {string|null|undefined} value - The ISO date-time value.
 * @returns {string} Formatted date or fallback value.
 */
function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('en-GB')
}

/**
 * Formats an ISO date-time string to a time string.
 *
 * @param {string|null|undefined} value - The ISO date-time value.
 * @returns {string} Formatted time or fallback value.
 */
function formatTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * Converts an alcohol log type enum value into user-friendly text.
 *
 * @param {string} type - The alcohol log type.
 * @returns {string} A user-friendly label.
 */
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

/**
 * Returns a CSS class name for a type badge.
 *
 * @param {string} type - The alcohol log type.
 * @returns {string} A CSS class suffix for styling.
 */
function badgeClass(type) {
  switch (type) {
    case 'AGE_CHECK':
      return 'badge-age-check'
    case 'SERVING_START':
      return 'badge-serving-start'
    case 'SERVING_END':
      return 'badge-serving-end'
    case 'BREAK':
      return 'badge-break'
    case 'INCIDENT':
      return 'badge-incident'
    default:
      return ''
  }
}

/**
 * Returns a normalized text value for comparison.
 *
 * @param {string|null|undefined} value - The value to normalize.
 * @returns {string} A trimmed normalized string.
 */
function normalizeText(value) {
  return (value || '').trim()
}

/**
 * Groups age-check and incident logs into one visual history row
 * when they represent the same denied-service event.
 *
 * <p>The grouping is based on:
 * - same recorded time
 * - same recordedBy
 * - same notes
 * - one AGE_CHECK and one INCIDENT entry
 *
 * @param {Array} logs - Raw alcohol log history from the backend.
 * @returns {Array} Grouped log entries for display.
 */
function groupHistoryLogs(logs) {
  const grouped = []
  const usedIndexes = new Set()

  for (let i = 0; i < logs.length; i += 1) {
    if (usedIndexes.has(i)) continue

    const current = logs[i]

    if (current.type === 'AGE_CHECK') {
      const matchIndex = logs.findIndex((candidate, index) => {
        if (index === i || usedIndexes.has(index)) return false

        return (
          candidate.type === 'INCIDENT' &&
          candidate.recordedAt === current.recordedAt &&
          candidate.recordedBy === current.recordedBy &&
          normalizeText(candidate.notes) === normalizeText(current.notes)
        )
      })

      if (matchIndex !== -1) {
        usedIndexes.add(i)
        usedIndexes.add(matchIndex)

        grouped.push({
          ...current,
          displayTypes: ['AGE_CHECK', 'INCIDENT'],
          grouped: true,
        })

        continue
      }
    }

    usedIndexes.add(i)
    grouped.push({
      ...current,
      displayTypes: [current.type],
      grouped: false,
    })
  }

  return grouped
}

function validateForm() {
  errorMessage.value = ''
  clearFieldErrors()

  if (!form.value.type) {
    errorMessage.value = 'Log type is required.'
    return false
  }

  if (!form.value.recordedTime) {
    fieldErrors.value.recordedTime = 'Time is required.'
    return false
  }

  if (form.value.type === 'INCIDENT' && !form.value.notes?.trim()) {
    fieldErrors.value.notes = 'Incident notes are required.'
    return false
  }

  if (form.value.type !== 'INCIDENT' && !requiresDenialNotes() && form.value.notes?.trim()) {
    fieldErrors.value.notes =
      'Notes are only allowed for incidents or denied age checks that require an explanation.'
    return false
  }

  if (form.value.type === 'AGE_CHECK') {
    if (form.value.guestAge == null || form.value.guestAge === '') {
      fieldErrors.value.guestAge = 'Guest age is required.'
      return false
    }

    if (form.value.guestAge < 0 || form.value.guestAge > 90) {
      fieldErrors.value.guestAge = 'Guest age must be between 0 and 90.'
      return false
    }

    if (form.value.alcoholPercentage == null || form.value.alcoholPercentage === '') {
      fieldErrors.value.alcoholPercentage = 'Alcohol percentage is required.'
      return false
    }

    if (form.value.alcoholPercentage < 0 || form.value.alcoholPercentage > 60) {
      fieldErrors.value.alcoholPercentage = 'Alcohol percentage must be between 0 and 60.'
      return false
    }

    if (form.value.idChecked !== true) {
      fieldErrors.value.idChecked = 'ID must be checked for an age check registration.'
      return false
    }

    if (requiresDenialNotes() && !form.value.notes?.trim()) {
      fieldErrors.value.notes =
        'A reason is required when service is denied even though the guest is old enough.'
      return false
    }

    if (form.value.guestAge < 18 && form.value.serviceDenied !== true) {
      fieldErrors.value.serviceDenied = 'Service must be denied for guests under 18.'
      return false
    }

    if (
      form.value.alcoholPercentage >= 22 &&
      form.value.guestAge < 20 &&
      form.value.serviceDenied !== true
    ) {
      fieldErrors.value.serviceDenied =
        'Service must be denied for alcohol at 22% or more when the guest is under 20.'
      return false
    }
  }

  return true
}

/**
 * Loads alcohol logs for the current active shift.
 *
 * @returns {Promise<void>}
 */
async function loadHistory() {
  isLoadingHistory.value = true
  errorMessage.value = ''

  try {
    const response = await alcoholApi.getCurrentShiftLogs()
    history.value = groupHistoryLogs(response.data || [])
  } catch (error) {
    console.error('Failed to load current shift history:', error)
    errorMessage.value = error.response?.data?.message || 'Failed to load history.'
  } finally {
    isLoadingHistory.value = false
  }
}

/**
 * Loads alcohol logs for a selected date.
 *
 * <p>This action is only intended for managers and administrators.
 *
 * @returns {Promise<void>}
 */
async function loadHistoryByDate() {
  if (!canSearchHistory.value || !historyDate.value) return

  isLoadingHistory.value = true
  errorMessage.value = ''

  try {
    const response = await alcoholApi.getLogsByDate(historyDate.value)
    history.value = groupHistoryLogs(response.data || [])
  } catch (error) {
    console.error('Failed to load history by date:', error)
    errorMessage.value = error.response?.data?.message || 'Failed to load history.'
  } finally {
    isLoadingHistory.value = false
  }
}

/**
 * Submits a new alcohol log entry to the backend.
 *
 * @returns {Promise<void>}
 */
async function submitLog() {
  if (!validateForm()) return

  isSaving.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const payload = {
      type: form.value.type,
      recordedTime: form.value.recordedTime,
      notes: form.value.type === 'INCIDENT' || requiresDenialNotes() ? form.value.notes : null,
      guestAge: form.value.type === 'AGE_CHECK' ? form.value.guestAge : null,
      alcoholPercentage: form.value.type === 'AGE_CHECK' ? form.value.alcoholPercentage : null,
      idChecked: form.value.type === 'AGE_CHECK' ? form.value.idChecked : null,
      serviceDenied: form.value.type === 'AGE_CHECK' ? form.value.serviceDenied : null,
    }

    await alcoholApi.createLog(payload)

    successMessage.value = 'Alcohol registration saved successfully.'
    form.value = {
      type: 'AGE_CHECK',
      recordedTime: getNowLocalTime(),
      notes: '',
      guestAge: null,
      alcoholPercentage: null,
      idChecked: null,
      serviceDenied: null,
    }

    await loadHistory()
    activeTab.value = 'history'
  } catch (error) {
    console.error('Failed to save alcohol log:', error)

    const backendMessage = error.response?.data?.message

    if (backendMessage) {
      errorMessage.value = backendMessage
    } else {
      errorMessage.value = 'Failed to save alcohol registration.'
    }
  } finally {
    isSaving.value = false
  }
}

/**
 * Returns whether the guest is legally old enough for the selected alcohol percentage.
 *
 * @returns {boolean} True if the guest is legally old enough, otherwise false.
 */
function isGuestOldEnough() {
  if (form.value.guestAge == null || form.value.alcoholPercentage == null) {
    return false
  }

  if (form.value.alcoholPercentage >= 22) {
    return form.value.guestAge >= 20
  }

  return form.value.guestAge >= 18
}

/**
 * Returns whether denial notes are required.
 *
 * <p>Notes are required when service is denied even though the guest is legally old enough.
 *
 * @returns {boolean} True if denial notes are required.
 */
function requiresDenialNotes() {
  return form.value.type === 'AGE_CHECK' && form.value.serviceDenied === true && isGuestOldEnough()
}

/**
 * Loads history when the history tab becomes active.
 */
watch(activeTab, async (newTab) => {
  if (newTab === 'history') {
    await loadHistory()
  }
})

watch(
  () => [form.value.guestAge, form.value.alcoholPercentage],
  ([age, percentage]) => {
    if (age == null || percentage == null) return

    if (age < 18 || (percentage >= 22 && age < 20)) {
      form.value.serviceDenied = true
    }
  },
)

/**
 * Loads initial history when the component is mounted.
 */
onMounted(async () => {
  await loadHistory()
})
</script>

<style scoped>
.alcohol-view {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 2.5rem;
}

.page-header p {
  margin: 0;
  color: #4b5563;
  font-size: 1rem;
}

.tab-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.tab-button {
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #111827;
  padding: 12px 22px;
  border-radius: 14px;
  cursor: pointer;
  font-size: 1rem;
}

.tab-button.active {
  background: #1f2b3d;
  color: #ffffff;
  border-color: #1f2b3d;
}

.card {
  background: #ffffff;
  border: 1px solid #dbe1e7;
  border-radius: 18px;
  padding: 28px;
}

.card h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 2rem;
}

.form-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: 1fr 1fr;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font: inherit;
  background: #ffffff;
}

.full-width {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.actions button,
.manager-actions button {
  border: none;
  padding: 11px 18px;
  border-radius: 10px;
  cursor: pointer;
  background: #2563eb;
  color: #ffffff;
  font-size: 0.95rem;
}

.secondary-button {
  background: #e5e7eb !important;
  color: #111827 !important;
}

.info-box {
  background: #f8fafc;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 16px;
}

.info-box p {
  margin-bottom: 0;
}

.alert {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 12px;
}

.alert.error {
  background: #fee2e2;
  color: #b91c1c;
}

.alert.success {
  background: #dcfce7;
  color: #166534;
}

.history-header {
  margin-bottom: 12px;
}

.muted-text {
  margin: 0;
  color: #6b7280;
}

.manager-tools {
  display: flex;
  gap: 16px;
  align-items: end;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.manager-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.loading-state,
.empty-state {
  color: #6b7280;
  padding: 14px 0;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th,
.history-table td {
  text-align: left;
  padding: 14px 12px;
  border-bottom: 1px solid #e5e7eb;
  vertical-align: top;
}

.type-badge {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
}

.badge-age-check {
  background: #dbeafe;
  color: #1d4ed8;
}

.badge-serving-start {
  background: #dcfce7;
  color: #166534;
}

.badge-serving-end {
  background: #fef3c7;
  color: #92400e;
}

.badge-break {
  background: #ede9fe;
  color: #6d28d9;
}

.badge-incident {
  background: #fee2e2;
  color: #b91c1c;
}

.conditional-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.law-box {
  background: #fffbeb;
  border: 1px solid #fcd34d;
  border-radius: 12px;
  padding: 14px;
}

.law-box p {
  margin: 8px 0 0 0;
}

.type-badge-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.checkbox-field {
  justify-content: end;
}

.checkbox-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 62px;
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: #ffffff;
  font-weight: 600;
  box-sizing: border-box;
  cursor: pointer;
}

.checkbox-row input[type='checkbox'] {
  width: 18px;
  height: 18px;
  margin: 0;
}

.field-error {
  margin: 6px 0 0 0;
  color: #b91c1c;
  font-size: 0.92rem;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    justify-content: flex-start;
  }

  .manager-tools {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
