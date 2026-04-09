<template>
  <div class="checklist-page">
    <header class="checklist-header">
      <div class="header-content">
        <h1>Checklists</h1>
        <p class="subtitle">Daily routines and quality control</p>
      </div>
      <button type="button" @click="goBack" class="back-btn-minimal">← Dashboard</button>
    </header>

    <div class="filter-bar" role="tablist" aria-label="Checklist frequency filters">
      <button
        v-for="freq in frequencies"
        :key="freq"
        type="button"
        role="tab"
        :aria-selected="selectedFrequency === freq"
        :class="['filter-btn', { active: selectedFrequency === freq }]"
        @click="selectedFrequency = freq"
      >
        {{ freq }}
      </button>
    </div>

    <div class="module-filter" role="group" aria-label="Module filter">
      <label for="module-select">Department/Module</label>
      <select id="module-select" v-model="selectedModule">
        <option value="ALL">All modules</option>
        <option value="KITCHEN">IK-Mat (Food Safety)</option>
        <option value="BAR">IK-Alkohol (Alcohol)</option>
      </select>
    </div>

    <LoadingSpinner v-if="loading" message="Loading checklists..." />

    <div v-else-if="error" class="error-banner" role="alert" aria-live="assertive">
      <span aria-hidden="true">⚠ </span>{{ error }}
    </div>

    <div v-else-if="filteredChecklists.length === 0" class="empty" role="status">
      <div class="empty-icon" aria-hidden="true">📋</div>
      <p>No checklists found for this category.</p>
    </div>

    <div v-else class="checklists-grid">
      <section
        v-for="checklist in filteredChecklists"
        :key="checklist.id"
        class="checklist-card"
        :class="{ 'is-complete': isChecklistDone(checklist) }"
        :aria-label="`Checklist: ${checklist.title}`"
      >
        <div class="card-header">
          <div class="header-main">
            <h2>{{ checklist.title }}</h2>
            <span class="frequency-tag">{{ checklist.frequency }}</span>
          </div>

          <div
            class="status-icon"
            v-if="isChecklistDone(checklist)"
            aria-label="Checklist completed"
            role="img"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" aria-hidden="true">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </div>
        </div>

        <p v-if="isChecklistDone(checklist)" class="status-text">
          Completed
        </p>

        <ul class="items-list">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            :class="['checklist-item', { completed: item.completed }]"
          >
            <label class="item-label">
              <input
                type="checkbox"
                :checked="item.completed"
                :disabled="item.completed"
                @change="handleComplete(checklist.id, item.id)"
              />
              <span class="item-text">{{ item.description }}</span>
            </label>
          </li>
        </ul>

        <p v-if="completeErrors[checklist.id]" class="error-inline" role="alert">
          <span aria-hidden="true">⚠ </span>{{ completeErrors[checklist.id] }}
        </p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import checklistApi from '@/api/checklist'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const checklists = ref([])
const loading = ref(true)
const error = ref('')
const completeErrors = ref({})
const selectedFrequency = ref('ALL')
const selectedModule = ref('ALL')
const frequencies = ['ALL', 'DAILY', 'WEEKLY', 'MONTHLY']

const filteredChecklists = computed(() => {
  return checklists.value.filter((c) => {
    const freqMatch = selectedFrequency.value === 'ALL' || c.frequency === selectedFrequency.value
    const moduleMatch = selectedModule.value === 'ALL' || c.module === selectedModule.value
    return freqMatch && moduleMatch
  })
})

function isChecklistDone(checklist) {
  return checklist.items && checklist.items.every(item => item.completed)
}

async function loadChecklists() {
  try {
    const res = await checklistApi.getAll()
    checklists.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = 'Failed to load checklists.'
  } finally {
    loading.value = false
  }
}

async function handleComplete(checklistId, itemId) {
  completeErrors.value[checklistId] = ''
  try {
    await checklistApi.completeItem(checklistId, itemId)
    const checklist = checklists.value.find((c) => c.id === checklistId)
    const item = checklist?.items.find((i) => i.id === itemId)
    if (item) item.completed = true
  } catch (e) {
    completeErrors.value[checklistId] = 'Failed to save. Try again.'
  }
}

function goBack() {
  router.push('/')
}

onMounted(loadChecklists)
</script>

<style scoped>
.checklist-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
  min-height: 100vh;
  background-color: #f7f6f2;
}

.checklist-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-content h1 {
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
  transition: all 0.2s;
}

.back-btn-minimal:hover {
  background: #ffffff;
  border-color: #534AB7;
  transform: translateX(-4px);
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.filter-btn {
  padding: 8px 18px;
  border: 1.5px solid #e0dfd8;
  border-radius: 20px;
  background: white;
  color: #4b5563;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.filter-btn.active {
  background: #534AB7;
  color: white;
  border-color: #534AB7;
  box-shadow: 0 4px 10px rgba(83, 74, 183, 0.2);
}

.module-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.module-filter label {
  font-weight: 700;
  font-size: 0.95rem;
  color: #3C3489;
  white-space: nowrap;
}

.module-filter select {
  padding: 8px 12px;
  border: 1.5px solid #d1d5db;
  border-radius: 10px;
  font-size: 0.95rem;
  background: white;
  color: #374151;
  cursor: pointer;
  min-width: 200px;
}

.module-filter select:focus {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.checklists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 24px;
}

.checklist-card {
  background: white;
  border: 1px solid #e0dfd8;
  border-left: 6px solid #534AB7;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
  transition: all 0.2s ease;
}

.checklist-card.is-complete {
  border-left-color: #10b981;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.card-header h2 {
  font-size: 1.25rem;
  color: #3C3489;
  font-weight: 800;
  margin-bottom: 6px;
}

button:focus-visible,
input:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.frequency-tag {
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #4338ca;
  background: #f5f4ff;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #c7d2fe;
}

.status-icon svg {
  width: 24px;
  height: 24px;
  color: #166534;
}

.status-text {
  margin: 0 0 12px 0;
  color: #166534;
  font-weight: 700;
  font-size: 0.95rem;
}

.items-list {
  list-style: none;
}

.checklist-item {
  margin-bottom: 4px;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.item-label:hover {
  background: #f8fafc;
}

input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #534AB7;
  flex-shrink: 0;
}

.item-text {
  font-size: 1rem;
  font-weight: 500;
  color: #2c2c2a;
}

.completed .item-text {
  text-decoration: line-through;
  color: #475569;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  text-align: center;
}

.error-inline {
  margin-top: 12px;
  color: #991b1b;
  font-weight: 700;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  border: 2px dashed #d1d5db;
  color: #374151;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

@media (max-width: 786px) {
  .checklist-header {
    flex-direction: column;
    gap: 16px;
  }

  .back-btn-minimal {
    width: 100%;
    text-align: center;
  }

  .checklists-grid {
    grid-template-columns: 1fr;
  }

  .module-filter {
    flex-direction: column;
    align-items: flex-start;
  }

  .module-filter select {
    width: 100%;
  }
}
</style>

