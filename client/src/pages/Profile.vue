<template>
  <div class="profile-container">
    <!-- Header Section -->
    <section class="profile-header eco-section">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-12 col-md-8">
            <div class="profile-header-content">
              <div class="profile-avatar">
                <div class="avatar-circle">
                  {{ initials }}
                </div>
              </div>
              <div class="profile-info">
                <h1 class="profile-name">{{ fullName }}</h1>
                <p class="profile-username">@{{ user?.username }}</p>
                <p class="profile-email">{{ user?.email }}</p>
              </div>
            </div>
          </div>
          <div class="col-12 col-md-4 text-end">
            <button
              class="btn eco-btn-primary me-2"
              @click="toggleEditMode"
              v-if="!isEditing"
            >
              <i class="bi bi-pencil-square"></i> Уреди Профил
            </button>
            <button
              class="btn btn-secondary"
              @click="toggleEditMode"
              v-if="isEditing"
            >
              Откажи
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Profile Content -->
    <section class="eco-section">
      <div class="container">
        <div class="row g-4">
          <!-- Main Profile Card -->
          <div class="col-12 col-lg-8">
            <div class="eco-card profile-card">
              <h2 class="card-title mb-4">Информации за профилот</h2>

              <!-- View Mode -->
              <div v-if="!isEditing" class="profile-display">
                <div class="info-row">
                  <label class="info-label">Име</label>
                  <p class="info-value">{{ user?.firstName || 'Не е наведено' }}</p>
                </div>
                <div class="info-row">
                  <label class="info-label">Презиме</label>
                  <p class="info-value">{{ user?.lastName || 'Не е наведено' }}</p>
                </div>
                <div class="info-row">
                  <label class="info-label">Корисничко име</label>
                  <p class="info-value">{{ user?.username }}</p>
                </div>
                <div class="info-row">
                  <label class="info-label">Е-пошта</label>
                  <p class="info-value">{{ user?.email }}</p>
                </div>
                <div class="info-row">
                  <label class="info-label">Член од</label>
                  <p class="info-value">{{ formatDate(user?.createdAt) }}</p>
                </div>
              </div>

              <!-- Edit Mode -->
              <form v-if="isEditing" @submit.prevent="saveProfile" class="profile-edit">
                <div class="mb-3">
                  <label for="firstName" class="form-label">Име</label>
                  <input
                    id="firstName"
                    v-model="editForm.firstName"
                    type="text"
                    class="form-control"
                    placeholder="Внесете име"
                  />
                </div>

                <div class="mb-3">
                  <label for="lastName" class="form-label">Презиме</label>
                  <input
                    id="lastName"
                    v-model="editForm.lastName"
                    type="text"
                    class="form-control"
                    placeholder="Внесете презиме"
                  />
                </div>

                <div class="mb-3">
                  <label for="email" class="form-label">Е-пошта</label>
                  <input
                    id="email"
                    v-model="editForm.email"
                    type="email"
                    class="form-control"
                    placeholder="Внесете е-пошта"
                  />
                </div>

                <div class="mb-3">
                  <label for="username" class="form-label">Корисничко име</label>
                  <input
                    id="username"
                    v-model="editForm.username"
                    type="text"
                    class="form-control"
                    placeholder="Внесете корисничко име"
                    disabled
                  />
                  <small class="text-muted">Корисничкото име не може да се промени</small>
                </div>

                <!-- Error Messages -->
                <div v-if="editError" class="alert alert-danger alert-dismissible fade show">
                  {{ editError }}
                  <button
                    type="button"
                    class="btn-close"
                    @click="editError = null"
                  ></button>
                </div>

                <!-- Success Messages -->
                <div v-if="editSuccess" class="alert alert-success alert-dismissible fade show">
                  Профилот е успешно ажуриран!
                  <button
                    type="button"
                    class="btn-close"
                    @click="editSuccess = false"
                  ></button>
                </div>

                <!-- Password Change Toggle Button -->
                <div class="password-toggle-section mb-3">
                  <button
                    type="button"
                    class="btn btn-outline-secondary w-100"
                    @click="togglePasswordSection"
                  >
                    <i :class="showPasswordSection ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                    {{ showPasswordSection ? 'Скрии' : 'Промени' }} Лозинка
                  </button>
                </div>

                <!-- Password Change Form (Hidden by default) -->
                <div v-if="showPasswordSection" class="password-section-edit mb-4">
                  <div class="mb-3">
                    <label for="currentPasswordEdit" class="form-label">Тековна лозинка</label>
                    <input
                      id="currentPasswordEdit"
                      v-model="passwordForm.current"
                      type="password"
                      class="form-control"
                      placeholder="Внесете тековна лозинка"
                    />
                  </div>

                  <div class="mb-3">
                    <label for="newPasswordEdit" class="form-label">Нова лозинка</label>
                    <input
                      id="newPasswordEdit"
                      v-model="passwordForm.new"
                      type="password"
                      class="form-control"
                      placeholder="Внесете нова лозинка"
                    />
                  </div>

                  <div class="mb-3">
                    <label for="confirmPasswordEdit" class="form-label">Потврди лозинка</label>
                    <input
                      id="confirmPasswordEdit"
                      v-model="passwordForm.confirm"
                      type="password"
                      class="form-control"
                      placeholder="Потврди нова лозинка"
                    />
                  </div>

                  <div v-if="passwordError" class="alert alert-danger alert-dismissible fade show">
                    {{ passwordError }}
                    <button
                      type="button"
                      class="btn-close"
                      @click="passwordError = null"
                    ></button>
                  </div>

                  <button
                    type="button"
                    class="btn eco-btn-primary w-100"
                    @click="changePassword"
                    :disabled="isChangingPassword"
                  >
                    <span v-if="!isChangingPassword">Ажурирај лозинка</span>
                    <span v-else>
                      <span
                        class="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                      ></span>
                      Ажурирање...
                    </span>
                  </button>
                </div>

                <div class="form-actions">
                  <button
                    type="submit"
                    class="btn eco-btn-primary"
                    :disabled="isSaving"
                  >
                    <span v-if="!isSaving">Зачувај измени</span>
                    <span v-else>
                      <span
                        class="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                      ></span>
                      Зачувување...
                    </span>
                  </button>
                  <button
                    type="button"
                    class="btn btn-secondary"
                    @click="cancelEdit"
                  >
                    Отфрли
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Sidebar Stats -->
          <div class="col-12 col-lg-4">
            <!-- Account Stats -->
            <div class="eco-card stats-card mb-4">
              <h3 class="card-title mb-3">Статистика на сметка</h3>
              <div class="stat-item">
                <span class="stat-label">Статус на сметка</span>
                <span class="stat-value badge bg-success">Активна</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Член од</span>
                <span class="stat-value">{{ formatYear(user?.createdAt) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Вкупно квизови</span>
                <span class="stat-value">0</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Поени заработени</span>
                <span class="stat-value">0 поени</span>
              </div>
            </div>

            <!-- Quick Actions -->
            <div class="eco-card actions-card">
              <h3 class="card-title mb-3">Брзи акции</h3>
              <div class="d-grid gap-2">
                <button class="btn btn-outline-primary btn-sm" @click="goToQuizzes">
                  <i class="bi bi-question-circle"></i> Направи квиз
                </button>
                <button class="btn btn-outline-success btn-sm" @click="goToNews">
                  <i class="bi bi-newspaper"></i> Најнови вести
                </button>
                <button class="btn btn-outline-info btn-sm" @click="goToGames">
                  <i class="bi bi-joystick"></i> Игри
                </button>
                <button class="btn btn-outline-danger btn-sm" @click="handleLogout">
                  <i class="bi bi-box-arrow-right"></i> Одјави се
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const user = computed(() => authStore.user);
const isEditing = ref(false);
const isSaving = ref(false);
const editError = ref(null);
const editSuccess = ref(false);
const isChangingPassword = ref(false);
const passwordError = ref(null);
const showPasswordSection = ref(false);

const editForm = ref({
  firstName: user.value?.firstName || '',
  lastName: user.value?.lastName || '',
  email: user.value?.email || '',
});

const passwordForm = ref({
  current: '',
  new: '',
  confirm: '',
});

const fullName = computed(() => {
  if (user.value?.firstName && user.value?.lastName) {
    return `${user.value.firstName} ${user.value.lastName}`;
  }
  return user.value?.username || 'User';
});

const initials = computed(() => {
  if (user.value?.firstName && user.value?.lastName) {
    return `${user.value.firstName[0]}${user.value.lastName[0]}`.toUpperCase();
  }
  if (user.value?.username) {
    return user.value.username.substring(0, 2).toUpperCase();
  }
  return 'U';
});

function formatDate(dateString) {
  if (!dateString) return 'Unknown';
  try {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return dateString;
  }
}

function togglePasswordSection() {
  showPasswordSection.value = !showPasswordSection.value;
}

function formatYear(dateString) {
  if (!dateString) return 'Unknown';
  try {
    return new Date(dateString).getFullYear();
  } catch {
    return dateString;
  }
}

function toggleEditMode() {
  if (isEditing.value) {
    isEditing.value = false;
    showPasswordSection.value = false;
  } else {
    editForm.value = {
      firstName: user.value?.firstName || '',
      lastName: user.value?.lastName || '',
      email: user.value?.email || '',
    };
    editError.value = null;
    showPasswordSection.value = false;
    isEditing.value = true;
  }
}

function cancelEdit() {
  isEditing.value = false;
  editError.value = null;
  editSuccess.value = false;
  showPasswordSection.value = false;
  passwordForm.value = {
    current: '',
    new: '',
    confirm: '',
  };
}

async function saveProfile() {
  isSaving.value = true;
  editError.value = null;

  try {
    const response = await fetch('http://localhost:8082/api/profile', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({
        firstName: editForm.value.firstName,
        lastName: editForm.value.lastName,
        email: editForm.value.email,
      }),
    });

    if (!response.ok) {
      throw new Error('Неможе да се ажурира профилот');
    }

    const updatedUser = await response.json();
    authStore.user.firstName = updatedUser.firstName;
    authStore.user.lastName = updatedUser.lastName;
    authStore.user.email = updatedUser.email;
    authStore.user.updatedAt = updatedUser.updatedAt;

    editSuccess.value = true;
    isEditing.value = false;

    setTimeout(() => {
      editSuccess.value = false;
    }, 3000);
  } catch (error) {
    editError.value = error.message || 'Неможе да се ажурира профилот';
  } finally {
    isSaving.value = false;
  }
}

async function changePassword() {
  if (passwordForm.value.new !== passwordForm.value.confirm) {
    passwordError.value = 'Лозинките не се совпаѓаат';
    return;
  }

  if (passwordForm.value.new.length < 6) {
    passwordError.value = 'Лозинката мора да има најмалку 6 знаци';
    return;
  }

  isChangingPassword.value = true;
  passwordError.value = null;

  try {
    const response = await fetch('http://localhost:8082/api/profile/change-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({
        currentPassword: passwordForm.value.current,
        newPassword: passwordForm.value.new,
      }),
    });

    if (!response.ok) {
      throw new Error('Неможе да се промени лозинката');
    }

    passwordForm.value = {
      current: '',
      new: '',
      confirm: '',
    };
    alert('Лозинката е успешно изменета!');
  } catch (error) {
    passwordError.value = error.message || 'Неможе да се промени лозинката';
  } finally {
    isChangingPassword.value = false;
  }
}

function goToQuizzes() {
  router.push('/quizzes');
}

function goToNews() {
  router.push('/news');
}

function goToGames() {
  router.push('/games');
}

function handleLogout() {
  if (confirm('Дали сте сигурни дека сакате да се одјавите?')) {
    authStore.logout();
    router.push('/login');
  }
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login');
  }
});
</script>

<style scoped>
/* ============ ROOT STYLES ============ */
:root {
  --eco-dark: #2d5016;
  --eco-primary: #4a7c2c;
  --eco-light: #6b8e23;
  --eco-accent: #8ba428;
  --eco-bg: #f5f8f2;
  --eco-card: #ffffff;
  --eco-text: #2c3e50;
  --eco-text-light: #7f8c8d;
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.profile-container {
  background: linear-gradient(135deg, #f5f8f2 0%, #f9fbf6 100%);
  min-height: 100vh;
  padding-bottom: 4rem;
}

/* ============ HEADER SECTION ============ */
.profile-header {
  background: linear-gradient(135deg, #5a8d3a 0%, #7aa84a 50%, #8ba428 100%);
  color: white;
  padding: 4rem 0;
  box-shadow: var(--shadow-lg), inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.profile-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="15" opacity="0.1"/><circle cx="80" cy="70" r="20" opacity="0.05"/></svg>');
  pointer-events: none;
}

.profile-header .container {
  position: relative;
  z-index: 1;
}

.profile-header-content {
  display: flex;
  align-items: center;
  gap: 3rem;
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-avatar {
  flex-shrink: 0;
  animation: slideInLeft 0.6s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.avatar-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  font-weight: 800;
  border: 4px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: var(--transition);
}

.avatar-circle:hover {
  transform: scale(1.08);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.profile-info {
  flex: 1;
  animation: slideInRight 0.6s ease-out;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.profile-info h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.profile-username {
  font-size: 1.15rem;
  opacity: 0.95;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.profile-email {
  font-size: 1rem;
  opacity: 0.85;
  margin: 0;
  letter-spacing: 0.3px;
}

/* Edit/Cancel Buttons in Header */
.profile-header .btn {
  border-radius: 10px;
  padding: 0.8rem 1.8rem;
  font-weight: 600;
  font-size: 0.95rem;
  transition: var(--transition);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.profile-header .eco-btn-primary {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border-color: white;
}

.profile-header .eco-btn-primary:hover {
  background-color: white;
  color: var(--eco-dark);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.profile-header .btn-secondary {
  background-color: rgba(255, 255, 255, 0.15);
  color: white;
  border-color: rgba(255, 255, 255, 0.4);
}

.profile-header .btn-secondary:hover {
  background-color: rgba(255, 255, 255, 0.25);
  color: white;
}

/* ============ MAIN CONTENT ============ */
.eco-section {
  padding: 3.5rem 0;
}

.eco-card {
  background: var(--eco-card);
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: var(--shadow-md);
  border: 1px solid rgba(106, 168, 74, 0.1);
  transition: var(--transition);
}

.eco-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: rgba(106, 168, 74, 0.2);
}

.profile-card {
  animation: fadeIn 0.5s ease-out 0.2s backwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-title {
  color: #5a8d3a;
  font-weight: 800;
  font-size: 1.5rem;
  border-bottom: 3px solid #7aa84a;
  padding-bottom: 1.25rem;
  margin-bottom: 2rem;
  position: relative;
}

.card-title::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #7aa84a 0%, transparent 100%);
  border-radius: 2px;
}

/* ============ PROFILE DISPLAY ============ */
.profile-display .info-row {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
  transition: var(--transition);
}

.profile-display .info-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.profile-display .info-row:hover {
  padding-left: 0.5rem;
  border-left: 4px solid var(--eco-light);
  padding-bottom: 1rem;
  padding-left: 1rem;
}

.info-label {
  font-weight: 700;
  color: #5a8d3a;
  display: block;
  margin-bottom: 0.75rem;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  opacity: 0.85;
}

.info-value {
  color: #2c3e50;
  font-size: 1.25rem;
  margin: 0;
  font-weight: 600;
}

/* ============ PROFILE EDIT FORM ============ */
.profile-edit .form-label {
  font-weight: 700;
  color: #5a8d3a;
  font-size: 0.95rem;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.profile-edit .form-control {
  border-radius: 12px;
  border: 2px solid #e8f0e8;
  padding: 0.95rem 1.25rem;
  font-size: 1rem;
  transition: var(--transition);
  background-color: #fafcfa;
}

.profile-edit .form-control:focus {
  border-color: #7aa84a;
  background-color: white;
  box-shadow: 0 0 0 0.35rem rgba(122, 168, 74, 0.15);
}

.profile-edit .form-control:hover {
  border-color: #d0e8d0;
}

.profile-edit .form-control:disabled {
  background-color: #f0f0f0;
  color: #999;
}

.profile-edit small {
  color: #7f8c8d;
  font-weight: 500;
}

.form-actions {
  display: flex;
  gap: 1.25rem;
  margin-top: 2.5rem;
  padding-top: 2rem;
  border-top: 2px solid #f0f0f0;
}

.form-actions button {
  flex: 1;
  padding: 1rem;
  font-weight: 700;
  border-radius: 12px;
  transition: var(--transition);
  font-size: 1rem;
}

/* ============ SIDEBAR CARDS ============ */
.stats-card,
.actions-card {
  border-radius: 16px;
  animation: fadeIn 0.5s ease-out 0.3s backwards;
}

.stats-card {
  background: linear-gradient(135deg, var(--eco-card) 0%, rgba(107, 142, 35, 0.02) 100%);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 2px solid #f5f5f5;
  transition: var(--transition);
}

.stat-item:hover {
  background-color: rgba(74, 124, 44, 0.05);
  transform: translateX(5px);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-weight: 700;
  color: #5a8d3a;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  color: #7aa84a;
  font-weight: 800;
  font-size: 1.3rem;
  padding: 0.35rem 0.85rem;
  background: linear-gradient(135deg, rgba(122, 168, 74, 0.1) 0%, rgba(107, 142, 35, 0.05) 100%);
  border-radius: 8px;
}

.stat-value.badge {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  background: linear-gradient(135deg, #2d5016 0%, #4a7c2c 100%);
  color: white;
}

/* ============ QUICK ACTIONS ============ */
.actions-card .d-grid {
  gap: 1rem;
}

.actions-card .btn {
  border-radius: 12px;
  font-weight: 700;
  padding: 0.95rem 1.2rem;
  font-size: 0.95rem;
  transition: var(--transition);
  border: 2px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 0.9rem;
  position: relative;
  overflow: hidden;
}

.actions-card .btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  transition: left 0.4s;
  z-index: 0;
}

.actions-card .btn > * {
  position: relative;
  z-index: 1;
}

.actions-card .btn:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.actions-card .btn:hover::before {
  left: 100%;
}

.actions-card .btn-outline-primary {
  color: #6b8e23;
  border-color: #6b8e23;
  background-color: transparent;
}

.actions-card .btn-outline-primary:hover {
  background-color: #6b8e23;
  border-color: #6b8e23;
  color: white;
  box-shadow: 0 8px 24px rgba(107, 142, 35, 0.4);
}

.actions-card .btn-outline-success {
  color: #27ae60;
  border-color: #27ae60;
  background-color: transparent;
}

.actions-card .btn-outline-success:hover {
  background-color: #27ae60;
  border-color: #27ae60;
  color: white;
  box-shadow: 0 8px 24px rgba(39, 174, 96, 0.4);
}

.actions-card .btn-outline-info {
  color: #3498db;
  border-color: #3498db;
  background-color: transparent;
}

.actions-card .btn-outline-info:hover {
  background-color: #3498db;
  border-color: #3498db;
  color: white;
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.4);
}

.actions-card .btn-outline-danger {
  color: #e74c3c;
  border-color: #e74c3c;
  background-color: transparent;
}

.actions-card .btn-outline-danger:hover {
  background-color: #e74c3c;
  border-color: #e74c3c;
  color: white;
  box-shadow: 0 8px 24px rgba(231, 76, 60, 0.4);
}

/* ============ PRIMARY BUTTONS ============ */
.eco-btn-primary {
  background: linear-gradient(135deg, #7aa84a 0%, #8ba428 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 1rem 1.8rem;
  font-weight: 700;
  transition: var(--transition);
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-md);
  position: relative;
  overflow: hidden;
}

.eco-btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.eco-btn-primary:hover {
  background: linear-gradient(135deg, #6b9a3c 0%, #7aa84a 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(106, 154, 60, 0.25);
}

.eco-btn-primary:hover::before {
  left: 100%;
}

.eco-btn-primary:active {
  transform: translateY(0);
}

.eco-btn-primary:disabled {
  background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
  cursor: not-allowed;
  transform: none;
  box-shadow: var(--shadow-sm);
}

/* ============ PASSWORD TOGGLE SECTION ============ */
.password-toggle-section {
  margin: 2rem 0;
  padding: 1rem 0;
  border-top: 2px solid #f0f0f0;
  border-bottom: 2px solid #f0f0f0;
}

.password-toggle-section .btn {
  border-radius: 12px;
  font-weight: 600;
  transition: var(--transition);
  border: 2px solid #ddd;
  color: #5a8d3a;
}

.password-toggle-section .btn:hover {
  background-color: #f9fbf6;
  border-color: #7aa84a;
  color: #7aa84a;
}

/* ============ PASSWORD SECTION EDIT ============ */
.password-section-edit {
  background: linear-gradient(135deg, #f9fbf6 0%, #f5f8f2 100%);
  padding: 2rem;
  border-radius: 12px;
  border-left: 4px solid #7aa84a;
  animation: slideDown 0.3s ease-out;
}

.password-section-edit .form-label {
  font-weight: 700;
  color: #5a8d3a;
  font-size: 0.95rem;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.password-section-edit .form-control {
  border-radius: 12px;
  border: 2px solid #e8f0e8;
  padding: 0.95rem 1.25rem;
  font-size: 1rem;
  transition: var(--transition);
  background-color: #ffffff;
}

.password-section-edit .form-control:focus {
  border-color: #7aa84a;
  background-color: white;
  box-shadow: 0 0 0 0.35rem rgba(122, 168, 74, 0.15);
}

.password-section-edit .form-control:hover {
  border-color: #d0e8d0;
}

/* ============ PASSWORD FORM ============ */
.password-form {
  max-width: 100%;
}

.password-form .form-control {
  border-radius: 12px;
  border: 2px solid #e8f0e8;
  padding: 0.95rem 1.25rem;
  transition: var(--transition);
  background-color: #fafcfa;
}

.password-form .form-control:focus {
  border-color: #7aa84a;
  box-shadow: 0 0 0 0.35rem rgba(122, 168, 74, 0.15);
}

/* ============ ALERTS ============ */
.alert {
  border-radius: 12px;
  border: none;
  border-left: 4px solid;
  padding: 1.25rem 1.5rem;
  margin-bottom: 1.5rem;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.alert-danger {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  color: #c92a2a;
  border-left-color: #e74c3c;
}

.alert-success {
  background: linear-gradient(135deg, #f0fdf4 0%, #e8f9f0 100%);
  color: #22863a;
  border-left-color: #27ae60;
}

.alert .btn-close {
  color: inherit;
  opacity: 0.5;
  transition: var(--transition);
}

.alert .btn-close:hover {
  opacity: 1;
}

/* ============ BACKGROUND VARIANTS ============ */
.bg-light {
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f3f7 100%) !important;
}

/* ============ RESPONSIVE DESIGN ============ */
@media (max-width: 992px) {
  .profile-header {
    padding: 3rem 0;
  }

  .profile-header-content {
    gap: 2rem;
  }

  .avatar-circle {
    width: 120px;
    height: 120px;
    font-size: 2.5rem;
  }

  .profile-info h1 {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  .profile-header {
    padding: 2.5rem 0;
  }

  .profile-header-content {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .avatar-circle {
    width: 110px;
    height: 110px;
    font-size: 2.2rem;
  }

  .profile-info h1 {
    font-size: 1.75rem;
  }

  .profile-info {
    animation: none;
  }

  .eco-card {
    padding: 1.75rem;
  }

  .card-title {
    font-size: 1.25rem;
    margin-bottom: 1.5rem;
  }

  .profile-display .info-row {
    margin-bottom: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
    gap: 0.75rem;
  }

  .form-actions button {
    flex: 1 1 auto;
    padding: 0.85rem;
    font-size: 0.9rem;
  }

  .profile-header .btn {
    padding: 0.7rem 1.2rem;
    font-size: 0.85rem;
  }

  .stat-item {
    padding: 1.2rem;
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }

  .stat-label,
  .stat-value {
    width: 100%;
  }

  .actions-card .btn {
    padding: 0.85rem 1rem;
    font-size: 0.85rem;
  }

  .eco-section {
    padding: 2rem 0;
  }
}

@media (max-width: 576px) {
  .profile-header {
    padding: 2rem 0;
  }

  .profile-info h1 {
    font-size: 1.5rem;
  }

  .profile-username,
  .profile-email {
    font-size: 0.9rem;
  }

  .eco-card {
    padding: 1.5rem;
  }

  .card-title {
    font-size: 1.1rem;
  }

  .info-label {
    font-size: 0.75rem;
  }

  .info-value {
    font-size: 1.1rem;
  }

  .eco-btn-primary {
    padding: 0.8rem 1.2rem;
    font-size: 0.85rem;
  }

  .avatar-circle {
    width: 90px;
    height: 90px;
    font-size: 2rem;
  }
}

/* ============ DARK MODE SUPPORT (Optional) ============ */
@media (prefers-color-scheme: dark) {
  .profile-container {
    background: linear-gradient(135deg, #e8f0e0 0%, #f0f5eb 100%);
  }

  .eco-card {
    background: #f5f9f3;
    border-color: rgba(107, 142, 35, 0.2);
  }

  .profile-display .info-row {
    border-bottom-color: #e0e8d8;
  }

  .profile-display .info-row:hover {
    border-left-color: var(--eco-light);
  }

  .info-label {
    color: #5a8d3a;
  }

  .info-value {
    color: #2c3e50;
  }

  .profile-edit .form-control {
    background-color: #fafcfa;
    border-color: rgba(107, 142, 35, 0.2);
    color: #2c3e50;
  }

  .profile-edit .form-control:focus {
    border-color: var(--eco-light);
    box-shadow: 0 0 0 0.35rem rgba(74, 124, 44, 0.25);
  }
}
</style>
