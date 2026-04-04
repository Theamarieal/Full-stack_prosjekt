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
        :class="['filter-btn', {active: selectedFrequency === freq}]"
        @click="selectedFrequency = freq"
        >
        {{freq}}
      </button>
    </div>

    <div v-if="loading" class="loading"> Loading checklists...</div>
    <div v-else-if="filteredChecklists.length === 0" class="empty">
      No checklists found.
    </div>
    <div v-else class="checklists-grid">
      <div
        v-for="checklist in filteredChecklists"
        :key="checklist.id"
        class="checklist-card"
        >
        <h2>{{checklist.title}}</h2>
        <p class="frequency-badge">{{checklist.frequency}}</p>

        <ul class="items-list">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            :class="['checklist-item', {completed: item.completed}]"
            >
            <input
              type="checkbox"
              :checked="item.completed"
              :disabled="item.completed"
              @change="handleComplete(checklist.id, item.id)"
              />
            <span>{{item.description}}</span>
          </li>
        </ul>
    </div>
   </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from "vue"
import {useRouter} from 'vue-router'
import checklistApi from '@/api/checklist'

const router = useRouter()
const checklists = ref([])
const loading = ref(true)
const selectedFrequency = ref('ALL')
const frequencies = ['ALL', 'DAILY', 'WEEKLY', 'MONTHLY']

const filteredChecklists = computed(() => {
  if (selectedFrequency.value === 'ALL') return checklists.value
  return checklists.value.filter(c => c.frequency === selectedFrequency.value)
})

async function loadChecklists() {
  try {
    const res = await checklistApi.getAll()
    checklists.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Failed to load checklists', e)
  } finally {
    loading.value = false
  }
}

async function handleComplete(checklistId, itemId) {
  try {
    await checklistApi.completeItem(checklistId, itemId)
    const checklist = checklists.value.find(c => c.id === checklistId)
    const item = checklist?.items.find(i => i.id === itemId)
    if (item) item.completed = true
  } catch (e) {
    console.error('Failed to complete item', e)
  }
}

function goBack() {
  router.push('/dashboard')
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
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
</style>


