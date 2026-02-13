<template>
  <section class="eco-section eco-section--auth">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-12 col-sm-8 col-md-6 col-lg-5">
          <div class="eco-card auth-card p-5">
            <div class="text-center mb-4">
              <h2 class="eco-title mb-2">Регистрирај се</h2>
              <p class="text-muted">Придружи се еко-заедницата! 🌍</p>
            </div>

            <form @submit.prevent="handleSignup">
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
                <small class="text-muted d-block mt-1">Минимум 3 карактери</small>
              </div>

              <!-- First Name -->
              <div class="mb-3">
                <label for="firstName" class="form-label">Име</label>
                <input
                  id="firstName"
                  v-model="form.firstName"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.firstName }"
                  placeholder="Твое име"
                  required
                />
                <div v-if="errors.firstName" class="invalid-feedback d-block">
                  {{ errors.firstName }}
                </div>
              </div>

              <!-- Last Name -->
              <div class="mb-3">
                <label for="lastName" class="form-label">Презиме</label>
                <input
                  id="lastName"
                  v-model="form.lastName"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.lastName }"
                  placeholder="Твое презиме"
                />
                <div v-if="errors.lastName" class="invalid-feedback d-block">
                  {{ errors.lastName }}
                </div>
              </div>

              <!-- Email -->
              <div class="mb-3">
                <label for="email" class="form-label">Имејл адреса</label>
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': errors.email }"
                  placeholder="твој@имејл.com"
                  required
                />
                <div v-if="errors.email" class="invalid-feedback d-block">
                  {{ errors.email }}
                </div>
              </div>

              <!-- Password -->
              <div class="mb-3">
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
                <small class="text-muted d-block mt-1">Минимум 6 карактери</small>
              </div>

              <!-- Password Confirm -->
              <div class="mb-4">
                <label for="passwordConfirm" class="form-label">Потврди лозинка</label>
                <input
                  id="passwordConfirm"
                  v-model="form.passwordConfirm"
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': errors.passwordConfirm }"
                  placeholder="••••••••"
                  required
                />
                <div v-if="errors.passwordConfirm" class="invalid-feedback d-block">
                  {{ errors.passwordConfirm }}
                </div>
              </div>

              <!-- Error Message -->
              <div v-if="authStore.error" class="alert alert-danger alert-dismissible fade show">
                {{ authStore.error }}
                <button type="button" class="btn-close" @click="authStore.clearError()"></button>
              </div>

              <!-- Terms -->
              <div class="mb-4">
                <div class="form-check">
                  <input
                    id="terms"
                    v-model="form.agreeTerms"
                    type="checkbox"
                    class="form-check-input"
                    :class="{ 'is-invalid': errors.agreeTerms }"
                  />
                  <label class="form-check-label" for="terms">
                    Се согласувам со условите на користење
                  </label>
                </div>
                <div v-if="errors.agreeTerms" class="invalid-feedback d-block">
                  {{ errors.agreeTerms }}
                </div>
              </div>

              <!-- Submit Button -->
              <button type="submit" class="btn eco-btn w-100 mb-3" :disabled="authStore.loading">
                <span v-if="!authStore.loading">Регистрирај се</span>
                <span v-else>
                  <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  Регистрирање...
                </span>
              </button>
            </form>

            <!-- Login Link -->
            <div class="text-center">
              <p class="text-muted mb-0">
                Веќе имаш профил?
                <RouterLink to="/login" class="text-success text-decoration-none fw-bold"
                  >Пријави се тука</RouterLink
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
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    passwordConfirm: '',
    agreeTerms: false,
  });

  const errors = ref({});

  const handleSignup = async () => {
    errors.value = {};

    if (!form.username) {
      errors.value.username = 'Корисничкото име е задолжително';
    } else if (form.username.length < 3) {
      errors.value.username = 'Корисничкото име мора да има барем 3 карактери';
    }

    if (!form.firstName) {
      errors.value.firstName = 'Името е задолжително';
    }

    if (!form.email) {
      errors.value.email = 'Имејл адреса е задолжителна';
    } else if (!form.email.includes('@')) {
      errors.value.email = 'Внеси валидна имејл адреса';
    }

    if (!form.password) {
      errors.value.password = 'Лозинка е задолжителна';
    } else if (form.password.length < 6) {
      errors.value.password = 'Лозинка мора да има минимум 6 карактери';
    }

    if (!form.passwordConfirm) {
      errors.value.passwordConfirm = 'Потврдување на лозинка е задолжително';
    } else if (form.password !== form.passwordConfirm) {
      errors.value.passwordConfirm = 'Лозинките не се совпаѓаат';
    }

    if (!form.agreeTerms) {
      errors.value.agreeTerms = 'Мораш да се согласиш со условите';
    }

    if (Object.keys(errors.value).length > 0) {
      return;
    }

    try {
      await authStore.signup(
        form.username,
        form.email,
        form.password,
        form.passwordConfirm,
        form.firstName,
        form.lastName
      );
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

  .form-control,
  .form-check-input {
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    transition: all 0.3s ease;
  }

  .form-control {
    padding: 0.75rem 1rem;
    font-size: 1rem;
  }

  .form-control:focus,
  .form-check-input:focus {
    border-color: var(--eco-accent);
    box-shadow: 0 0 0 0.2rem rgba(129, 199, 132, 0.2);
  }

  .form-control.is-invalid,
  .form-check-input.is-invalid {
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
