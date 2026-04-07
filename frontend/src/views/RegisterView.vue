<template>
  <div class="register-wrapper">
    <div class="register-container">
      <h2>Create user</h2>
      <p class="subtitle">Become a part of Testrestaurant AS</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>E-mail</label>
          <input v-model="email" type="email" placeholder="name@bedrift.no" required />
        </div>

        <div class="form-group">
          <label>Password</label>
          <input v-model="password" type="password" placeholder="At least 8 characters" required />
        </div>

        <div class="form-group">
          <label>Your role</label>
          <select v-model="role">
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
          </select>
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Creating account...' : 'Sign me up' }}
        </button>
      </form>

      <p v-if="error" class="error-message">{{ error }}</p>

      <div class="footer-links">
        <span>Do you already have a user? </span>
        <router-link to="/login">Log in here</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const role = ref('EMPLOYEE')
const error = ref('')
const loading = ref(false)

const handleRegister = async () => {
  error.value = ''
  loading.value = true

  try {
    const result = await auth.register(email.value, password.value, role.value, 1)

    if (result.success) {
      router.push('/')
    } else {
      error.value = result.message || 'Something went wrong. Try again.'
    }
  } catch (err) {
    error.value = 'Could not create user.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f7f6f2;
  padding: 12px;
}

.register-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 440px;
  padding: 2rem 1.5rem;
  border-radius: 16px;
  border: 1px solid #e0dfd8;
}

h2 {
  text-align: center;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 4px;
}

input,
select {
  width: 100%;
  padding: 12px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  font-size: 16px;
}

button {
  width: 100%;
  padding: 14px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  margin-top: 1rem;
}

.footer-links {
  text-align: center;
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.9rem;
}

a {
  color: #4caf50;
  text-decoration: none;
  font-weight: bold;
}
</style>
