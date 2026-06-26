<template>
  <section class="eco-section eco-section--auth">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-12 col-sm-8 col-md-6 col-lg-5">
          <div class="eco-card auth-card p-5">
            <div class="text-center mb-4">
              <h2 class="eco-title mb-2">Пријави се</h2>
              <p class="text-muted">Добродојде назад на ЕкоСреќа! 🌿</p>
            </div>

            <form @submit.prevent="handleLogin">
              <!-- Username -->
              <div class="mb-3">
                <label for="username" class="form-label">Корисничко име</label>
                <input
                  id="username"
                  v-model="form.username"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.username }"
                  placeholder="твое_корисничко_име"
                  required
                />
                <div v-if="errors.username" class="invalid-feedback d-block">
                  {{ errors.username }}
                </div>
              </div>

              <!-- Password -->
              <div class="mb-4">
                <label for="password" class="form-label">Лозинка</label>
                <input
                  id="password"
                  v-model="form.password"
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': errors.password }"
                  placeholder="••••••••"
                  required
                />
                <div v-if="errors.password" class="invalid-feedback d-block">
                  {{ errors.password }}
                </div>
              </div>

              <!-- Error Message -->
              <div v-if="authStore.error" class="alert alert-danger alert-dismissible fade show">
                {{ authStore.error }}
                <button type="button" class="btn-close" @click="authStore.clearError()"></button>
              </div>

              <!-- Submit Button -->
              <button type="submit" class="btn eco-btn w-100 mb-3" :disabled="authStore.loading">
                <span v-if="!authStore.loading">Пријави се</span>
                <span v-else>
                  <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  Пријавување...
                </span>
              </button>
            </form>

            <!-- Signup Link -->
            <div class="text-center">
              <p class="text-muted mb-0">
                Немаш профил?
                <RouterLink to="/signup" class="text-success text-decoration-none fw-bold"
                  >Регистрирај се тука</RouterLink
                >
              </p>
            </div>
          </div>

          <!-- Decorative Elements -->
          <div class="auth-decoration">
            <div class="decoration-leaf leaf-1">🌱</div>
            <div class="decoration-leaf leaf-2">🍃</div>
            <div class="decoration-leaf leaf-3">🌿</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '../stores/authStore';

  const router = useRouter();
  const authStore = useAuthStore();

  const form = reactive({
    username: '',
    password: '',
  });

  const errors = ref({});

  const handleLogin = async () => {
    errors.value = {};

    if (!form.username) {
      errors.value.username = 'Корисничкото име е задолжително';
    }

    if (!form.password) {
      errors.value.password = 'Лозинка е задолжителна';
    } else if (form.password.length < 6) {
      errors.value.password = 'Лозинка мора да има минимум 6 карактери';
    }

    if (Object.keys(errors.value).length > 0) {
      return;
    }

    try {
      await authStore.login(form.username, form.password);
      router.push('/');
    } catch (err) {
      // Error is handled by authStore
    }
  };
</script>

<style scoped>
  .eco-section--auth {
    background: var(--eco-dark);
    min-height: 100vh;
    display: flex;
    align-items: center;
    padding: 40px 0;
  }

  .auth-card {
    background: var(--eco-card-bg);
    color: var(--eco-text-dark);
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(76, 175, 80, 0.1);
    border: 1px solid rgba(76, 175, 80, 0.1);
    position: relative;
    z-index: 2;
  }

  .form-label {
    font-weight: 600;
    color: var(--eco-text-dark);
    margin-bottom: 0.5rem;
  }

  .form-control {
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    padding: 0.75rem 1rem;
    font-size: 1rem;
    transition: all 0.3s ease;
  }

  .form-control:focus {
    border-color: var(--eco-accent);
    box-shadow: 0 0 0 0.2rem rgba(129, 199, 132, 0.2);
  }

  .form-control.is-invalid {
    border-color: #dc3545;
  }

  .btn-eco {
    background: var(--eco-accent);
    border: none;
    border-radius: 12px;
    font-weight: 600;
    padding: 0.75rem 1.5rem;
    transition: all 0.3s ease;
  }

  .btn-eco:hover:not(:disabled) {
    background: var(--eco-accent-2);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  }

  .auth-decoration {
    position: relative;
    height: 100px;
    margin-top: -30px;
    pointer-events: none;
  }

  .decoration-leaf {
    position: absolute;
    font-size: 2rem;
    opacity: 0.4;
    animation: float 3s ease-in-out infinite;
  }

  .leaf-1 {
    left: 5%;
    animation-delay: 0s;
  }

  .leaf-2 {
    right: 10%;
    animation-delay: 1s;
  }

  .leaf-3 {
    left: 50%;
    animation-delay: 2s;
  }

  @keyframes float {
    0%,
    100% {
      transform: translateY(0) rotate(0deg);
    }
    50% {
      transform: translateY(-15px) rotate(10deg);
    }
  }

  @media (max-width: 576px) {
    .eco-section--auth {
      padding: 20px 0;
    }

    .auth-card {
      padding: 1.5rem !important;
    }

    .auth-decoration {
      display: none;
    }
  }
</style>
