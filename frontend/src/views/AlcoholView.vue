<template>
  <div class="alcohol-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Alcohol Control</h1>
        <p class="subtitle">Compliance logs and serving routines</p>
      </div>
      <button type="button" class="back-btn-minimal" @click="router.push('/')">← Dashboard</button>
    </header>

    <LoadingSpinner v-if="initialLoading" message="Loading..." />

    <template v-else>
      <div class="tab-container" role="tablist" aria-label="Alcohol control sections">
        <button
          id="tab-register"
          type="button"
          role="tab"
          class="tab-pill"
          :class="{ active: activeTab === 'register' }"
          :aria-selected="activeTab === 'register'"
          aria-controls="panel-register"
          @click="activeTab = 'register'"
        >
          New Registration
        </button>
        <button
          id="tab-history"
          type="button"
          role="tab"
          class="tab-pill"
          :class="{ active: activeTab === 'history' }"
          :aria-selected="activeTab === 'history'"
          aria-controls="panel-history"
          @click="activeTab = 'history'"
        >
          Shift History
        </button>
      </div>

      <div v-if="errorMessage" class="error-banner" role="alert">
        <span aria-hidden="true">⚠ </span>{{ errorMessage }}
      </div>
      <div v-if="successMessage" class="success-banner" role="status">
        <span aria-hidden="true">✓ </span>{{ successMessage }}
      </div>

      <section
        v-if="activeTab === 'register'"
        id="panel-register"
        role="tabpanel"
        aria-labelledby="tab-register"
        class="stat-card"
      >
        <div class="card-header">
          <h3>Register shift event</h3>
        </div>

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
            <label for="recordedTime">Time of event</label>
            <input
              id="recordedTime"
              v-model="form.recordedTime"
              type="time"
              required
              :aria-invalid="!!fieldErrors.recordedTime"
              :aria-describedby="fieldErrors.recordedTime ? 'recordedTime-error' : undefined"
            />
            <p
              v-if="fieldErrors.recordedTime"
              id="recordedTime-error"
              class="field-error"
            >
              {{ fieldErrors.recordedTime }}
            </p>
          </div>

          <div v-if="form.type === 'AGE_CHECK'" class="full-width conditional-area">
            <div class="inner-grid">
              <div class="form-group">
                <label for="guestAge">Guest age</label>
                <input
                  id="guestAge"
                  v-model.number="form.guestAge"
                  type="number"
                  min="0"
                  max="90"
                  placeholder="e.g. 19"
                  :aria-invalid="!!fieldErrors.guestAge"
                  :aria-describedby="fieldErrors.guestAge ? 'guestAge-error' : undefined"
                />
                <p
                  v-if="fieldErrors.guestAge"
                  id="guestAge-error"
                  class="field-error"
                >
                  {{ fieldErrors.guestAge }}
                </p>
              </div>

              <div class="form-group">
                <label for="alcoholPercentage">Alcohol %</label>
                <input
                  id="alcoholPercentage"
                  v-model.number="form.alcoholPercentage"
                  type="number"
                  step="0.1"
                  placeholder="e.g. 4.7"
                  :aria-invalid="!!fieldErrors.alcoholPercentage"
                  :aria-describedby="fieldErrors.alcoholPercentage ? 'alcoholPercentage-error' : undefined"
                />
                <p
                  v-if="fieldErrors.alcoholPercentage"
                  id="alcoholPercentage-error"
                  class="field-error"
                >
                  {{ fieldErrors.alcoholPercentage }}
                </p>
              </div>

              <div class="form-group">
                <div class="custom-checkbox-row">
                  <input
                    id="idChecked"
                    type="checkbox"
                    v-model="form.idChecked"
                    :aria-invalid="!!fieldErrors.idChecked"
                    :aria-describedby="fieldErrors.idChecked ? 'idChecked-error' : undefined"
                  />
                  <label for="idChecked">ID Checked</label>
                </div>
                <p
                  v-if="fieldErrors.idChecked"
                  id="idChecked-error"
                  class="field-error"
                >
                  {{ fieldErrors.idChecked }}
                </p>
              </div>

              <div class="form-group">
                <div class="custom-checkbox-row">
                  <input
                    id="serviceDenied"
                    type="checkbox"
                    v-model="form.serviceDenied"
                    :aria-invalid="!!fieldErrors.serviceDenied"
                    :aria-describedby="fieldErrors.serviceDenied ? 'serviceDenied-error' : undefined"
                  />
                  <label for="serviceDenied">Service Denied</label>
                </div>
                <p
                  v-if="fieldErrors.serviceDenied"
                  id="serviceDenied-error"
                  class="field-error"
                >
                  {{ fieldErrors.serviceDenied }}
                </p>
              </div>
            </div>

            <div v-if="showAgeCheckNotes" class="form-group full-width">
              <label for="ageCheckNotes">{{ ageCheckNotesLabel }}</label>
              <textarea
                id="ageCheckNotes"
                v-model="form.notes"
                rows="4"
                :placeholder="ageCheckNotesPlaceholder"
                :aria-invalid="!!fieldErrors.notes"
                :aria-describedby="fieldErrors.notes ? 'notes-error' : undefined"
              />
              <p v-if="fieldErrors.notes" id="notes-error" class="field-error">
                {{ fieldErrors.notes }}
              </p>
            </div>

            <div class="law-box">
              <strong>Age control rules</strong>
              <p>
                Guests must be at least 18 for alcohol below 22% and at least 20 for alcohol at 22%
                or above. If service is not legally allowed, it must be denied.
              </p>
            </div>
          </div>

          <div class="info-tile neutral-tile">
            <strong>Recommended usage</strong>
            <p>
              Use <em>Age check</em> for ID control routines, <em>Serving start</em> and 
              <em>Serving end</em> for serving hours, <em>Break</em> for pauses in serving, 
              and <em>Incident</em> for alcohol-related deviations or events.
            </p>
          </div>

          <div v-if="form.type === 'INCIDENT'" class="form-group full-width">
            <label for="notes">
              {{ form.type === 'INCIDENT' ? 'Incident description' : 'Reason for denied service' }}
            </label>
            <textarea
              id="notes"
              v-model="form.notes"
              rows="4"
              :placeholder="form.type === 'INCIDENT' ? 'Describe what happened...' : 'Explain why service was denied...'"
              :aria-invalid="!!fieldErrors.notes"
              :aria-describedby="fieldErrors.notes ? 'notes-error' : undefined"
            ></textarea>
            <p
              v-if="fieldErrors.notes"
              id="notes-error"
              class="field-error"
            >
              {{ fieldErrors.notes }}
            </p>
          </div>

          <div class="form-actions full-width">
            <button type="submit" class="save-btn" :disabled="isSaving">
              {{ isSaving ? 'Saving...' : 'Save registration' }}
            </button>
          </div>
        </form>
      </section>

      <section
        v-else
        id="panel-history"
        role="tabpanel"
        aria-labelledby="tab-history"
        class="stat-card history-section"
      >
        <div class="card-header">
          <h3>Shift History</h3>
          <p class="results-count">{{ history.length }} entries recorded</p>
        </div>

        <div v-if="canSearchHistory" class="manager-tools-grid">
          <div class="form-group">
            <label for="historyDate">Search date</label>
            <p id="historyDate-help" class="help-text">Choose the date you want to load history for.</p>
            <input id="historyDate" v-model="historyDate" type="date" aria-describedby="historyDate-help"/>
          </div>
          <div class="manager-actions">
            <button type="button" @click="loadHistoryByDate" class="apply-btn" :disabled="!historyDate">
              Load date
            </button>
            <button type="button" @click="loadHistory" class="secondary-btn-minimal">
              Current shift
            </button>
          </div>
        </div>

        <LoadingSpinner v-if="isLoadingHistory" message="Loading logs..." />
        <div v-else-if="history.length === 0" class="empty-state">No alcohol logs found.</div>

        <div v-else class="table-container">
          <table class="history-table">
            <caption class="sr-only">Alcohol shift history log</caption>
            <thead>
              <tr>
                <th scope="col">Time</th>
                <th scope="col">Type</th>
                <th scope="col">Notes / Details</th>
                <th scope="col">Staff</th>
                <th v-if="canSearchHistory" scope="col">Date</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in history" :key="log.id">
                <td class="time-cell">{{ formatTime(log.recordedAt) }}</td>
                <td>
                  <div class="tag-stack">
                    <span
                      v-for="t in log.displayTypes"
                      :key="t"
                      :class="['tag', badgeClass(t)]"
                    >
                      {{ formatType(t) }}
                    </span>
                  </div>
                </td>
                <td class="notes-cell">
                  {{ log.notes?.trim() ? log.notes : 'Ingen detaljer registrert' }}
                </td>
                <td class="user-cell">{{ log.recordedBy?.split('@')[0] }}</td>
                <td v-if="canSearchHistory" class="date-cell">{{ formatDate(log.recordedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import alcoholApi from '@/api/alcohol'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const authStore = useAuthStore()

const activeTab = ref('register')
const isSaving = ref(false)
const isLoadingHistory = ref(false)
const initialLoading = ref(true)
const errorMessage = ref('')
const successMessage = ref('')
const history = ref([])
const historyDate = ref('')

const form = ref(createDefaultForm())

const fieldErrors = ref({
  guestAge: '',
  alcoholPercentage: '',
  idChecked: '',
  serviceDenied: '',
  notes: '',
  recordedTime: '',
})

function createDefaultForm() {
  return {
    type: 'AGE_CHECK',
    recordedTime: getNowLocalTime(),
    notes: '',
    guestAge: null,
    alcoholPercentage: null,
    idChecked: null,
    serviceDenied: null,
  }
}

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
 */
const canSearchHistory = computed(() => {
  const role = authStore.user?.role
  return role === 'MANAGER' || role === 'ADMIN'
})

/**
 * Returns whether the guest is legally old enough for the selected alcohol percentage.
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
 * Returns whether this age check represents a compliance-risk situation.
 * These cases should be allowed to save, and the backend should create a deviation.
 */
const isDeviationAgeCheck = computed(() => {
  if (form.value.type !== 'AGE_CHECK') {
    return false
  }

  if (
    form.value.guestAge == null ||
    form.value.alcoholPercentage == null ||
    form.value.idChecked == null ||
    form.value.serviceDenied == null
  ) {
    return false
  }

  const noIdChecked = form.value.idChecked === false
  const servedUnder18 = form.value.guestAge < 18 && form.value.serviceDenied === false
  const servedStrongAlcoholUnder20 =
    form.value.alcoholPercentage >= 22 &&
    form.value.guestAge < 20 &&
    form.value.serviceDenied === false

  return noIdChecked || servedUnder18 || servedStrongAlcoholUnder20
})

/**
 * Notes are shown for all age checks so the employee can document the situation.
 */
const showAgeCheckNotes = computed(() => {
  return form.value.type === 'AGE_CHECK'
})

const ageCheckNotesLabel = computed(() => {
  if (requiresDenialNotes()) {
    return 'Reason for denied service'
  }

  if (isDeviationAgeCheck.value) {
    return 'Additional notes'
  }

  return 'Notes (optional)'
})

const ageCheckNotesPlaceholder = computed(() => {
  if (requiresDenialNotes()) {
    return 'Explain why service was denied even though the guest met the age requirement.'
  }

  if (isDeviationAgeCheck.value) {
    return 'Add any relevant details about this registration.'
  }

  return 'Add optional notes for this age check.'
})

/**
 * Returns whether denial notes are required.
 * Notes are required when service is denied even though the guest is legally old enough.
 */
function requiresDenialNotes() {
  return form.value.type === 'AGE_CHECK' && form.value.serviceDenied === true && isGuestOldEnough()
}

/**
 * Returns the current local time formatted for a time input.
 */
function getNowLocalTime() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${pad(now.getHours())}:${pad(now.getMinutes())}`
}

/**
 * Formats an ISO date-time string to a date string.
 */
function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('nb-NO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}


/**
 * Formats an ISO date-time string to a time string.
 */
function formatTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleTimeString('nb-NO', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * Converts an alcohol log type enum value into user-friendly text.
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
 */
function normalizeText(value) {
  return (value || '').trim()
}

/**
 * Groups age-check and incident logs into one visual history row
 * when they represent the same denied-service event.
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

    if (form.value.idChecked == null) {
      fieldErrors.value.idChecked = 'Please select whether ID was checked.'
      return false
    }

    if (form.value.serviceDenied == null) {
      fieldErrors.value.serviceDenied = 'Please select whether service was denied.'
      return false
    }

    if (requiresDenialNotes() && !form.value.notes?.trim()) {
      fieldErrors.value.notes =
        'A reason is required when service is denied even though the guest is old enough.'
      return false
    }
  }

  return true
}

/**
 * Loads alcohol logs for the current active shift.
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
      notes:
        form.value.type === 'INCIDENT' || form.value.type === 'AGE_CHECK' ? form.value.notes : null,
      guestAge: form.value.type === 'AGE_CHECK' ? form.value.guestAge : null,
      alcoholPercentage: form.value.type === 'AGE_CHECK' ? form.value.alcoholPercentage : null,
      idChecked: form.value.type === 'AGE_CHECK' ? form.value.idChecked : null,
      serviceDenied: form.value.type === 'AGE_CHECK' ? form.value.serviceDenied : null,
    }

    await alcoholApi.createLog(payload)

    successMessage.value = 'Alcohol registration saved successfully.'
    form.value = createDefaultForm()

    await loadHistory()
    activeTab.value = 'history'
  } catch (error) {
    console.error('Failed to save alcohol log:', error)

    const responseData = error.response?.data

    if (typeof responseData === 'string') {
      errorMessage.value = responseData
    } else if (responseData?.message) {
      errorMessage.value = responseData.message
    } else {
      errorMessage.value = 'Failed to save alcohol registration.'
    }
  } finally {
    isSaving.value = false
  }
}

/**
 * Clear messages when the log type changes.
 */
watch(
  () => form.value.type,
  (newType) => {
    clearFieldErrors()
    errorMessage.value = ''
    successMessage.value = ''

    if (newType !== 'AGE_CHECK') {
      form.value.guestAge = null
      form.value.alcoholPercentage = null
      form.value.idChecked = null
      form.value.serviceDenied = null
    }

    if (newType !== 'AGE_CHECK' && newType !== 'INCIDENT') {
      form.value.notes = ''
    }

    if (newType === 'AGE_CHECK') {
      form.value.notes = ''
      form.value.idChecked = null
      form.value.serviceDenied = null
    }
  },
)

/**
 * Loads history when the history tab becomes active.
 */
watch(activeTab, async (newTab) => {
  if (newTab === 'history') {
    await loadHistory()
  }
})

/**
 * Loads initial history when the component is mounted.
 */
onMounted(async () => {
  await loadHistory()
  initialLoading.value = false
})
</script>

<style scoped>
.alcohol-page {
  max-width: 1100px;
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

.back-btn-minimal {
  background: transparent; color: #534AB7; border: 1.5px solid #e0dfd8;
  padding: 10px 16px; border-radius: 10px; font-weight: 700; cursor: pointer;
}

.tab-container { display: flex; gap: 8px; margin-bottom: 24px; }
.tab-pill {
  padding: 10px 20px; border-radius: 25px; border: 1.5px solid #e0dfd8;
  background: white; color: #666; font-weight: 700; cursor: pointer; transition: all 0.2s;
}
.tab-pill.active { background: #534AB7; color: white; border-color: #534AB7; box-shadow: 0 4px 10px rgba(83, 74, 183, 0.2); }

.stat-card {
  background: white; border: 1px solid #e0dfd8; border-left: 6px solid #534AB7;
  border-radius: 14px; padding: 32px; box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
}

.card-header h3 { color: #3C3489; font-weight: 800; margin-bottom: 24px; font-size: 1.25rem; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.inner-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; width: 100%; }
.full-width { grid-column: 1 / -1; }

.form-group label { display: block; font-weight: 700; margin-bottom: 8px; color: #3C3489; font-size: 0.85rem; }
input, select, textarea {
  padding: 12px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  width: 100%;
  background: #fafaf8;
  font-size: 1rem;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #534AB7;
  background: white;
}

button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.custom-checkbox-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border: 1.5px solid #e0dfd8;
  border-radius: 12px;
  background: #fafaf8;
  min-height: 58px;
}

.custom-checkbox-row label {
  margin-bottom: 0;
  font-weight: 700;
  color: #3C3489;
  font-size: 0.95rem;
  cursor: pointer;
}

.custom-checkbox-row input[type="checkbox"] {
  width: 22px;
  height: 22px;
  accent-color: #534AB7;
  margin: 0;
  flex-shrink: 0;
}

.info-tile { width: 100%; grid-column: 1 / -1; max-width: none; padding: 16px; border-radius: 12px; margin-top: 16px; font-size: 0.9rem; }
.warning-tile { background: #fffbeb; border: 1.5px solid #fcd34d; }
.neutral-tile {  background: #f3f4f6;  border: 1.5px solid #d1d5db;  color: #111827;}

.tag-stack { display: flex; gap: 6px; }
.tag { padding: 4px 10px; border-radius: 6px; font-size: 0.7rem; font-weight: 800; text-transform: uppercase; }
.badge-age-check { background: #f5f4ff; color: #4338ca; }
.badge-incident { background: #fff5f5; color: #b91c1c; }
.badge-serving-start { background: #ecfdf5; color: #166534; }
.badge-serving-end { background: #fffbeb; color: #92400e; }
.badge-break { background: #f0f9ff; color: #0c4a6e; }

.field-error { color: #b91c1c; font-size: 0.85rem; font-weight: 700; margin-top: 6px; }

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

.conditional-area {
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

.tag-stack {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}


.manager-tools-grid { display: flex; gap: 20px; align-items: flex-end; margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #f0f0f0; }
.manager-actions { display: flex; gap: 10px; }

.save-btn {
  background: #534AB7;
  color: white;
  border: none;
  padding: 14px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  width: 100%;
  transition: background 0.2s;
}

.save-btn:hover {
  background: #3C3489;
}

.save-btn:disabled {
  background: #a8a3d6;
  cursor: not-allowed;
}

.apply-btn {
  background: #534AB7;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
}

.apply-btn:hover {
  background: #3C3489;
}

.apply-btn:disabled {
  background: #a8a3d6;
  cursor: not-allowed;
}

.secondary-btn-minimal {
  background: transparent;
  border: 1.5px solid #e0dfd8;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  color: #3C3489;
  transition: all 0.2s;
}

.secondary-btn-minimal:hover {
  background: #fafaf8;
  border-color: #534AB7;
}

.form-actions {
  display: flex;
  width: 100%;
}

.error-banner {
  background: #fff5f5;
  color: #991b1b;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 20px;
  border: 1px solid #fecaca;
}

.success-banner {
  background: #ecfdf5;
  color: #166534;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 20px;
  border: 1px solid #bbf7d0;
}

.empty-state {
  text-align: center;
  color: #4b5563;
  padding: 40px;
  font-weight: 600;
}

.results-count {
  color: #374151;
  font-weight: 600;
}

.help-text {
  font-size: 0.8rem;
  color: #4b5563;
  margin: 0 0 6px 0;
}

@media (max-width: 350px) {
  .form-grid, .inner-grid, .manager-tools-grid { grid-template-columns: 1fr; }
  .manager-actions { width: 100%; }
  .apply-btn, .secondary-btn-minimal { flex: 1; }
  .page-header-section { flex-direction: column; gap: 16px; }
  .back-btn-minimal { width: 100%; text-align: center; }
}
</style>