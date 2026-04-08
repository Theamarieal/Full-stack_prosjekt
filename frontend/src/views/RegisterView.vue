<template>
  <div class="register-page">
    <div class="register-card">
      
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

      <div class="header-text">
        <h2>Create user</h2>
        <p class="subtitle">Become a part of Everest Sushi & Fusion AS</p>
      </div>

      <form @submit.prevent="handleRegister" novalidate>
        <div class="form-group">
          <label for="reg-email">E-mail</label>
          <input
            id="reg-email"
            v-model="email"
            type="email"
            autocomplete="email"
            placeholder="name@restaurant.com"
            required
            :aria-invalid="!!error"
            :aria-describedby="error ? 'register-error' : undefined"
          />
        </div>
      
        <div class="form-group">
          <label for="reg-password">Password</label>
          <input
            id="reg-password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            placeholder="At least 8 characters"
            required
            :aria-invalid="!!error"
            :aria-describedby="error ? 'register-error' : undefined"
          />
        </div>

        <div class="form-group">
          <label for="reg-role">Your role</label>
          <select
            id="reg-role"
            v-model="role"
            :aria-invalid="!!error"
            :aria-describedby="error ? 'register-error' : undefined"
          >
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
          </select>
        </div>
      
        <button type="submit" :disabled="loading" class="register-btn">
          {{ loading ? 'Creating account...' : 'Sign me up' }}
        </button>
      </form>

<p v-if="error" id="register-error" class="error-msg" role="alert" aria-live="assertive">
  <span aria-hidden="true">⚠ </span>{{ error }}
</p>
      
      <div class="footer-link">
        <span>Do you already have a user? </span>
        <router-link to="/login">Log in here</router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const role = ref('EMPLOYEE');
const error = ref('');
const loading = ref(false);

const handleRegister = async () => {
  error.value = '';
  loading.value = true;

  try {
    const success = await auth.register(
      email.value, 
      password.value, 
      role.value, 
      1
    );

    if (success) {
      router.push('/');
    } else {
      error.value = "Something went wrong. Try again.";
    }
  } catch (err) {
    error.value = "Could not create user. Maybe e-mail is already in use?";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background-color: #f7f6f2;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border: 1px solid #e0dfd8;
  border-radius: 16px;
  padding: 3rem 2rem;
  box-shadow: 0 4px 20px rgba(60, 52, 137, 0.05);
  display: flex;
  flex-direction: column;
}

.branding { text-align: center; margin-bottom: 2rem; }

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
.slogan { font-size: 0.85rem; color: #5a529f; text-transform: uppercase; letter-spacing: 0.15em; }

.header-text { text-align: center; margin-bottom: 1.5rem; }
h2 { color: #3C3489; font-weight: 700; margin-bottom: 4px; }
.subtitle { color: #4b5563; font-size: 0.95rem; }

.form-group { display: flex; flex-direction: column; margin-bottom: 1.25rem; }
label { font-weight: 600; margin-bottom: 6px; color: #3C3489; font-size: 0.9rem; }
input, select { 
  padding: 12px; 
  border: 1.5px solid #e0dfd8; 
  border-radius: 10px; 
  font-size: 16px; 
  width: 100%; 
  background: #fafaf8; 
  color: #2c2c2a;
}
input:focus, select:focus { outline: none; border-color: #7F77DD; background: #ffffff; }

.register-btn {
  width: 100%; padding: 14px;
  background: #534AB7; color: white;
  border: none; border-radius: 10px;
  font-weight: 700; cursor: pointer; transition: background 0.2s;
  margin-top: 1rem;
}
.register-btn:hover { background: #3C3489; }

.error-msg {
  color: #991b1b;
  background: #fff5f5;
  border: 1px solid #fecaca;
  border-radius: 10px;
  padding: 12px;
  font-size: 0.9rem;
  font-weight: 700;
  margin-top: 1rem;
  text-align: center;
}

.footer-link { text-align: center; margin-top: 2rem; font-size: 0.9rem; color: #4b5563; }
.footer-link a { color: #534AB7; font-weight: 700; text-decoration: underline; }

@media (max-width: 350px) {
  .register-page {
    padding: 0;
    background-color: #ffffff;
    align-items: flex-start;
  }

  .register-card {
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