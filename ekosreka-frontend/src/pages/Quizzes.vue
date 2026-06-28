<template>
  <section class="quizzes-page">
    <div class="quizzes-shell">
      <header class="quiz-hero">
        <div class="hero-copy">
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
  import defaultQuizImage from '../img/default_quiz.png';

  const levels = [
    { key: 'ALL', label: 'Сите' },
    { key: 'BEGINNER', label: 'Почетно' },
    { key: 'INTERMEDIATE', label: 'Средно' },
    { key: 'ADVANCED', label: 'Напредно' },
    { key: 'FUN', label: 'Забавно' },
  ];
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
    return quiz.imageUrl || defaultQuizImage;
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
    align-items: end;
    background:
      linear-gradient(115deg, rgba(19, 67, 37, 0.96) 0%, rgba(46, 125, 50, 0.9) 48%, rgba(46, 125, 50, 0.42) 100%),
      url('../img/default_quiz.png') right center / contain no-repeat;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 280px;
    isolation: isolate;
    margin-bottom: 1.5rem;
    min-height: 260px;
    overflow: hidden;
    padding: clamp(1.5rem, 4vw, 2.5rem);
    position: relative;
  }
  .quiz-hero::after {
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    color: rgba(255, 255, 255, 0.2);
    content: '?';
    display: grid;
    font-size: 8rem;
    font-weight: 900;
    height: 180px;
    line-height: 1;
    place-items: center;
    position: absolute;
    right: 315px;
    top: -48px;
    width: 180px;
    z-index: -1;
  }
  .hero-copy {
    max-width: 700px;
  }
  .eyebrow {
    font-size: 0.78rem;
    font-weight: 900;
    letter-spacing: 0.08em;
    margin: 0 0 0.75rem;
    text-transform: uppercase;
  }
  .quiz-hero h1 {
    font-size: clamp(2.2rem, 6vw, 4.6rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
  }
  .quiz-hero p {
    font-size: 1rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.92;
  }
  .hero-stat {
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 8px;
    color: #1b2a1b;
    display: grid;
    gap: 0.6rem;
    padding: 1rem;
  }
  .hero-stat strong {
    font-size: 3rem;
    font-weight: 900;
    line-height: 1;
  }
  .hero-stat span {
    color: #2e7d32;
    font-size: 0.76rem;
    font-weight: 900;
    text-transform: uppercase;
  }
  .level-tabs {
    background: rgba(255, 255, 255, 0.72);
    border: 1px solid rgba(69, 128, 81, 0.12);
    border-radius: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin: 1.25rem 0;
    padding: 0.5rem;
  }
  .level-tabs button {
    background: transparent;
    border: 1px solid transparent;
    border-radius: 8px;
    color: #2e7d32;
    font-weight: 800;
    min-height: 42px;
    padding: 0.55rem 0.95rem;
  }
  .level-tabs button.active {
    background: #1b4d2b;
    border-color: #1b4d2b;
    box-shadow: 0 8px 18px rgba(27, 77, 43, 0.18);
    color: #fff;
  }
  .quiz-grid {
    display: grid;
    gap: 1.25rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .quiz-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    color: inherit;
    overflow: hidden;
    text-decoration: none;
    transition:
      border-color 0.2s ease,
      transform 0.2s ease;
  }
  .quiz-card::before {
    background: #2e7d32;
    content: '';
    display: block;
    height: 4px;
    width: 100%;
  }
  .quiz-card:hover {
    border-color: rgba(69, 128, 81, 0.36);
    transform: translateY(-2px);
  }
  .quiz-image {
    aspect-ratio: 18 / 10;
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
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    font-weight: 800;
    text-transform: uppercase;
  }
  .level-chip,
  .quiz-meta span {
    background: rgba(102, 187, 106, 0.1);
    border-radius: 8px;
    padding: 0.25rem 0.5rem;
  }
  .quiz-card h2 {
    color: #1b2a1b;
    font-size: 1.25rem;
    font-weight: 900;
    margin: 0;
  }
  .quiz-card p {
    color: #506650;
    line-height: 1.6;
    margin: 0;
  }
  .start-link {
    align-items: center;
    background: #1b4d2b;
    border-radius: 8px;
    color: #fff;
    justify-content: center;
    min-height: 42px;
  }
  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
    text-align: center;
  }
  @media (max-width: 991px) {
    .quiz-hero,
    .quiz-grid {
      grid-template-columns: 1fr;
    }
    .quiz-hero {
      background:
        linear-gradient(135deg, rgba(27, 77, 43, 0.96), rgba(46, 125, 50, 0.84)),
        url('../img/default_quiz.png') right 1rem bottom 1rem / 130px no-repeat;
    }
    .quiz-hero::after {
      display: none;
    }
  }
</style>
