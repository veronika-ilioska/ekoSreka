<template>
  <section class="eco-section">
    <div class="container">
      <div v-if="beginner?.length" class="mb-5">
        <h2 class="eco-title mb-3">Почетно</h2>
        <div class="row g-4 align-items-stretch">
          <div v-for="q in beginner" :key="q.id" class="col-12 col-sm-6 col-lg-4">
            <RouterLink :to="`/quizzes/${q.id}`" class="poster-link">
              <article
                class="quiz-poster"
                :style="{ '--bg': `url(${poster(q)})` }"
                role="button"
                :aria-label="q.title"
              >
                <div class="overlay"></div>
                <div class="content">
                  <h5 class="title mb-2">{{ q.title }}</h5>
                  <p class="desc text-white-50 mb-3">{{ q.description }}</p>
                  <span class="chip"
                    >Започни <img class="icon" src="../img/guide.svg" alt=""
                  /></span>
                </div>
              </article>
            </RouterLink>
          </div>
        </div>
      </div>

      <div v-if="intermediate?.length" class="mb-5">
        <h2 class="eco-title mb-3">Средно</h2>
        <div class="row g-4 align-items-stretch">
          <div v-for="q in intermediate" :key="q.id" class="col-12 col-sm-6 col-lg-4">
            <RouterLink :to="`/quizzes/${q.id}`" class="poster-link">
              <article class="quiz-poster" :style="{ '--bg': `url(${poster(q)})` }">
                <div class="overlay"></div>
                <div class="content">
                  <h5 class="title mb-2">{{ q.title }}</h5>
                  <p class="desc text-white-50 mb-3">{{ q.description }}</p>
                  <span class="chip"
                    >Започни <img class="icon" src="../img/guide.svg" alt=""
                  /></span>
                </div>
              </article>
            </RouterLink>
          </div>
        </div>
      </div>

      <div v-if="advanced?.length" class="mb-5">
        <h2 class="eco-title mb-3">Напредно</h2>
        <div class="row g-4 align-items-stretch">
          <div v-for="q in advanced" :key="q.id" class="col-12 col-sm-6 col-lg-4">
            <RouterLink :to="`/quizzes/${q.id}`" class="poster-link">
              <article class="quiz-poster" :style="{ '--bg': `url(${poster(q)})` }">
                <div class="overlay"></div>
                <div class="content">
                  <h5 class="title mb-2">{{ q.title }}</h5>
                  <p class="desc text-white-50 mb-3">{{ q.description }}</p>
                  <span class="chip"
                    >Започни <img class="icon" src="../img/guide.svg" alt=""
                  /></span>
                </div>
              </article>
            </RouterLink>
          </div>
        </div>
      </div>

      <div v-if="fun?.length">
        <h2 class="eco-title mb-3">Забавно</h2>
        <div class="row g-4 align-items-stretch">
          <div v-for="q in fun" :key="q.id" class="col-12 col-sm-6 col-lg-4">
            <RouterLink :to="`/quizzes/${q.id}`" class="poster-link">
              <article class="quiz-poster" :style="{ '--bg': `url(${poster(q)})` }">
                <div class="overlay"></div>
                <div class="content">
                  <h5 class="title mb-2">{{ q.title }}</h5>
                  <p class="desc text-white-50 mb-3">{{ q.description }}</p>
                  <span class="chip"
                    >Започни <img class="icon" src="../img/guide.svg" alt=""
                  /></span>
                </div>
              </article>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
<script setup>
  import { ref, onMounted } from 'vue';
  import { api } from '../api';
  import { RouterLink } from 'vue-router';

  const beginner = ref([]);
  const intermediate = ref([]);
  const advanced = ref([]);
  const fun = ref([]);

  async function load(level, targetRef) {
    const { data } = await api.get('/quizzes', { params: { level, size: 100 } });
    targetRef.value = data?.content || [];
  }

  const poster = (q) =>
    q?.coverImage || q?.image || q?.thumbnail || '/images/placeholders/quiz.jpg';

  onMounted(async () => {
    await Promise.all([
      load('BEGINNER', beginner),
      load('INTERMEDIATE', intermediate),
      load('ADVANCED', advanced),
      load('FUN', fun),
    ]);
  });
</script>
<style scoped>
  .poster-link {
    text-decoration: none;
    display: block;
  }

  .quiz-poster {
    position: relative;
    min-height: 260px;
    border-radius: 18px;
    overflow: hidden;
    background-image: var(--bg);
    background-size: cover;
    background-position: center;
    box-shadow:
      0 6px 20px rgba(0, 0, 0, 0.15),
      inset 0 0 0 1px rgba(255, 255, 255, 0.05);
    transition:
      transform 0.22s ease,
      box-shadow 0.22s ease;
    color: #fff;
    isolation: isolate;
  }
  .quiz-poster:hover {
    transform: translateY(-4px);
    box-shadow:
      0 14px 28px rgba(0, 0, 0, 0.22),
      inset 0 0 0 1px rgba(255, 255, 255, 0.07);
  }

  .overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0.12) 0%,
      rgba(0, 0, 0, 0.35) 45%,
      rgba(0, 0, 0, 0.6) 100%
    );
    z-index: 0;
  }

  .content {
    position: absolute;
    inset: 18px;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    z-index: 1;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.45);
  }
  .title {
    font-weight: 800;
    letter-spacing: 0.2px;
    font-size: 1.4rem;
    line-height: 1.2;
  }
  .desc {
    margin: 0 0 12px 0;
  }

  .chip {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(2px);
    font-weight: 700;
    font-size: 0.85rem;
    color: #fff;
  }

  @media (min-width: 992px) {
    .quiz-poster {
      min-height: 300px;
    }
    .title {
      font-size: 1.6rem;
    }
  }
  .icon {
    width: 18px;
    height: 18px;
    vertical-align: -2px;
    margin-right: 6px;
  }
</style>
