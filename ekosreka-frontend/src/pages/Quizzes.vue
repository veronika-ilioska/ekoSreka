<template>
  <section class="quizzes-page">
    <div class="quizzes-shell">
      <header class="quiz-hero">
        <div>
          <p class="eyebrow">Еко квизови</p>
          <h1>Провери што знаеш и научи една навика подобро</h1>
          <p>Избери ниво, започни краток квиз и гради го еко знаењето прашање по прашање.</p>
        </div>
        <div class="hero-stat">
          <strong>{{ totalQuizzes }}</strong>
          <span>достапни квизови</span>
        </div>
      </header>

      <div class="level-tabs" role="tablist" aria-label="Нивоа на квизови">
        <button
          v-for="level in levels"
          :key="level.key"
          type="button"
          :class="{ active: activeLevel === level.key }"
          @click="activeLevel = level.key"
        >
          {{ level.label }}
        </button>
      </div>

      <div v-if="loading" class="state-card">Квизовите се вчитуваат...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>
      <div v-else-if="visibleQuizzes.length === 0" class="state-card">Нема квизови за ова ниво.</div>

      <div v-else class="quiz-grid">
        <RouterLink v-for="quiz in visibleQuizzes" :key="quiz.id" class="quiz-card" :to="`/quizzes/${quiz.id}`">
          <img class="quiz-image" :src="imageFor(quiz)" :alt="quiz.title" />
          <div class="quiz-card-body">
            <div class="quiz-meta">
              <span class="level-chip">{{ levelLabel(quiz.level) }}</span>
              <span>{{ quiz.timeMinutes || 5 }} мин</span>
            </div>
            <h2>{{ quiz.title }}</h2>
            <p>{{ quiz.description || 'Краток квиз за да ги изостриш твоите еко навики.' }}</p>
            <span class="start-link">Започни квиз</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { RouterLink } from 'vue-router';
  import  api  from '../api';
  import beforeImage from '../img/before1.jpg';
  import afterImage from '../img/after1.jpg';
  import gameImage from '../img/game.png';
  import quizImage from '../img/quiz.png';

  const levels = [
    { key: 'ALL', label: 'Сите' },
    { key: 'BEGINNER', label: 'Почетно' },
    { key: 'INTERMEDIATE', label: 'Средно' },
    { key: 'ADVANCED', label: 'Напредно' },
    { key: 'FUN', label: 'Забавно' },
  ];
  const levelImages = { BEGINNER: beforeImage, INTERMEDIATE: afterImage, ADVANCED: quizImage, FUN: gameImage };
  const quizzes = ref([]);
  const activeLevel = ref('ALL');
  const loading = ref(false);
  const loadError = ref('');
  const totalQuizzes = computed(() => quizzes.value.length);
  const visibleQuizzes = computed(() =>
    activeLevel.value === 'ALL' ? quizzes.value : quizzes.value.filter((quiz) => quiz.level === activeLevel.value)
  );

  function levelLabel(level) {
    return levels.find((item) => item.key === level)?.label || level || 'Квиз';
  }
  function imageFor(quiz) {
    return levelImages[quiz.level] || quizImage;
  }
  async function loadQuizzes() {
    loading.value = true;
    loadError.value = '';
    try {
      const { data } = await api.get('/quizzes', { params: { size: 100 } });
      quizzes.value = data?.content || [];
    } catch {
      loadError.value = 'Квизовите не може да се вчитаат.';
      quizzes.value = [];
    } finally {
      loading.value = false;
    }
  }
  onMounted(loadQuizzes);
</script>

<style scoped>
  .quizzes-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }
  .quizzes-shell {
    margin: 0 auto;
    max-width: 1120px;
  }
  .quiz-hero {
    background: linear-gradient(135deg, rgba(69, 128, 81, 0.94), rgba(102, 187, 106, 0.84)), url('../img/quiz.png') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 220px;
    margin-bottom: 1rem;
    min-height: 300px;
    padding: 2rem;
  }
  .level-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin: 1.25rem 0;
  }
  .level-tabs button {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.25);
    border-radius: 999px;
    color: #2e7d32;
    font-weight: 800;
    padding: 0.65rem 1rem;
  }
  .level-tabs button.active {
    background: #458051;
    color: #fff;
  }
  .quiz-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .quiz-card {
    background: var(--eco-card-bg);
    border-radius: 8px;
    color: inherit;
    overflow: hidden;
    text-decoration: none;
  }
  .quiz-image {
    aspect-ratio: 16 / 10;
    object-fit: cover;
    width: 100%;
  }
  .quiz-card-body {
    display: grid;
    gap: 0.7rem;
    padding: 1.2rem;
  }
  .quiz-meta,
  .start-link {
    color: #2e7d32;
    font-weight: 800;
    text-transform: uppercase;
  }
  .state-card {
    background: var(--eco-card-bg);
    border-radius: 8px;
    padding: 1.5rem;
    text-align: center;
  }
  @media (max-width: 991px) {
    .quiz-hero,
    .quiz-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
