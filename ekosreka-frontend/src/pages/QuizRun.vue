<template>
  <section class="quiz-run-page">
    <div class="quiz-run-shell">
      <div v-if="loading" class="state-card">Loading quiz...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>

      <template v-else-if="quiz">
        <header class="run-hero">
          <RouterLink class="back-link" to="/quizzes">Back to quizzes</RouterLink>
          <h1>{{ quiz.title }}</h1>
          <p>{{ quiz.description }}</p>
          <div class="run-meta">
            <span>{{ quiz.level }}</span>
            <span>{{ quiz.timeMinutes || 5 }} min</span>
            <span>{{ totalQs }} questions</span>
          </div>
          <div v-if="!result" class="progress-track" aria-label="Quiz progress">
            <div class="progress-bar" :style="{ width: progressPct + '%' }"></div>
          </div>
        </header>

        <article v-if="!result" class="question-card">
          <div class="question-topline">
            <span>Question {{ currentIndex + 1 }} of {{ totalQs }}</span>
            <span>{{ answeredCount }} answered</span>
          </div>

          <h2>{{ currentQuestion.text }}</h2>

          <div class="options">
            <label
              v-for="option in currentQuestion.options"
              :key="option.id"
              class="option"
              :class="{ selected: answers[currentQuestion.id] === option.id }"
            >
              <input
                type="radio"
                :name="`q_${currentQuestion.id}`"
                :value="option.id"
                :checked="answers[currentQuestion.id] === option.id"
                @change="pick(currentQuestion.id, option.id)"
              />
              <span>{{ option.text }}</span>
            </label>
          </div>

          <div class="question-actions">
            <button class="btn btn-outline-secondary" type="button" :disabled="currentIndex === 0" @click="previousQuestion">
              Previous
            </button>
            <button
              v-if="!isLastQuestion"
              class="btn eco-btn"
              type="button"
              :disabled="!answers[currentQuestion.id]"
              @click="nextQuestion"
            >
              Next
            </button>
            <button
              v-else
              class="btn eco-btn"
              type="button"
              :disabled="answeredCount < totalQs || submitting"
              @click="submitQuiz"
            >
              {{ submitting ? 'Submitting...' : 'Submit quiz' }}
            </button>
          </div>
        </article>

        <section v-else class="results-panel">
          <div class="score-card">
            <span>Your score</span>
            <strong>{{ result.score }}/{{ result.total }}</strong>
            <p>{{ scoreMessage }}</p>
          </div>

          <div class="result-list">
            <article
              v-for="detail in result.details"
              :key="detail.questionId"
              class="result-row"
              :class="detail.correct ? 'correct' : 'incorrect'"
            >
              <div>
                <h3>{{ questionText(detail.questionId) }}</h3>
                <p>Your answer: {{ optionText(detail.questionId, detail.chosenOptionId) }}</p>
                <p v-if="!detail.correct">Correct answer: {{ optionText(detail.questionId, detail.correctOptionId) }}</p>
              </div>
              <span>{{ detail.correct ? 'Correct' : 'Review' }}</span>
            </article>
          </div>

          <div class="results-actions">
            <button class="btn btn-outline-success" type="button" @click="restartQuiz">Try again</button>
            <RouterLink class="btn eco-btn" to="/quizzes">Choose another quiz</RouterLink>
          </div>
        </section>
      </template>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { RouterLink, useRoute } from 'vue-router';
  import { api } from '../api';

  const route = useRoute();
  const quizId = Number(route.params.id);

  const quiz = ref(null);
  const answers = ref({});
  const result = ref(null);
  const currentIndex = ref(0);
  const loading = ref(false);
  const submitting = ref(false);
  const loadError = ref('');

  const totalQs = computed(() => quiz.value?.questions?.length || 0);
  const answeredCount = computed(() => Object.keys(answers.value).length);
  const progressPct = computed(() =>
    totalQs.value ? Math.round((answeredCount.value / totalQs.value) * 100) : 0
  );
  const currentQuestion = computed(() => quiz.value.questions[currentIndex.value]);
  const isLastQuestion = computed(() => currentIndex.value >= totalQs.value - 1);
  const scoreMessage = computed(() => {
    if (!result.value?.total) return '';
    const pct = result.value.score / result.value.total;
    if (pct >= 0.8) return 'Excellent work. Your eco knowledge is looking strong.';
    if (pct >= 0.5) return 'Good start. Review the missed answers and try again.';
    return 'Keep going. Every quiz makes the next eco choice easier.';
  });

  function pick(questionId, optionId) {
    answers.value = { ...answers.value, [questionId]: optionId };
  }

  function nextQuestion() {
    if (!isLastQuestion.value) currentIndex.value += 1;
  }

  function previousQuestion() {
    if (currentIndex.value > 0) currentIndex.value -= 1;
  }

  async function submitQuiz() {
    submitting.value = true;
    try {
      const payload = Object.entries(answers.value).map(([qid, oid]) => ({
        questionId: Number(qid),
        optionId: Number(oid),
      }));
      const { data } = await api.post(`/quizzes/${quizId}/submit`, payload);
      result.value = data;
    } finally {
      submitting.value = false;
    }
  }

  function restartQuiz() {
    answers.value = {};
    result.value = null;
    currentIndex.value = 0;
  }

  function optionText(questionId, optionId) {
    const question = quiz.value?.questions?.find((item) => item.id === questionId);
    const option = question?.options?.find((item) => item.id === optionId);
    return option?.text ?? 'No answer';
  }

  function questionText(questionId) {
    return quiz.value?.questions?.find((item) => item.id === questionId)?.text ?? '';
  }

  async function loadQuiz() {
    loading.value = true;
    loadError.value = '';

    try {
      const { data } = await api.get(`/quizzes/${quizId}`);
      quiz.value = data;
    } catch (error) {
      loadError.value = 'Could not load this quiz.';
    } finally {
      loading.value = false;
    }
  }

  onMounted(loadQuiz);
</script>

<style scoped>
  .quiz-run-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .quiz-run-shell {
    margin: 0 auto;
    max-width: 920px;
  }

  .run-hero,
  .question-card,
  .results-panel,
  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
  }

  .run-hero {
    background:
      linear-gradient(135deg, rgba(36, 77, 43, 0.95), rgba(69, 128, 81, 0.9)),
      url('../img/quiz.png') center / cover;
    color: #fff;
    margin-bottom: 1rem;
    padding: 2rem;
  }

  .back-link {
    color: rgba(255, 255, 255, 0.88);
    display: inline-block;
    font-weight: 800;
    margin-bottom: 1rem;
    text-decoration: none;
  }

  .run-hero h1 {
    font-size: clamp(2rem, 5vw, 3.5rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.05;
    margin: 0 0 0.75rem;
  }

  .run-hero p {
    line-height: 1.7;
    margin: 0;
    max-width: 680px;
    opacity: 0.92;
  }

  .run-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin: 1rem 0;
  }

  .run-meta span {
    background: rgba(255, 255, 255, 0.16);
    border-radius: 999px;
    font-size: 0.82rem;
    font-weight: 800;
    padding: 0.4rem 0.75rem;
    text-transform: uppercase;
  }

  .progress-track {
    background: rgba(255, 255, 255, 0.24);
    border-radius: 999px;
    height: 9px;
    overflow: hidden;
  }

  .progress-bar {
    background: #b9f6ca;
    height: 100%;
    transition: width 0.2s ease;
  }

  .question-card,
  .results-panel,
  .state-card {
    padding: 1.5rem;
  }

  .question-topline {
    color: #506650;
    display: flex;
    font-size: 0.84rem;
    font-weight: 800;
    justify-content: space-between;
    margin-bottom: 1rem;
    text-transform: uppercase;
  }

  .question-card h2 {
    color: #1b2a1b;
    font-size: 1.6rem;
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.25;
    margin: 0 0 1rem;
  }

  .options {
    display: grid;
    gap: 0.75rem;
  }

  .option {
    align-items: center;
    background: rgba(102, 187, 106, 0.08);
    border: 1px solid rgba(69, 128, 81, 0.18);
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    gap: 0.8rem;
    padding: 0.95rem 1rem;
    transition:
      background 0.2s ease,
      border-color 0.2s ease;
  }

  .option.selected {
    background: rgba(102, 187, 106, 0.2);
    border-color: #458051;
  }

  .option input {
    accent-color: #458051;
  }

  .option span {
    color: #1b2a1b;
    font-weight: 700;
  }

  .question-actions,
  .results-actions {
    display: flex;
    gap: 0.75rem;
    justify-content: flex-end;
    margin-top: 1.25rem;
  }

  .score-card {
    background: rgba(102, 187, 106, 0.12);
    border: 1px solid rgba(69, 128, 81, 0.18);
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 1.25rem;
    text-align: center;
  }

  .score-card span {
    color: #506650;
    font-size: 0.82rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  .score-card strong {
    color: #1b2a1b;
    display: block;
    font-size: 3rem;
    line-height: 1;
    margin: 0.35rem 0;
  }

  .score-card p {
    color: #506650;
    margin: 0;
  }

  .result-list {
    display: grid;
    gap: 0.75rem;
  }

  .result-row {
    align-items: start;
    border-radius: 8px;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    padding: 1rem;
  }

  .result-row.correct {
    background: rgba(102, 187, 106, 0.12);
  }

  .result-row.incorrect {
    background: rgba(198, 40, 40, 0.08);
  }

  .result-row h3 {
    color: #1b2a1b;
    font-size: 1rem;
    font-weight: 800;
    margin: 0 0 0.5rem;
  }

  .result-row p {
    color: #506650;
    margin: 0.2rem 0;
  }

  .result-row > span {
    border-radius: 999px;
    font-size: 0.78rem;
    font-weight: 800;
    padding: 0.35rem 0.65rem;
    text-transform: uppercase;
  }

  .correct > span {
    background: #458051;
    color: #fff;
  }

  .incorrect > span {
    background: #c62828;
    color: #fff;
  }

  .state-card {
    color: #506650;
    text-align: center;
  }

  @media (max-width: 640px) {
    .run-hero,
    .question-card,
    .results-panel {
      padding: 1.25rem;
    }

    .question-topline,
    .question-actions,
    .results-actions,
    .result-row {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
