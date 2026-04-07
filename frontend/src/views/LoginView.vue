<template>
  <div class="login-page">
    <div class="login-card">
      
      <div class="branding">
        <div class="logo-icon" aria-hidden="true">
          <div class="logo-line line-1"></div>
          <div class="logo-line line-2"></div>
          <div class="logo-line line-3"></div>
          <div class="logo-line line-4"></div>
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

      <form @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label for="email">E-mail</label>
          <input 
            id="email" 
            v-model="email" 
            type="email" 
            required 
            placeholder="name@restaurant.com" 
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input 
            id="password" 
            v-model="password" 
            type="password" 
            required 
            placeholder="••••••••" 
          />
        </div>

        <div v-if="error" class="error-msg" role="alert">{{ error }}</div>

        <button type="submit" :disabled="loading" class="login-btn">
          {{ loading ? 'Logging in...' : 'Log in' }}
        </button>
      </form>

      <p class="dev-hint">Test: <code>employee@test.no</code> / <code>password123</code></p>

      <div class="footer-link">
        <span>Not a user here? </span>
        <router-link to="/register">Sign up here</router-link>
      </div>

    </div>
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
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background-color: #f7f6f2;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border: 1px solid #e0dfd8;
  border-radius: 16px;
  padding: 3rem 2rem;
  box-shadow: 0 4px 20px rgba(60, 52, 137, 0.05);
  display: flex;
  flex-direction: column;
}

.branding { text-align: center; margin-bottom: 2.5rem; }

.logo-icon {
  position: relative;
  width: 72px; height: 72px;
  background: #534AB7;
  border-radius: 20px;
  margin: 0 auto 1rem;
  display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 5px;
}

.logo-line { height: 6px; border-radius: 3px; }
.line-1 { width: 34px; background: #FAC775; opacity: 0.9; }
.line-2 { width: 26px; background: #9FE1CB; opacity: 0.85; }
.line-3 { width: 30px; background: #F5C4B3; opacity: 0.8; }
.line-4 { width: 20px; background: #B5D4F4; opacity: 0.7; }

.logo-check {
  position: absolute; bottom: -6px; right: -6px;
  width: 26px; height: 26px;
  background: #26215C; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2px solid #ffffff;
}
.logo-check svg { width: 14px; height: 14px; }

.app-name { font-size: 2.25rem; font-weight: 800; color: #3C3489; margin: 0; letter-spacing: -0.5px; }
.slogan { font-size: 0.85rem; color: #7F77DD; text-transform: uppercase; letter-spacing: 0.15em; }

.form-group { display: flex; flex-direction: column; margin-bottom: 1.25rem; }
label { font-weight: 600; margin-bottom: 6px; color: #3C3489; font-size: 0.9rem; }
input { padding: 12px; border: 1.5px solid #e0dfd8; border-radius: 10px; font-size: 16px; width: 100%; background: #fafaf8; }
input:focus { outline: none; border-color: #7F77DD; background: #ffffff; }

.login-btn {
  width: 100%; padding: 14px;
  background: #534AB7; color: white;
  border: none; border-radius: 10px;
  font-weight: 700; cursor: pointer; transition: background 0.2s;
}
.login-btn:hover { background: #3C3489; }

.footer-link { text-align: center; margin-top: 2rem; font-size: 0.9rem; color: #666; }
.footer-link a { color: #534AB7; font-weight: 600; text-decoration: none; }

.dev-hint { font-size: 0.75rem; color: #999; text-align: center; margin-top: 1rem; }

@media (max-width: 350px) {
  .login-page {
    padding: 0;
    background-color: #ffffff;
    align-items: flex-start;
  }

  .login-card {
    border: none;
    border-radius: 0;
    box-shadow: none;
    min-height: 100vh;
    padding: 3rem 1.5rem;
  }

  .footer-link {
    margin-top: auto;
    padding-bottom: 2rem;
  }
}
</style>