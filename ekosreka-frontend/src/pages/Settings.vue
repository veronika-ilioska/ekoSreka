<template>
  <section class="settings-page">
    <div class="settings-shell">
      <header class="settings-hero">
        <div>
          <p class="eyebrow">Поставки</p>
          <h1>Прилагоди ја ЕкоСреќа според тебе</h1>
          <p>Избери како да се однесува апликацијата и од каде започнува твојата еко активност.</p>
        </div>
        <RouterLink class="btn hero-button" to="/profile">Уреди профил</RouterLink>
      </header>

      <div v-if="savedMessage" class="alert alert-success">{{ savedMessage }}</div>

      <div class="settings-grid">
        <article class="settings-panel">
          <div class="panel-heading">
            <h2>Преференции</h2>
            <p>Се зачувува на овој уред за побрзо и поудобно користење.</p>
          </div>

          <form class="settings-form" @submit.prevent="saveSettings">
            <div class="field">
              <label for="startPage">Почетна страница</label>
              <select id="startPage" v-model="settings.startPage" class="form-select">
                <option value="/">Почетна</option>
                <option value="/news">Вести</option>
                <option value="/fun">Забава</option>
                <option value="/quizzes">Квизови</option>
              </select>
            </div>

            <div class="field">
              <label for="contentDensity">Густина на содржина</label>
              <select id="contentDensity" v-model="settings.contentDensity" class="form-select">
                <option value="comfortable">Удобно</option>
                <option value="compact">Компактно</option>
              </select>
            </div>

            <div class="field">
              <label for="ecoGoal">Еко цел</label>
              <select id="ecoGoal" v-model="settings.ecoGoal" class="form-select">
                <option value="learn">Научи повеќе секоја недела</option>
                <option value="reduce">Намали домашен отпад</option>
                <option value="energy">Заштеди енергија</option>
                <option value="nature">Помина повеќе време во природа</option>
              </select>
            </div>

            <div class="toggle-list">
              <label class="toggle-row">
                <span>
                  <strong>Неделен еко преглед</strong>
                  <small>Прикажува потсетници за најнови вести и идеи за активности.</small>
                </span>
                <input v-model="settings.weeklyDigest" type="checkbox" />
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Потсетници за квизови</strong>
                  <small>Ги задржува предлозите за квизови видливи во апликацијата.</small>
                </span>
                <input v-model="settings.quizReminders" type="checkbox" />
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Намалени анимации</strong>
                  <small>Користи посмирени ефекти каде што е поддржано.</small>
                </span>
                <input v-model="settings.reducedMotion" type="checkbox" />
              </label>
            </div>

            <div class="actions">
              <button class="btn eco-btn" type="submit">Зачувај поставки</button>
              <button class="btn btn-outline-secondary" type="button" @click="resetSettings">
                Ресетирај
              </button>
            </div>
          </form>
        </article>

        <aside class="settings-stack">
          <article class="settings-panel account-card">
            <h2>Профил</h2>
            <div class="account-row">
              <span>Име</span>
              <strong>{{ displayName }}</strong>
            </div>
            <div class="account-row">
              <span>Е-пошта</span>
              <strong>{{ authStore.user?.email || 'Не е достапно' }}</strong>
            </div>
            <RouterLink class="btn btn-outline-success w-100" to="/profile">
              Уреди детали за профилот
            </RouterLink>
          </article>

          <article class="settings-panel">
            <h2>Брзи линкови</h2>
            <div class="quick-links">
              <RouterLink :to="settings.startPage">Отвори почетна страница</RouterLink>
              <RouterLink to="/profile">Промени лозинка</RouterLink>
              <button type="button" @click="logout">Одјави се</button>
            </div>
          </article>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '../stores/authStore';

  const STORAGE_KEY = 'ekosrekja.settings';
  const router = useRouter();
  const authStore = useAuthStore();
  const savedMessage = ref('');
  const defaults = {
    startPage: '/',
    contentDensity: 'comfortable',
    ecoGoal: 'learn',
    weeklyDigest: true,
    quizReminders: true,
    reducedMotion: false,
  };
  const settings = reactive({ ...defaults });

  const displayName = computed(() => {
    const firstName = authStore.user?.firstName?.trim();
    const lastName = authStore.user?.lastName?.trim();
    return [firstName, lastName].filter(Boolean).join(' ') || authStore.userName;
  });

  function applySettings() {
    document.documentElement.dataset.contentDensity = settings.contentDensity;
    document.documentElement.dataset.reducedMotion = settings.reducedMotion ? 'true' : 'false';
  }

  function loadSettings() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) Object.assign(settings, defaults, JSON.parse(saved));
    applySettings();
  }

  function saveSettings() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    applySettings();
    savedMessage.value = 'Поставките се успешно зачувани.';
    window.setTimeout(() => {
      savedMessage.value = '';
    }, 2500);
  }

  function resetSettings() {
    Object.assign(settings, defaults);
    saveSettings();
  }

  function logout() {
    authStore.logout();
    router.push('/');
  }

  onMounted(() => {
    if (!authStore.isAuthenticated) {
      router.push('/login');
      return;
    }
    loadSettings();
  });
</script>

<style scoped>
  .settings-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .settings-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .settings-hero {
    align-items: center;
    background: linear-gradient(135deg, #458051 0%, #66bb6a 100%);
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) auto;
    margin-bottom: 1.5rem;
    padding: 2rem;
  }

  .settings-grid {
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 360px;
  }

  .settings-panel {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .settings-form,
  .settings-stack,
  .quick-links {
    display: grid;
    gap: 1rem;
  }

  .field {
    display: grid;
    gap: 0.45rem;
  }

  .toggle-row {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    padding: 1rem 0;
  }

  .actions {
    display: flex;
    gap: 0.75rem;
  }

  .quick-links a,
  .quick-links button {
    background: rgba(102, 187, 106, 0.1);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    color: #2e7d32;
    font-weight: 800;
    padding: 0.75rem 0.9rem;
    text-align: left;
    text-decoration: none;
  }

  @media (max-width: 991px) {
    .settings-grid,
    .settings-hero {
      grid-template-columns: 1fr;
    }
  }
</style>
