<template>
  <section class="profile-page">
    <div class="profile-shell">
      <header class="profile-hero">
        <div class="avatar" aria-hidden="true">{{ initials }}</div>
        <div class="hero-copy">
          <p class="eyebrow">Мој профил</p>
          <h1>{{ displayName }}</h1>
          <p>{{ user?.email || 'Одржувај ги податоците на профилот ажурирани.' }}</p>
        </div>
        <button v-if="!isEditing" class="btn eco-btn edit-button" type="button" @click="startEditing">
          Уреди
        </button>
      </header>

      <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div v-if="pageError" class="alert alert-danger">{{ pageError }}</div>

      <div class="profile-grid">
        <article class="profile-panel">
          <div class="panel-heading">
            <h2>Детали за профилот</h2>
            <p>Овие податоци го идентификуваат твојот профил на ЕкоСреќа.</p>
          </div>

          <div v-if="!isEditing" class="details-list">
            <div class="detail-row">
              <span>Име</span>
              <strong>{{ user?.firstName || 'Не е поставено' }}</strong>
            </div>
            <div class="detail-row">
              <span>Презиме</span>
              <strong>{{ user?.lastName || 'Не е поставено' }}</strong>
            </div>
            <div class="detail-row">
              <span>Корисничко име</span>
              <strong>{{ user?.username }}</strong>
            </div>
            <div class="detail-row">
              <span>Е-пошта</span>
              <strong>{{ user?.email }}</strong>
            </div>
            <div class="detail-row">
              <span>Член од</span>
              <strong>{{ memberSince }}</strong>
            </div>
          </div>

          <form v-else class="profile-form" @submit.prevent="saveProfile">
            <div class="form-field">
              <label for="firstName">Име</label>
              <input id="firstName" v-model.trim="form.firstName" type="text" class="form-control" />
            </div>
            <div class="form-field">
              <label for="lastName">Презиме</label>
              <input id="lastName" v-model.trim="form.lastName" type="text" class="form-control" />
            </div>
            <div class="form-field">
              <label for="email">Е-пошта</label>
              <input id="email" v-model.trim="form.email" type="email" class="form-control" required />
            </div>
            <div class="form-field">
              <label for="username">Корисничко име</label>
              <input id="username" :value="user?.username" type="text" class="form-control" disabled />
              <small>Корисничкото име не може да се промени.</small>
            </div>

            <div v-if="formError" class="alert alert-danger">{{ formError }}</div>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="isSaving">
                {{ isSaving ? 'Се зачувува...' : 'Зачувај промени' }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="cancelEditing">
                Откажи
              </button>
            </div>
          </form>
        </article>

        <aside class="profile-panel side-panel">
          <h2>Безбедност</h2>
          <p class="side-copy">Промени ја лозинката откако ќе ја потврдиш тековната.</p>

          <form class="password-form" @submit.prevent="changePassword">
            <div class="form-field">
              <label for="currentPassword">Тековна лозинка</label>
              <input id="currentPassword" v-model="passwordForm.currentPassword" type="password" class="form-control" autocomplete="current-password" />
            </div>
            <div class="form-field">
              <label for="newPassword">Нова лозинка</label>
              <input id="newPassword" v-model="passwordForm.newPassword" type="password" class="form-control" autocomplete="new-password" />
            </div>
            <div class="form-field">
              <label for="confirmPassword">Потврди лозинка</label>
              <input id="confirmPassword" v-model="passwordForm.confirmPassword" type="password" class="form-control" autocomplete="new-password" />
            </div>

            <div v-if="passwordError" class="alert alert-danger">{{ passwordError }}</div>

            <button class="btn btn-outline-success w-100" type="submit" :disabled="isChangingPassword">
              {{ isChangingPassword ? 'Се ажурира...' : 'Ажурирај лозинка' }}
            </button>
          </form>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import  api  from '../api';
  import { useAuthStore } from '../stores/authStore';

  const router = useRouter();
  const authStore = useAuthStore();
  const user = computed(() => authStore.user);
  const isEditing = ref(false);
  const isSaving = ref(false);
  const isChangingPassword = ref(false);
  const pageError = ref('');
  const formError = ref('');
  const passwordError = ref('');
  const successMessage = ref('');
  const form = reactive({ firstName: '', lastName: '', email: '' });
  const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' });

  const displayName = computed(() => {
    const fullName = [user.value?.firstName?.trim(), user.value?.lastName?.trim()].filter(Boolean).join(' ');
    return fullName || user.value?.username || 'Твој профил';
  });

  const initials = computed(() =>
    (displayName.value || user.value?.email || 'Корисник')
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase()
  );

  const memberSince = computed(() => {
    if (!user.value?.createdAt) return 'Непознато';
    return new Date(Number(user.value.createdAt)).toLocaleDateString('mk-MK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  });

  function fillForm() {
    form.firstName = user.value?.firstName || '';
    form.lastName = user.value?.lastName || '';
    form.email = user.value?.email || '';
  }

  function requireLogin() {
    if (!authStore.isAuthenticated || !authStore.userId) {
      router.push('/login');
      return false;
    }
    return true;
  }

  async function loadProfile() {
    if (!requireLogin()) return;
    try {
      const { data } = await api.get(`/profile/${authStore.userId}`);
      authStore.updateUser(data);
      fillForm();
    } catch (error) {
      pageError.value = error.response?.data?.message || 'Профилот не може да се вчита.';
    }
  }

  function startEditing() {
    successMessage.value = '';
    formError.value = '';
    fillForm();
    isEditing.value = true;
  }

  function cancelEditing() {
    isEditing.value = false;
    formError.value = '';
    fillForm();
  }

  async function saveProfile() {
    if (!requireLogin()) return;
    formError.value = '';
    successMessage.value = '';
    if (!form.email) {
      formError.value = 'Е-поштата е задолжителна.';
      return;
    }

    isSaving.value = true;
    try {
      const { data } = await api.put(`/profile/${authStore.userId}`, {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
      });
      authStore.updateUser(data);
      isEditing.value = false;
      successMessage.value = 'Профилот е успешно ажуриран.';
    } catch (error) {
      formError.value = error.response?.data?.message || 'Профилот не може да се ажурира.';
    } finally {
      isSaving.value = false;
    }
  }

  async function changePassword() {
    if (!requireLogin()) return;
    passwordError.value = '';
    successMessage.value = '';
    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      passwordError.value = 'Тековната и новата лозинка се задолжителни.';
      return;
    }
    if (passwordForm.newPassword.length < 6) {
      passwordError.value = 'Новата лозинка мора да има најмалку 6 карактери.';
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      passwordError.value = 'Новите лозинки не се совпаѓаат.';
      return;
    }

    isChangingPassword.value = true;
    try {
      await api.post(`/profile/${authStore.userId}/change-password`, {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      passwordForm.currentPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      successMessage.value = 'Лозинката е успешно ажурирана.';
    } catch (error) {
      passwordError.value = error.response?.data || 'Лозинката не може да се ажурира.';
    } finally {
      isChangingPassword.value = false;
    }
  }

  onMounted(loadProfile);
</script>

<style scoped>
  .profile-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .profile-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .profile-hero {
    align-items: center;
    background: linear-gradient(135deg, #458051 0%, #66bb6a 100%);
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: auto 1fr auto;
    margin-bottom: 1.5rem;
    padding: 2rem;
  }

  .avatar {
    align-items: center;
    aspect-ratio: 1;
    background: rgba(255, 255, 255, 0.2);
    border: 3px solid rgba(255, 255, 255, 0.75);
    border-radius: 50%;
    display: flex;
    font-size: 2rem;
    font-weight: 800;
    justify-content: center;
    width: 96px;
  }

  .profile-grid {
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 360px;
  }

  .profile-panel {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .detail-row {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: flex;
    justify-content: space-between;
    padding: 1rem 0;
  }

  .profile-form,
  .password-form {
    display: grid;
    gap: 1rem;
  }

  .form-field {
    display: grid;
    gap: 0.45rem;
  }

  .form-actions {
    display: flex;
    gap: 0.75rem;
  }

  @media (max-width: 991px) {
    .profile-grid,
    .profile-hero {
      grid-template-columns: 1fr;
    }
  }
</style>
