<template>
  <div class="login-page">
    <div class="login-card">

      <div class="branding">
        <div class="logo-icon" aria-hidden="true">
          <!-- checklist line things -->
          <div class="logo-line line-1"></div>
          <div class="logo-line line-2"></div>
          <div class="logo-line line-3"></div>
          <div class="logo-line line-4"></div>
          <!-- checkmark circle -->
          <div class="logo-check">
            <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <polyline points="5,12 10,17 19,7" stroke="#CECBF6" stroke-width="2.5"
                stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </div>
        <h1 class="app-name">Checkd</h1>
        <p class="slogan">Kjekt. Enkelt. Ordentlig.</p>
      </div>

      <!-- login form -->
      <form @submit.prevent="handleLogin" novalidate>

        <div class="form-group">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="email"
            type="email"
            required
            autocomplete="email"
            placeholder="name@restaurant.com"
            :class="{ 'input-error': !!error }"
            :aria-describedby="error ? 'login-error' : undefined"
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            autocomplete="current-password"
            placeholder="••••••••"
            :class="{ 'input-error': !!error }"
            :aria-describedby="error ? 'login-error' : undefined"
          />
        </div>

        <!-- error message -->
        <div v-if="error" id="login-error" class="error-msg" role="alert">
          <span class="error-icon" aria-hidden="true">!</span>
          {{ error }}
        </div>

        <button type="submit" :disabled="loading" class="login-btn">
          <span v-if="loading" class="spinner" aria-hidden="true"></span>
          {{ loading ? 'Logging in...' : 'Log in' }}
        </button>

      </form>

      <!-- test credentials hint (remove before production)!!!! -->
      <p class="dev-hint">
        Test: <code>employee@test.no</code> / <code>password123</code>
      </p>

    </div>
  </div>

<div class="footer-link">
  <span>Not a user here? </span>
  <router-link to="/register">Sign up here</router-link>
</div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  // clear previous error before new attempt
  loading.value = true
  error.value = ""

  const success = await authStore.login(email.value, password.value)

  if (success) {
    router.push('/')
  } else {
    error.value = 'Wrong e-mail or password. Please try again.'
  }

  loading.value = false
}
</script>

<style scoped>
/* page layout */
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f7f6f2;
  padding: 1rem;
}

/* card */
.login-card {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  border: 0.5px solid #e0dfd8;
  border-radius: 16px;
  padding: 2.5rem 2rem;
}

/* branding */
.branding {
  text-align: center;
  margin-bottom: 2rem;
}

/* logo icon */
.logo-icon {
  position: relative;
  width: 72px;
  height: 72px;
  background: #534AB7;
  border-radius: 20px;
  margin: 0 auto 1rem;
  padding: 10px 12px 10px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
}

.logo-line {
  height: 6px;
  border-radius: 3px;
}
.line-1 { width: 34px; background: #FAC775; opacity: 0.9; }
.line-2 { width: 26px; background: #9FE1CB; opacity: 0.85; }
.line-3 { width: 30px; background: #F5C4B3; opacity: 0.8; }
.line-4 { width: 20px; background: #B5D4F4; opacity: 0.7; }

/* checkmark circle sits bottom-right of the logo, can me moved if better placement */
.logo-check {
  position: absolute;
  bottom: -6px;
  right: -6px;
  width: 26px;
  height: 26px;
  background: #26215C;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #ffffff;
}
.logo-check svg {
  width: 14px;
  height: 14px;
}

.app-name {
  font-size: 2rem;
  font-weight: 800;
  color: #3C3489;
  margin: 0 0 0.25rem;
  letter-spacing: -0.5px;
}

.slogan {
  font-size: 0.85rem;
  color: #7F77DD;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin: 0;
}

/* form */
.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #3C3489;
  margin-bottom: 0.4rem;
}

input {
  padding: 0.7rem 0.9rem;
  border: 1.5px solid #e0dfd8;
  border-radius: 8px;
  font-size: 1rem;
  background: #fafaf8;
  color: #2C2C2A;
  transition: border-color 0.15s;
  min-height: 44px;
}

input:focus {
  outline: none;
  border-color: #7F77DD;
  background: #ffffff;
}

/* red border for error with more than just color*/
input.input-error {
  border-color: #E24B4A;
}

/* error message */
.error-msg {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #A32D2D;
  background: #FCEBEB;
  border: 1px solid #F7C1C1;
  border-radius: 8px;
  padding: 0.6rem 0.9rem;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.error-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background: #E24B4A;
  color: white;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

/* login button */
.login-btn {
  width: 100%;
  min-height: 44px;
  padding: 0.75rem;
  background: #534AB7;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: background 0.15s;
}

.login-btn:hover:not(:disabled) {
  background: #3C3489;
}

.login-btn:focus-visible {
  outline: 3px solid #7F77DD;
  outline-offset: 2px;
}

.login-btn:disabled {
  background: #B4B2A9;
  cursor: not-allowed;
}

/* loading spinner */
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* dev hint */
.dev-hint {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.78rem;
  color: #888780;
  border-top: 1px solid #f0efe8;
  padding-top: 1rem;
}
.dev-hint code {
  background: #f1efe8;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 0.78rem;
}
</style>
