<template>
  <div class="admin-wrapper">
    <div class="admin-container">
      <h2>User Management</h2>
      <p class="subtitle">Admin panel: manage users and roles</p>

      <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <section class="card">
        <h3>Create new user</h3>
        <div class="form-group">
          <label>E-mail</label>
          <input v-model="newUser.email" type="email" placeholder="name@bedrift.no" />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="newUser.password" type="password" placeholder="At least 8 characters" />
        </div>
        <div class="form-group">
          <label>Role</label>
          <select v-model="newUser.role">
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <button @click="handleCreateUser" :disabled="loading">
          {{ loading ? 'Creating...' : 'Create user' }}
        </button>
      </section>

      <section class="card">
        <h3>All users</h3>
        <p v-if="users.length === 0" class="empty-state">No users found.</p>
        <table v-else>
          <thead>
          <tr>
            <th>E-mail</th>
            <th>Role</th>
            <th>Change role</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.email }}</td>
            <td>
              <span :class="['role-badge', roleBadgeClass(user.role)]">{{ user.role }}</span>
            </td>
            <td>
              <div class="role-change">
                <select v-model="user.pendingRole">
                  <option value="EMPLOYEE">Employee</option>
                  <option value="MANAGER">Manager</option>
                  <option value="ADMIN">Admin</option>
                </select>
                <button
                  class="btn-save"
                  @click="handleChangeRole(user)"
                  :disabled="user.pendingRole === user.role"
                >
                  Save
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, createUser, updateUserRole } from '@/api/admin'

const users = ref([])
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const newUser = ref({
  email: '',
  password: '',
  role: 'EMPLOYEE',
})

function showSuccess(msg) {
  successMessage.value = msg
  errorMessage.value = ''
  setTimeout(() => (successMessage.value = ''), 3000)
}

function showError(msg) {
  errorMessage.value = msg
  successMessage.value = ''
}

function roleBadgeClass(role) {
  return {
    ADMIN: 'badge-admin',
    MANAGER: 'badge-manager',
    EMPLOYEE: 'badge-employee',
  }[role] || ''
}

async function fetchUsers() {
  try {
    const data = await getUsers()
    users.value = data.map((u) => ({ ...u, pendingRole: u.role }))
  } catch {
    // fall back to mock data while backend is not ready
    users.value = mockUsers.map((u) => ({ ...u }))
  }
}

async function handleCreateUser() {
  if (!newUser.value.email || !newUser.value.password) {
    showError('E-mail and password are required.')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(newUser.value.email)) {
    showError('Must be a valid e-mail address.')
    return
  }

  if (newUser.value.password.length < 8) {
    showError('Password must be at least 8 characters.')
    return
  }

  loading.value = true
  try {
    await createUser(newUser.value)
    showSuccess(`User ${newUser.value.email} created.`)
    newUser.value = { email: '', password: '', role: 'EMPLOYEE' }
    await fetchUsers()
  } catch (err) {
    const data = err.response?.data
    const rawMessage = data?.errors?.[0] || data?.error || 'Something went wrong. Try again.'
    const message = rawMessage.includes(': ') ? rawMessage.split(': ').slice(1).join(': ') : rawMessage
    showError(message)
  } finally {
    loading.value = false
  }
}

async function handleChangeRole(user) {
  try {
    await updateUserRole(user.id, user.pendingRole)
    user.role = user.pendingRole
    showSuccess(`Role updated to ${user.role} for ${user.email}.`)
  } catch {
    showError('Could not update role. Try again.')
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-wrapper {
  display: flex;
  justify-content: center;
  padding: 2rem 1rem;
}

.admin-container {
  width: 100%;
  max-width: 800px;
}

h2 {
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #666;
  margin-bottom: 1.5rem;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

h3 {
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: bold;
  color: #2c3e50;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

label {
  font-weight: bold;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

input,
select {
  padding: 0.6rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  padding: 0.7rem 1.4rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 0.5rem;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 0.6rem 0.8rem;
  background: #f0f0f0;
  font-size: 0.85rem;
  color: #555;
}

td {
  padding: 0.7rem 0.8rem;
  border-bottom: 1px solid #eee;
  font-size: 0.95rem;
  vertical-align: middle;
}

.role-change {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.role-change select {
  padding: 0.4rem;
  font-size: 0.9rem;
}

.btn-save {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
  margin-top: 0;
}

.role-badge {
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.badge-admin {
  background: #fdecea;
  color: #c0392b;
}

.badge-manager {
  background: #fff8e1;
  color: #f39c12;
}

.badge-employee {
  background: #e8f5e9;
  color: #27ae60;
}

.empty-state {
  color: #999;
  font-style: italic;
}

.success-message {
  color: #27ae60;
  background: #e8f5e9;
  padding: 0.5rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.error-message {
  color: #d32f2f;
  background: #ffebee;
  padding: 0.5rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}
</style>
