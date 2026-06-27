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

      <article class="profile-panel history-panel">
        <div class="panel-heading">
          <h2>Историја на квизови</h2>
          <p>Следи ги квизовите што си ги решил и историјата на резултатите.</p>
        </div>

        <div class="history-summary">
          <div>
            <span>Квизови</span>
            <strong>{{ quizHistory.length }}</strong>
          </div>
          <div>
            <span>Најдобар резултат</span>
            <strong>{{ bestScoreLabel }}</strong>
          </div>
          <div>
            <span>Просек</span>
            <strong>{{ averageScoreLabel }}</strong>
          </div>
        </div>

        <div v-if="historyLoading" class="history-empty">Историјата се вчитува...</div>
        <div v-else-if="quizHistory.length === 0" class="history-empty">Сè уште немаш решени квизови.</div>
        <div v-else class="history-list">
          <RouterLink
            v-for="attempt in quizHistory"
            :key="attempt.id"
            class="history-row"
            :to="`/quizzes/${attempt.quizId}`"
          >
            <div>
              <strong>{{ attempt.quizTitle }}</strong>
              <span>{{ formatAttemptDate(attempt.createdAt) }}</span>
            </div>
            <span class="score-pill">{{ attempt.score }}/{{ attempt.total }}</span>
          </RouterLink>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { RouterLink, useRouter } from 'vue-router';
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
  const quizHistory = ref([]);
  const historyLoading = ref(false);
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

  const bestScoreLabel = computed(() => {
    if (!quizHistory.value.length) return '0%';
    const best = Math.max(...quizHistory.value.map(scorePercent));
    return `${best}%`;
  });

  const averageScoreLabel = computed(() => {
    if (!quizHistory.value.length) return '0%';
    const total = quizHistory.value.reduce((sum, attempt) => sum + scorePercent(attempt), 0);
    return `${Math.round(total / quizHistory.value.length)}%`;
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

  async function loadQuizHistory() {
    if (!requireLogin()) return;
    historyLoading.value = true;
    try {
      const { data } = await api.get('/quizzes/me/history', {
        headers: { 'X-User-Id': authStore.userId },
      });
      quizHistory.value = data;
    } catch (error) {
      pageError.value = error.response?.data?.message || 'Историјата на квизови не може да се вчита.';
    } finally {
      historyLoading.value = false;
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

  function scorePercent(attempt) {
    if (!attempt?.total) return 0;
    return Math.round((attempt.score / attempt.total) * 100);
  }

  function formatAttemptDate(value) {
    if (!value) return 'Без датум';
    return new Date(value).toLocaleDateString('mk-MK', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }

  onMounted(async () => {
    await loadProfile();
    await loadQuizHistory();
  });
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
    background:
      linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.82)),
      url('../img/guide.svg') right 2rem center / 180px no-repeat;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: auto 1fr auto;
    margin-bottom: 1.5rem;
    min-height: 260px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .hero-copy {
    max-width: 680px;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 900;
    letter-spacing: 0.08em;
    margin: 0 0 0.75rem;
    text-transform: uppercase;
  }

  .profile-hero h1 {
    font-size: clamp(2.2rem, 6vw, 4.4rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
  }

  .profile-hero p {
    font-size: 1rem;
    line-height: 1.7;
    margin: 0;
    opacity: 0.92;
  }

  .avatar {
    align-items: center;
    aspect-ratio: 1;
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.28);
    border-radius: 8px;
    display: flex;
    font-size: 2rem;
    font-weight: 900;
    justify-content: center;
    width: 96px;
  }

  .edit-button {
    background: #fff;
    border: none;
    color: #24552d;
    font-weight: 900;
    min-height: 44px;
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
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .panel-heading {
    margin-bottom: 1rem;
  }

  .panel-heading h2,
  .profile-panel h2 {
    color: #1b2a1b;
    font-size: 1.35rem;
    font-weight: 900;
    margin: 0 0 0.25rem;
  }

  .panel-heading p,
  .side-copy {
    color: #506650;
    margin: 0;
  }

  .detail-row {
    background: rgba(102, 187, 106, 0.07);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    display: grid;
    gap: 0.25rem;
    padding: 1rem;
  }

  .details-list {
    display: grid;
    gap: 0.75rem;
  }

  .detail-row span,
  .history-summary span,
  .history-row span {
    color: #506650;
    font-size: 0.76rem;
    font-weight: 900;
    text-transform: uppercase;
  }

  .profile-form,
  .password-form {
    display: grid;
    gap: 1rem;
  }

  .form-field {
    display: grid;
    gap: 0.45rem;
    background: rgba(102, 187, 106, 0.07);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    padding: 1rem;
  }

  .form-field label {
    color: #24552d;
    font-size: 0.82rem;
    font-weight: 900;
    text-transform: uppercase;
  }

  .form-control {
    border: 1px solid #d7e6d8;
    border-radius: 8px;
    min-height: 44px;
  }

  .form-control:focus {
    border-color: #66bb6a;
    box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2);
  }

  .form-actions {
    display: flex;
    gap: 0.75rem;
  }

  .history-panel {
    margin-top: 1.5rem;
  }

  .history-summary {
    display: grid;
    gap: 0.75rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    margin-bottom: 1rem;
  }

  .history-summary > div,
  .history-row,
  .history-empty {
    background: rgba(102, 187, 106, 0.08);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    padding: 1rem;
  }

  .history-summary span,
  .history-row span {
    color: #506650;
  }

  .history-summary strong {
    display: block;
    font-size: 1.6rem;
  }

  .history-list {
    display: grid;
    gap: 0.75rem;
  }

  .history-row {
    align-items: center;
    color: inherit;
    display: flex;
    justify-content: space-between;
    text-decoration: none;
  }

  .history-row > div {
    display: grid;
    gap: 0.25rem;
  }

  .score-pill {
    background: #2e7d32;
    border-radius: 999px;
    color: #fff !important;
    padding: 0.45rem 0.75rem;
    white-space: nowrap;
  }

  @media (max-width: 991px) {
    .profile-grid,
    .history-summary,
    .profile-hero {
      grid-template-columns: 1fr;
    }

    .profile-hero {
      background:
        linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.84)),
        url('../img/guide.svg') right 1rem bottom 1rem / 120px no-repeat;
    }

    .avatar {
      width: 72px;
    }
  }
</style>
