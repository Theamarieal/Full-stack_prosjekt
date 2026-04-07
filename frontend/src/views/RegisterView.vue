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
          <ul class="password-requirements">
            <li :class="{ met: password.length >= 8 }">At least 8 characters</li>
            <li :class="{ met: /[A-Z]/.test(password) }">At least one uppercase letter</li>
            <li :class="{ met: /[0-9]/.test(password) }">At least one number</li>
          </ul>
        </div>

        <div class="form-group">
          <label>Confirm password</label>
          <input v-model="confirmPassword" type="password" placeholder="Repeat your password" required />
          <p v-if="confirmPassword && password !== confirmPassword" class="field-error">
            Passwords do not match.
          </p>
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
const confirmPassword = ref('')
const role = ref('EMPLOYEE')
const error = ref('')
const loading = ref(false)

const validatePassword = (pwd) => {
  if (pwd.length < 8) return 'Password must be at least 8 characters.'
  if (!/[A-Z]/.test(pwd)) return 'Password must contain at least one uppercase letter.'
  if (!/[0-9]/.test(pwd)) return 'Password must contain at least one number.'
  return null
}

const handleRegister = async () => {
  error.value = ''

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.value)) {
    error.value = 'Must be a valid e-mail address.'
    return
  }

  const passwordError = validatePassword(password.value)
  if (passwordError) {
    error.value = passwordError
    return
  }

  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match.'
    return
  }

  loading.value = true
  try {
    const result = await auth.register(email.value, password.value, role.value, 1)
    if (result.success) {
      router.push('/')
    } else {
      error.value = result.message || 'Something went wrong. Try again.'
    }
  } catch {
    error.value = 'Could not create user. Maybe e-mail is already in use?'
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
  width: 100%;
  max-width: 440px;
  padding: 2rem 1.5rem;
  border-radius: 16px;
  border: 1px solid #e0dfd8;
}

h2 {
  text-align: center;
  color: #3C3489;
  margin-bottom: 4px;
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
  cursor: pointer;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.password-requirements {
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 0 0;
  font-size: 0.8rem;
}

.password-requirements li {
  color: #999;
  padding: 2px 0;
}

.password-requirements li::before {
  content: '✗ ';
}

.password-requirements li.met {
  color: #27ae60;
}

.password-requirements li.met::before {
  content: '✓ ';
}

.field-error {
  color: #d32f2f;
  font-size: 0.8rem;
  margin-top: 4px;
}

.error-message {
  color: #d32f2f;
  background: #ffebee;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.footer-links {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.85rem;
}

a {
  color: #4caf50;
  text-decoration: none;
  font-weight: bold;
}

@media (max-width: 350px) {
  .register-wrapper {
    padding: 0;
    background: white;
    align-items: flex-start;
  }

  .register-container {
    border: none;
    border-radius: 0;
    box-shadow: none;
    padding: 2rem 1.5rem;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  .footer-links {
    margin-top: auto;
    padding-bottom: 2rem;
  }
}
</style>
