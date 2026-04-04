<template>
  <div class="dashboard">
    <main class="dashboard-content">
      <div class="welcome-card">
        <h2>Welcome to Checkd</h2>
        <p>
          This is your overview for
          <strong>{{ authStore.user?.organization?.name || 'your restaurant' }}</strong>.
        </p>
      </div>

      <div class="sections-grid">
        <section class="module-section">
          <h2>IK-Mat</h2>
          <div class="stats-grid">
            <div class="stat-card clickable-card" @click="router.push('/checklists')">
              <h3>Checklists</h3>
              <p v-if="matStats.total === 0">No checklists found.</p>
              <p v-else>
                <span class="stat-number">{{ matStats.completed }}</span> / {{ matStats.total }} completed
              </p>
              <p v-if="matStats.remaining > 0" class="warning-text">{{ matStats.remaining }} tasks remaining ⚠️</p>
              <p v-else-if="matStats.total > 0" class="ok-text">All done ✓</p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/temperature')">
              <h3>Temperature</h3>
              <p>Last measurements from your fridges and freezers.</p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/deviations')">
              <h3>Deviations</h3>
              <div class="deviation-header">
                <span v-if="openDeviations > 0" class="badge">{{ openDeviations }}</span>
              </div>
              <p v-if="openDeviations > 0" class="warning-text">Open deviations</p>
              <p v-else class="ok-text">No open deviations</p>
            </div>
          </div>
        </section>

        <section class="module-section">
          <h2>IK-Alkohol</h2>
          <div class="stats-grid">
            <div class="stat-card clickable-card" @click="router.push('/alcohol')">
              <h3>Alcohol registration</h3>
              <p>Register age checks, serving times, and current shift history.</p>
              <p v-if="alcoholStatus === false" class="warning-text">No alcohol registration for today ⚠️</p>
              <p v-else-if="alcoholStatus === true" class="ok-text">OK ✓</p>
            </div>

            <div class="stat-card clickable-card" @click="router.push('/checklists')">
              <h3>Checklists</h3>
              <p v-if="alcoholStats.total === 0">No checklists found.</p>
              <p v-else>
                <span class="stat-number">{{ alcoholStats.completed }}</span> / {{ alcoholStats.total }} completed
              </p>
              <p v-if="alcoholStats.remaining > 0" class="warning-text">{{ alcoholStats.remaining }} tasks remaining ⚠️</p>
              <p v-else-if="alcoholStats.total > 0" class="ok-text">All done ✓</p>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import alcoholApi from '@/api/alcohol'
import checklistApi from '@/api/checklist'
import deviationApi from '@/api/deviation'

const authStore = useAuthStore()
const router = useRouter()
const alcoholStatus = ref(null)
const checklists = ref([])
const openDeviations = ref(0)

const matChecklists = computed(() =>
  checklists.value.filter(c => c.module !== 'BAR')
)

const alcoholChecklists = computed(() =>
  checklists.value.filter(c => c.module === 'BAR')
)

const matStats = computed(() => {
  const allItems = matChecklists.value.flatMap(c => c.items || [])
  const completed = allItems.filter(i => i.completed).length
  const total = allItems.length
  return { completed, total, remaining: total - completed }
})

const alcoholStats = computed(() => {
  const allItems = alcoholChecklists.value.flatMap(c => c.items || [])
  const completed = allItems.filter(i => i.completed).length
  const total = allItems.length
  return { completed, total, remaining: total - completed }
})

onMounted(async () => {
  try {
    const res = await alcoholApi.getAlcoholStatus()
    alcoholStatus.value = res.data.hasLogs
  } catch (e) {
    console.error('Failed to load alcohol status', e)
  }

  try {
    const res = await deviationApi.getAll()
    const deviations = Array.isArray(res.data) ? res.data : []
    openDeviations.value = deviations.filter(d => d.status === 'OPEN').length
  } catch (e) {
    console.error('Failed to load deviations', e)
  }

  try {
    const res = await checklistApi.getAll()
    checklists.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Failed to load checklists', e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.welcome-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}
.sections-grid {
  display: flex;
  flex-direction: column;
  gap: 30px;
}
.module-section {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}
.module-section h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #2c3e50;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}
.stat-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
}
.clickable-card {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}
.clickable-card:hover {
  transform: translateY(-2px);
  border-color: #2563eb;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.12);
}
.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2c3e50;
}
.warning-text {
  color: #dc2626;
  font-weight: 600;
}
.ok-text {
  color: #16a34a;
  font-weight: 600;
}
.deviation-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.badge {
  background: #dc2626;
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: bold;
}
</style>
