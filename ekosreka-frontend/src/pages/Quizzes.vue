<template>
  <section class="quizzes-page">
    <div class="quizzes-shell">
      <header class="quiz-hero">
        <div>
          <p class="eyebrow">Eco quizzes</p>
          <h1>Test what you know, then learn one habit better</h1>
          <p>Choose a level, start a short quiz, and build your eco knowledge one question at a time.</p>
        </div>
        <div class="hero-stat">
          <strong>{{ totalQuizzes }}</strong>
          <span>available quizzes</span>
        </div>
      </header>

      <div class="level-tabs" role="tablist" aria-label="Quiz levels">
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

      <div v-if="loading" class="state-card">Loading quizzes...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>
      <div v-else-if="visibleQuizzes.length === 0" class="state-card">No quizzes found for this level.</div>

      <div v-else class="quiz-grid">
        <RouterLink
          v-for="quiz in visibleQuizzes"
          :key="quiz.id"
          class="quiz-card"
          :to="`/quizzes/${quiz.id}`"
        >
          <img class="quiz-image" :src="imageFor(quiz)" :alt="quiz.title" />
          <div class="quiz-card-body">
            <div class="quiz-meta">
              <span class="level-chip">{{ levelLabel(quiz.level) }}</span>
              <span>{{ quiz.timeMinutes || 5 }} min</span>
            </div>
            <h2>{{ quiz.title }}</h2>
            <p>{{ quiz.description || 'A short quiz to sharpen your eco instincts.' }}</p>
            <span class="start-link">Start quiz</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { RouterLink } from 'vue-router';
  import { api } from '../api';
  import beforeImage from '../img/before1.jpg';
  import afterImage from '../img/after1.jpg';
  import gameImage from '../img/game.png';
  import quizImage from '../img/quiz.png';

  const levels = [
    { key: 'ALL', label: 'All' },
    { key: 'BEGINNER', label: 'Beginner' },
    { key: 'INTERMEDIATE', label: 'Intermediate' },
    { key: 'ADVANCED', label: 'Advanced' },
    { key: 'FUN', label: 'Fun' },
  ];

  const levelImages = {
    BEGINNER: beforeImage,
    INTERMEDIATE: afterImage,
    ADVANCED: quizImage,
    FUN: gameImage,
  };

  const quizzes = ref([]);
  const activeLevel = ref('ALL');
  const loading = ref(false);
  const loadError = ref('');

  const totalQuizzes = computed(() => quizzes.value.length);

  const visibleQuizzes = computed(() => {
    if (activeLevel.value === 'ALL') return quizzes.value;
    return quizzes.value.filter((quiz) => quiz.level === activeLevel.value);
  });

  function levelLabel(level) {
    return levels.find((item) => item.key === level)?.label || level || 'Quiz';
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
    } catch (error) {
      loadError.value = 'Could not load quizzes.';
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
      linear-gradient(135deg, rgba(69, 128, 81, 0.94), rgba(102, 187, 106, 0.84)),
      url('../img/quiz.png') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 220px;
    margin-bottom: 1rem;
    min-height: 300px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin: 0 0 0.65rem;
    text-transform: uppercase;
  }

  .quiz-hero h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.02;
    margin: 0 0 1rem;
    max-width: 760px;
  }

  .quiz-hero p {
    font-size: 1.04rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.94;
  }

  .hero-stat {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.32);
    border-radius: 8px;
    padding: 1rem;
  }

  .hero-stat strong {
    display: block;
    font-size: 2.3rem;
    line-height: 1;
  }

  .hero-stat span {
    display: block;
    font-size: 0.8rem;
    font-weight: 800;
    margin-top: 0.35rem;
    text-transform: uppercase;
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
    border-color: #458051;
    color: #fff;
  }

  .quiz-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .quiz-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    color: inherit;
    display: grid;
    overflow: hidden;
    text-decoration: none;
    transition:
      box-shadow 0.2s ease,
      transform 0.2s ease;
  }

  .quiz-card:hover {
    box-shadow: 0 14px 30px rgba(27, 42, 27, 0.16);
    transform: translateY(-3px);
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

  .quiz-meta {
    align-items: center;
    color: #506650;
    display: flex;
    font-size: 0.84rem;
    font-weight: 800;
    justify-content: space-between;
    text-transform: uppercase;
  }

  .level-chip {
    background: rgba(102, 187, 106, 0.16);
    border-radius: 999px;
    color: #2e7d32;
    padding: 0.32rem 0.65rem;
  }

  .quiz-card h2 {
    color: #1b2a1b;
    font-size: 1.25rem;
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.15;
    margin: 0;
  }

  .quiz-card p {
    color: #506650;
    line-height: 1.55;
    margin: 0;
  }

  .start-link {
    color: #2e7d32;
    font-size: 0.86rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    color: #506650;
    padding: 1.5rem;
    text-align: center;
  }

  @media (max-width: 991px) {
    .quiz-hero {
      grid-template-columns: 1fr;
    }

    .hero-stat {
      max-width: 240px;
    }

    .quiz-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    .quiz-hero {
      min-height: 0;
    }

    .quiz-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
