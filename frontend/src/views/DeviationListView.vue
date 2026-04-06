<template>
  <div class="deviation-list">
    <div class="header">
      <h1>Deviations</h1>
      <button class="btn-primary" @click="router.push('/deviations/new')">+ Report deviation</button>
    </div>

    <div class="filters">
      <div class="filter-group">
        <label>Status</label>
        <select v-model="filterStatus">
          <option value="">All statuses</option>
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In progress</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Module</label>
        <select v-model="filterModule">
          <option value="">All modules</option>
          <option value="IK_MAT">IK-Mat</option>
          <option value="IK_ALKOHOL">IK-Alkohol</option>
        </select>
      </div>
    </div>

    <LoadingSpinner v-if="loading" message="Loading deviations..." />

    <div v-else-if="error" class="error-banner">{{ error }}</div>

    <div v-else-if="filtered.length === 0" class="empty">
      No deviations found.
    </div>

    <div v-else class="deviation-cards">
      <div
        v-for="deviation in filtered"
        :key="deviation.id"
        class="deviation-card"
        :class="statusClass(deviation.status)"
      >
        <div class="card-header">
          <h3>{{ deviation.title }}</h3>
          <span class="status-badge" :class="statusClass(deviation.status)">
            {{ formatStatus(deviation.status) }}
          </span>
        </div>

        <p class="module-tag">{{ formatModule(deviation.module) }}</p>
        <p v-if="deviation.description" class="description">{{ deviation.description }}</p>
        <p class="meta">
          Reported: {{ formatDate(deviation.createdAt) }}
          <span v-if="deviation.reportedBy"> by {{ deviation.reportedBy.email }}</span>
        </p>

        <div v-if="updateError === deviation.id" class="error-inline">
          Failed to update status. Please try again.
        </div>

        <div v-if="isManagerOrAdmin" class="status-actions">
          <label>Update status:</label>
          <select
            :value="deviation.status"
            @change="updateStatus(deviation.id, $event.target.value)"
          >
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import deviationApi from '@/api/deviation'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const deviations = ref([])
const loading = ref(true)
const error = ref('')
const updateError = ref(null)
const filterStatus = ref('')
const filterModule = ref(route.query.module || '')

const isManagerOrAdmin = computed(() => {
  const role = authStore.user?.role
  return role === 'MANAGER' || role === 'ADMIN'
})

const filtered = computed(() => {
  return deviations.value.filter((d) => {
    const matchStatus = filterStatus.value ? d.status === filterStatus.value : true
    const matchModule = filterModule.value ? d.module === filterModule.value : true
    return matchStatus && matchModule
  })
})

watch(() => route.query.module, (val) => {
  filterModule.value = val || ''
})

onMounted(async () => {
  try {
    const res = await deviationApi.getAll()
    deviations.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = 'Failed to load deviations.'
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function updateStatus(id, newStatus) {
  updateError.value = null
  try {
    const res = await deviationApi.updateStatus(id, newStatus)
    const index = deviations.value.findIndex((d) => d.id === id)
    if (index !== -1) deviations.value[index] = res.data
  } catch (e) {
    updateError.value = id
    console.error('Failed to update status', e)
  }
}

function statusClass(status) {
  if (status === 'OPEN') return 'status-open'
  if (status === 'IN_PROGRESS') return 'status-in-progress'
  if (status === 'RESOLVED') return 'status-resolved'
  return ''
}

function formatStatus(status) {
  if (status === 'OPEN') return 'Open'
  if (status === 'IN_PROGRESS') return 'In progress'
  if (status === 'RESOLVED') return 'Resolved'
  return status
}

function formatModule(module) {
  if (module === 'IK_MAT') return 'IK-Mat'
  if (module === 'IK_ALKOHOL') return 'IK-Alkohol'
  return module || ''
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('no-NO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}
</script>

<style scoped>
.deviation-list {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h1 {
  color: #2c3e50;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #2c3e50;
}

select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  background: white;
}

.deviation-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.deviation-card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid #ddd;
}

.deviation-card.status-open {
  border-left-color: #dc2626;
}

.deviation-card.status-in-progress {
  border-left-color: #f59e0b;
}

.deviation-card.status-resolved {
  border-left-color: #16a34a;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-header h3 {
  color: #2c3e50;
  margin: 0;
}

.status-badge {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
}

.status-badge.status-open {
  background: #fee2e2;
  color: #dc2626;
}

.status-badge.status-in-progress {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.status-resolved {
  background: #dcfce7;
  color: #16a34a;
}

.module-tag {
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 8px;
}

.description {
  color: #4b5563;
  margin-bottom: 8px;
}

.meta {
  font-size: 0.8rem;
  color: #9ca3af;
  margin-bottom: 12px;
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
  margin-bottom: 8px;
}

.status-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.status-actions label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #2c3e50;
}

.empty {
  text-align: center;
  color: #6b7280;
  padding: 40px;
}

.btn-primary {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-primary:hover {
  background: #1d4ed8;
}
</style>
