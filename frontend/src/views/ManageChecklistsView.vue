<template>
  <div class="manage-checklists">
    <h1>Administer checklists</h1>
    
    <section class="create-section card">
      <h2>Create new checklist</h2>
      <form @submit.prevent="handleCreate">
        <div class="form-grid">
          <input v-model="form.title" placeholder="Title (for example Morningroutine Kitchen)" required />
          <select v-model="form.frequency">
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
          </select>
          <select v-model="form.module">
            <option value="KITCHEN">Kitchen</option>
            <option value="BAR">Bar</option>
            <option value="FLOOR">Floor</option>
          </select>
        </div>
        <textarea v-model="form.description" placeholder="Description of checklist..."></textarea>

        <div class="items-management">
          <h3>Checklists</h3>
          <div v-for="(item, index) in form.items" :key="index" class="item-row">
            <input v-model="form.items[index]" placeholder="What needs to be done?" required />
            <button type="button" @click="removeItem(index)" class="btn-remove-item">✖</button>
          </div>
          <button type="button" @click="addItem" class="btn-secondary">+ Add point</button>
        </div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving...' : 'Create checklist' }}
        </button>
      </form>
    </section>

    <section class="list-section">
      <h2>Active checklists</h2>
      <div v-if="checklists.length === 0" class="empty-state">No checklists created yet.</div>
      
      <div v-for="list in checklists" :key="list.id" class="checklist-card card">
        <div class="info">
          <h3>{{ list.title }}</h3>
          <p>{{ list.description }}</p>
          <div class="badges">
            <span class="badge">{{ list.frequency }}</span>
            <span class="badge">{{ list.module }}</span>
          </div>
        </div>
        <div class="actions">
          <button @click="handleDelete(list.id)" class="btn-delete" title="Delete checklist">
            Delete
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const checklists = ref([]);
const loading = ref(false);

const form = ref({
  title: '',
  description: '',
  frequency: 'DAILY',
  module: 'KITCHEN',
  items: [''] 
});

const fetchChecklists = async () => {
  try {
    const response = await api.get('/checklists');
    checklists.value = response.data;
  } catch (err) {
    console.error("Could not fetch lists");
  }
};

const addItem = () => form.value.items.push('');
const removeItem = (index) => form.value.items.splice(index, 1);

const handleCreate = async () => {
  if (form.value.items.some(item => !item.trim())) {
    alert("Please fill out all checklists or remove the empty ones.");
    return;
  }

  loading.value = true;
  try {
    await api.post('/checklists', form.value);
    // Reset form after sucess
    form.value = { title: '', description: '', frequency: 'DAILY', module: 'KITCHEN', items: [''] };
    await fetchChecklists(); 
    alert("Checklist created!");
  } catch (err) {
    alert("Error with saving. Check that all fields are filled out.");
  } finally {
    loading.value = false;
  }
};

// functionality to delete list
const handleDelete = async (id) => {
  if (!confirm("Are you sure you want to delete this checklist? It cannot be undone.")) {
    return;
  }

  try {
    await api.delete(`/checklists/${id}`);
    await fetchChecklists(); // update list after deletion
  } catch (err) {
    console.error(err);
    alert("Could not delete checklist. Do you have rights for this?");
  }
};

onMounted(fetchChecklists);
</script>

<style scoped>
.manage-checklists { max-width: 800px; margin: 0 auto; padding: 20px; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }

/* styling */
.form-grid { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 10px; margin-bottom: 10px; }
input, select, textarea { padding: 8px; border-radius: 4px; border: 1px solid #ccc; font-size: 1rem; }
textarea { width: 100%; height: 80px; margin-bottom: 15px; }

.items-management { margin-bottom: 20px; }
.item-row { display: flex; gap: 10px; margin-bottom: 8px; }
.item-row input { flex: 1; }

/* checklist card in list */
.checklist-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info h3 { margin-bottom: 5px; color: #2c3e50; }
.info p { color: #666; margin-bottom: 10px; font-size: 0.9rem; }
.badges { display: flex; gap: 5px; }

/* buttons */
.btn-primary { background: #4CAF50; color: white; padding: 10px 24px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-secondary { background: #f8f9fa; border: 1px solid #ddd; padding: 8px 16px; cursor: pointer; margin-top: 5px; border-radius: 4px; }
.btn-remove-item { background: #eee; border: none; padding: 0 12px; border-radius: 4px; cursor: pointer; color: #666; }
.btn-remove-item:hover { background: #ff4444; color: white; }

.btn-delete {
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-delete:hover {
  background: #e74c3c;
  color: white;
}

.badge { background: #e8f5e9; color: #2e7d32; padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: bold; text-transform: uppercase; }
.empty-state { text-align: center; color: #999; padding: 40px; font-style: italic; }
</style>