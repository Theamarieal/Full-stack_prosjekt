<template>
  <div class="checklist">
    <header class="checklist-header">
      <h1>Checklists</h1>
      <button @click="goBack" class="back-btn">Back to dashboard</button>
    </header>

    <div class="filter-bar">
      <button
        v-for="freq in frequencies"
        :key="freq"
        :class="['filter-btn', { active: selectedFrequency === freq }]"
        @click="selectedFrequency = freq"
      >
        {{ freq }}
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Loading checklists..." />

    <div v-else-if="error" class="error-banner">{{ error }}</div>

    <div v-else-if="filteredChecklists.length === 0" class="empty">
      No checklists found.
    </div>

    <div v-else class="checklists-grid">
      <div v-for="checklist in filteredChecklists" :key="checklist.id" class="checklist-card">
        <h2>{{ checklist.title }}</h2>
        <p class="frequency-badge">{{ checklist.frequency }}</p>

        <ul class="items-list">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            :class="['checklist-item', { completed: item.completed }]"
          >
            <input
              type="checkbox"
              :checked="item.completed"
              :disabled="item.completed"
              @change="handleComplete(checklist.id, item.id)"
            />
            <span>{{ item.description }}</span>
          </li>
        </ul>

        <p v-if="completeErrors[checklist.id]" class="error-inline">
          {{ completeErrors[checklist.id] }}
        </p>
      </div>
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
const frequencies = ['ALL', 'DAILY', 'WEEKLY', 'MONTHLY']

const filteredChecklists = computed(() => {
  if (selectedFrequency.value === 'ALL') return checklists.value
  return checklists.value.filter((c) => c.frequency === selectedFrequency.value)
})

async function loadChecklists() {
  try {
    const res = await checklistApi.getAll()
    checklists.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = 'Failed to load checklists.'
    console.error('Failed to load checklists', e)
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
    completeErrors.value[checklistId] = 'Failed to complete task. Please try again.'
    console.error('Failed to complete item', e)
  }
}

function goBack() {
  router.push('/')
}

onMounted(loadChecklists)
</script>

<style scoped>
.checklist {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.checklist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  background: #2c3e50;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-btn {
  padding: 6px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  background: white;
}

.filter-btn.active {
  background: #2c3e50;
  color: white;
  border-color: #2c3e50;
}

.checklists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.checklist-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.frequency-badge {
  display: inline-block;
  background: #e8f4f8;
  color: #2c3e50;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  margin-bottom: 10px;
}

.items-list {
  list-style: none;
  padding: 0;
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.checklist-item.completed span {
  text-decoration: line-through;
  color: #aaa;
}

.error-banner {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 16px;
  border-radius: 8px;
  font-weight: 600;
  text-align: center;
}

.error-inline {
  color: #dc2626;
  font-size: 0.85rem;
  margin-top: 8px;
}

.empty {
  text-align: center;
  color: #6b7280;
  padding: 40px;
}
@media (max-width: 768px) {
  .checklist {
    padding: 16px;
  }

  .checklist-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .back-btn {
    min-height: 44px;
    padding: 0 16px;
    width: 100%;
  }

  .filter-bar {
    flex-wrap: wrap;
  }

  .filter-btn {
    min-height: 44px;
    padding: 0 16px;
    flex: 1;
  }

  .checklists-grid {
    grid-template-columns: 1fr;
  }

  .checklist-item {
    gap: 14px;
  }

  .checklist-item input[type="checkbox"] {
    width: 22px;
    height: 22px;
    flex-shrink: 0;
  }
}
</style>


