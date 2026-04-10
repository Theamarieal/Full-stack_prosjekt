<template>
  <div class="admin-wrapper">
    <div class="admin-container">
      <div class="page-header">
        <div class="page-header-text">
          <h1>Administration</h1>
          <p class="subtitle">Manage users and organizations</p>
        </div>
        <button type="button" class="back-btn-minimal" @click="router.push('/')">
          ← Dashboard
        </button>
      </div>

      <p v-if="successMessage" class="success-message" role="status" aria-live="polite">
        <span aria-hidden="true">✓ </span>{{ successMessage }}
      </p>
      <p v-if="errorMessage" class="error-message" role="alert">
        <span aria-hidden="true">⚠ </span>{{ errorMessage }}
      </p>

      <!-- Create new organization -->
      <section class="card" aria-labelledby="create-org-heading">
        <h2 id="create-org-heading">Create new organization</h2>

        <div class="form-group">
          <label for="org-name">Organization name</label>
          <input
            id="org-name"
            v-model="newOrg.organizationName"
            type="text"
            placeholder="Restaurant name"
          />
        </div>

        <div class="form-group">
          <label for="org-admin-email">Admin e-mail</label>
          <input
            id="org-admin-email"
            v-model="newOrg.adminEmail"
            type="email"
            placeholder="admin@restaurant.no"
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <label for="org-admin-password">Admin password</label>
          <input
            id="org-admin-password"
            v-model="newOrg.adminPassword"
            type="password"
            placeholder="At least 8 characters"
            autocomplete="new-password"
          />
          <ul class="password-requirements">
            <li :class="{ met: newOrg.adminPassword.length >= 8 }">At least 8 characters</li>
            <li :class="{ met: /[A-Z]/.test(newOrg.adminPassword) }">At least one uppercase letter</li>
            <li :class="{ met: /[0-9]/.test(newOrg.adminPassword) }">At least one number</li>
          </ul>
        </div>

        <button class="primary-btn" @click="handleCreateOrganization" :disabled="orgLoading">
          {{ orgLoading ? 'Creating...' : 'Create organization' }}
        </button>
      </section>

      <!-- Create new user -->
      <section class="card" aria-labelledby="create-user-heading">
        <h2 id="create-user-heading">Create new user in {{ orgName }}</h2>

        <div class="form-group">
          <label for="new-user-email">E-mail</label>
          <input
            id="new-user-email"
            v-model="newUser.email"
            type="email"
            placeholder="name@bedrift.no"
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <label for="new-user-password">Password</label>
          <input
            id="new-user-password"
            v-model="newUser.password"
            type="password"
            placeholder="At least 8 characters"
            autocomplete="new-password"
            aria-describedby="password-rules"
          />
          <ul id="password-rules" class="password-requirements">
            <li :class="{ met: newUser.password.length >= 8 }">At least 8 characters</li>
            <li :class="{ met: /[A-Z]/.test(newUser.password) }">At least one uppercase letter</li>
            <li :class="{ met: /[0-9]/.test(newUser.password) }">At least one number</li>
          </ul>
        </div>

        <div class="form-group">
          <label for="new-user-confirm-password">Confirm password</label>
          <input
            id="new-user-confirm-password"
            v-model="newUser.confirmPassword"
            type="password"
            placeholder="Repeat your password"
            autocomplete="new-password"
          />
          <p
            v-if="newUser.confirmPassword && newUser.password !== newUser.confirmPassword"
            class="field-error"
          >
            Passwords do not match.
          </p>
        </div>

        <div class="form-group">
          <label for="new-user-role">Role</label>
          <select id="new-user-role" v-model="newUser.role">
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>

        <button class="primary-btn" @click="handleCreateUser" :disabled="loading">
          {{ loading ? 'Creating...' : 'Create user' }}
        </button>
      </section>

      <!-- All users -->
      <section class="card" aria-labelledby="all-users-heading">
        <h2 id="all-users-heading">All users</h2>
        <p v-if="users.length === 0" class="empty-state">No users found.</p>
        <div v-else class="table-wrapper">
          <table>
            <thead>
            <tr>
              <th scope="col">E-mail</th>
              <th scope="col">Status</th>
              <th scope="col">Role</th>
              <th scope="col">Change role</th>
              <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in users" :key="user.id" :class="{ 'row-inactive': !user.active }">
              <td data-label="E-mail">{{ user.email }}</td>
              <td data-label="Status">
                  <span :class="['status-badge', user.active ? 'badge-active' : 'badge-inactive']">
                    {{ user.active ? 'Active' : 'Inactive' }}
                  </span>
              </td>
              <td data-label="Role">
                  <span :class="['role-badge', roleBadgeClass(user.role)]">
                    {{ user.role }}
                  </span>
              </td>
              <td data-label="Change role">
                <div class="role-change">
                  <label class="sr-only" :for="`role-${user.id}`">Change role for {{ user.email }}</label>
                  <select
                    :id="`role-${user.id}`"
                    v-model="user.pendingRole"
                    :disabled="!user.active"
                  >
                    <option value="EMPLOYEE">Employee</option>
                    <option value="MANAGER">Manager</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                  <button
                    class="btn-save"
                    @click="handleChangeRole(user)"
                    :disabled="user.pendingRole === user.role || !user.active"
                  >
                    Save
                  </button>
                </div>
              </td>
              <td data-label="Actions">
                <div class="action-buttons">
                  <button class="btn-deactivate" @click="handleToggleActive(user)">
                    {{ user.active ? 'Deactivate' : 'Activate' }}
                  </button>
                  <button class="btn-delete" @click="handleDeleteUser(user)">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
/**
 * AdminView
 *
 * Administration panel accessible only to users with the ADMIN role.
 * Allows admins to manage users within their organization, including
 * creating new users, updating roles, toggling active status, and deleting accounts.
 * Also supports creating new organizations.
 */
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()
const orgName = authStore.user?.organization?.name || 'your organization'
import { getUsers, createUser, updateUserRole, deleteUser, toggleUserActive, createOrganization } from '@/api/admin'

const users = ref([])
const loading = ref(false)
const orgLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const newUser = ref({
  email: '',
  password: '',
  confirmPassword: '',
  role: 'EMPLOYEE',
})

const newOrg = ref({
  organizationName: '',
  adminEmail: '',
  adminPassword: '',
})

function showSuccess(msg) {
  successMessage.value = msg
  errorMessage.value = ''
  setTimeout(() => (successMessage.value = ''), 4000)
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

function validatePassword(pwd) {
  if (pwd.length < 8) return 'Password must be at least 8 characters.'
  if (!/[A-Z]/.test(pwd)) return 'Password must contain at least one uppercase letter.'
  if (!/[0-9]/.test(pwd)) return 'Password must contain at least one number.'
  return null
}

async function fetchUsers() {
  try {
    const data = await getUsers()
    users.value = data.map((u) => ({ ...u, pendingRole: u.role }))
  } catch {
    showError('Could not load users.')
  }
}

async function handleCreateOrganization() {
  if (!newOrg.value.organizationName || !newOrg.value.adminEmail || !newOrg.value.adminPassword) {
    showError('All fields are required.')
    return
  }

  const passwordError = validatePassword(newOrg.value.adminPassword)
  if (passwordError) {
    showError(passwordError)
    return
  }

  orgLoading.value = true
  try {
    const result = await createOrganization({
      organizationName: newOrg.value.organizationName,
      adminEmail: newOrg.value.adminEmail,
      adminPassword: newOrg.value.adminPassword,
    })
    showSuccess(`Organization "${result.organizationName}" created. Admin: ${result.adminEmail}`)
    newOrg.value = { organizationName: '', adminEmail: '', adminPassword: '' }
  } catch (err) {
    const data = err.response?.data
    const rawMessage = data?.errors?.[0] || data?.error || 'Something went wrong. Try again.'
    const message = rawMessage.includes(': ') ? rawMessage.split(': ').slice(1).join(': ') : rawMessage
    showError(message)
  } finally {
    orgLoading.value = false
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

  const passwordError = validatePassword(newUser.value.password)
  if (passwordError) {
    showError(passwordError)
    return
  }

  if (newUser.value.password !== newUser.value.confirmPassword) {
    showError('Passwords do not match.')
    return
  }

  loading.value = true
  try {
    await createUser({
      email: newUser.value.email,
      password: newUser.value.password,
      role: newUser.value.role,
    })
    showSuccess(`User ${newUser.value.email} created.`)
    newUser.value = { email: '', password: '', confirmPassword: '', role: 'EMPLOYEE' }
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
    window.alert('Could not update role. Try again.')
  }
}

async function handleToggleActive(user) {
  const action = user.active ? 'deactivate' : 'activate'
  if (!confirm(`Are you sure you want to ${action} ${user.email}?`)) return

  try {
    await toggleUserActive(user.id, !user.active)
    user.active = !user.active
    showSuccess(`User ${user.email} ${user.active ? 'activated' : 'deactivated'}.`)
  } catch {
    window.alert(`Could not ${action} user. Try again.`)
  }
}

async function handleDeleteUser(user) {
  if (!confirm(`Are you sure you want to permanently delete ${user.email}?`)) return
  try {
    await deleteUser(user.id)
    showSuccess(`User ${user.email} deleted.`)
    await fetchUsers()
  } catch (err) {
    const message = err.response?.data?.error || 'Could not delete user. Try again.'
    window.alert(message)
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-wrapper {
  display: flex;
  justify-content: center;
  padding: 2rem 1rem;
  background: #f7f6f2;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 1.5rem;
}

.page-header-text {
  flex: 1;
}

.back-btn-minimal {
  background: transparent;
  color: #534AB7;
  border: 1.5px solid #e0dfd8;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.back-btn-minimal:hover {
  background: #fafaf8;
}

.admin-container {
  width: 100%;
  max-width: 1100px;
}

h1 {
  margin-bottom: 0.5rem;
  color: #3C3489;
  font-size: 2rem;
}

h2 {
  margin-bottom: 1rem;
  font-size: 1.3rem;
  color: #3C3489;
}

.subtitle {
  color: #4b5563;
  margin-bottom: 0;
}

.card {
  background: white;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(60, 52, 137, 0.06);
  border: 1px solid #e0dfd8;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

label {
  font-weight: 700;
  font-size: 0.95rem;
  margin-bottom: 0.4rem;
  color: #3C3489;
}

input,
select {
  padding: 0.8rem 0.9rem;
  border: 1.5px solid #d1d5db;
  border-radius: 10px;
  font-size: 1rem;
  background: #fafaf8;
  width: 100%;
}

input:focus,
select:focus,
button:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.primary-btn {
  padding: 0.85rem 1.4rem;
  background-color: #534AB7;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  margin-top: 0.5rem;
}

.primary-btn:hover {
  background: #3C3489;
}

button:disabled {
  background-color: #c7c7c7;
  cursor: not-allowed;
}

.password-requirements {
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 0 0;
  font-size: 0.85rem;
}

.password-requirements li {
  color: #6b7280;
  padding: 2px 0;
}

.password-requirements li::before {
  content: '✗ ';
}

.password-requirements li.met {
  color: #166534;
  font-weight: 600;
}

.password-requirements li.met::before {
  content: '✓ ';
}

.field-error {
  color: #991b1b;
  font-size: 0.85rem;
  margin-top: 6px;
  font-weight: 600;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

th {
  text-align: left;
  padding: 0.8rem;
  background: #f3f4f6;
  font-size: 0.9rem;
  color: #374151;
}

.row-inactive {
  opacity: 0.65;
}

.role-change {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.role-change select {
  min-width: 140px;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-save,
.btn-deactivate,
.btn-delete {
  padding: 0.55rem 0.9rem;
  font-size: 0.9rem;
  margin-top: 0;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 700;
  cursor: pointer;
}

.role-badge,
.status-badge {
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
  display: inline-block;
}

.badge-inactive {
  background: #f3f4f6;
  color: #4b5563;
}

.empty-state {
  color: #6b7280;
  font-style: italic;
}

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

.btn-save { background: #6b7280; }
.btn-deactivate { background-color: #d97706; }
.btn-delete { background-color: #dc2626; }
.badge-admin { background: #fdecea; color: #b91c1c; }
.badge-manager { background: #fff7db; color: #b45309; }
.badge-employee { background: #e8f5e9; color: #166534; }
.badge-active { background: #e8f5e9; color: #166534; }

.success-message {
  color: #166534;
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  margin-bottom: 1rem;
  font-weight: 600;
}

.error-message {
  color: #991b1b;
  background: #fff5f5;
  border: 1px solid #fecaca;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  margin-bottom: 1rem;
  font-weight: 600;
}

td {
  padding: 0.9rem 0.8rem;
  border-bottom: 1px solid #eee;
  font-size: 0.95rem;
  vertical-align: middle;
}

.btn-save:focus-visible,
.btn-deactivate:focus-visible,
.btn-delete:focus-visible,
.primary-btn:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.btn-save:hover { background: #4b5563; }
.btn-deactivate:hover { background: #b45309; }
.btn-delete:hover { background: #b91c1c; }

@media (max-width: 768px) {
  .admin-wrapper { padding: 1rem 0.75rem; }
  .card { padding: 1rem; }
  .action-buttons, .role-change { flex-direction: column; align-items: stretch; }
  .btn-save, .btn-deactivate, .btn-delete { width: 100%; }
  .role-change select { width: 100%; min-width: 0; }
  .back-btn-minimal { width: 100%; text-align: center;}
  .page-header{ flex-direction: column; align-items: stretch;}
}
</style>
