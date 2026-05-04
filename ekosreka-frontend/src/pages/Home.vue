<template>
  <section class="home-page">
    <div class="home-shell">
      <header class="home-hero">
        <div class="hero-copy">
          <p class="eyebrow">ЕкоСреќа</p>
          <h1>Мали еко акции, направени за полесен почеток</h1>
          <p>
            Read fresh environmental news, test your knowledge, explore fun activities, and find
            practical swaps for a cleaner everyday routine.
          </p>
          <div class="hero-actions">
            <RouterLink class="btn eco-btn" to="/quizzes">Направи квиз</RouterLink>
            <RouterLink class="btn btn-outline-light" to="/news">Прочитај вести</RouterLink>
          </div>
        </div>

        <div class="hero-stats" aria-label="Site highlights">
          <div>
            <strong>{{ latest.length }}</strong>
            <span>нови вести</span>
          </div>
          <div>
            <strong>4</strong>
            <span>забавни секции</span>
          </div>
        </div>
      </header>

      <section class="section-block transformation">
        <div class="section-heading">
          <p class="eyebrow dark">Пред и потоа</p>
          <h2>Промената е полесна да се поверува кога може да се виде</h2>
        </div>

        <div class="before-after-grid">
          <article class="image-panel">
            <img src="../img/before1.jpg" alt="Polluted outdoor area before cleanup" />
            <span class="panel-label warning">Пред</span>
          </article>
          <article class="image-panel">
            <img src="../img/after1.jpg" alt="Clean outdoor area after cleanup" />
            <span class="panel-label success">Потоа</span>
          </article>
        </div>
      </section>

      <section class="section-block">
        <div class="action-grid">
          <RouterLink v-for="item in actions" :key="item.to" class="action-card" :to="item.to">
            <img :src="item.image" :alt="item.title" />
            <div>
              <span>{{ item.kicker }}</span>
              <h2>{{ item.title }}</h2>
              <p>{{ item.text }}</p>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="section-block">
        <div class="section-heading row-heading">
          <div>
            <p class="eyebrow dark">Вести</p>
            <h2>Најнови еко вести</h2>
          </div>
          <RouterLink to="/news" class="section-link">Сите вести</RouterLink>
        </div>

        <div v-if="loadingNews" class="state-card">Прикажување на најновите вести...</div>
        <div v-else-if="newsError" class="alert alert-danger">{{ newsError }}</div>
        <div v-else-if="latest.length" class="news-grid">
          <NewsCard v-for="item in latest" :key="item.id" :item="item" />
        </div>
        <div v-else class="state-card">Нема објавени вести.</div>
      </section>

      <section class="contact-strip">
        <div>
          <h2>Имате приказна или интересна еко идеа?</h2>
          <p>Споделете ја со заедницата и помогнете мала идеа да порасне во голема промена.</p>
        </div>
        <RouterLink class="btn eco-btn" to="/about">За нас</RouterLink>
      </section>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { RouterLink } from 'vue-router';
  import { api } from '../api';
  import NewsCard from '../components/NewsCard.vue';
  import bagImage from '../img/bag.png';
  import gameImage from '../img/game.png';
  import quizImage from '../img/quiz.png';

  const latest = ref([]);
  const loadingNews = ref(false);
  const newsError = ref('');

  const actions = [
    {
      to: '/quizzes',
      title: 'Quizzes',
      kicker: 'Learn',
      text: 'Quick challenges that make eco facts easier to remember.',
      image: quizImage,
    },
    {
      to: '/shop',
      title: 'Eco shop',
      kicker: 'Swap',
      text: 'Reusable everyday picks for cleaner habits at home.',
      image: bagImage,
    },
    {
      to: '/fun',
      title: 'Fun',
      kicker: 'Explore',
      text: 'Photos, videos, horoscope tips, and games in one place.',
      image: gameImage,
    },
  ];

  async function loadLatestNews() {
    loadingNews.value = true;
    newsError.value = '';

    try {
      const { data } = await api.get('/news', { params: { page: 0, size: 3 } });
      latest.value = data?.content || [];
    } catch (error) {
      newsError.value = 'Could not load latest news.';
      latest.value = [];
    } finally {
      loadingNews.value = false;
    }
  }

  onMounted(loadLatestNews);
</script>

<style scoped>
  .home-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .home-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .home-hero {
    align-items: end;
    background:
      linear-gradient(135deg, rgba(36, 77, 43, 0.94), rgba(69, 128, 81, 0.84)),
      url('../img/after1.jpg') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 260px;
    min-height: 420px;
    padding: clamp(1.5rem, 4vw, 3rem);
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin: 0 0 0.65rem;
    text-transform: uppercase;
  }

  .eyebrow.dark {
    color: #2e7d32;
  }

  .hero-copy h1 {
    font-size: clamp(2.4rem, 6vw, 5rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
    max-width: 820px;
  }

  .hero-copy p {
    font-size: 1.08rem;
    line-height: 1.7;
    margin: 0;
    max-width: 650px;
    opacity: 0.94;
  }

  .hero-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-top: 1.4rem;
  }

  .hero-actions .btn {
    border-radius: 8px;
    font-weight: 800;
    padding: 0.8rem 1rem;
  }

  .hero-stats {
    display: grid;
    gap: 0.75rem;
  }

  .hero-stats div {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.28);
    border-radius: 8px;
    padding: 1rem;
  }

  .hero-stats strong {
    display: block;
    font-size: 2.2rem;
    line-height: 1;
  }

  .hero-stats span {
    display: block;
    font-size: 0.78rem;
    font-weight: 800;
    margin-top: 0.35rem;
    text-transform: uppercase;
  }

  .section-block {
    margin-top: 1.5rem;
  }

  .section-heading {
    margin-bottom: 1rem;
  }

  .section-heading h2,
  .contact-strip h2 {
    color: #1b2a1b;
    font-size: clamp(1.7rem, 4vw, 2.6rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.08;
    margin: 0;
  }

  .row-heading {
    align-items: end;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
  }

  .section-link {
    color: #2e7d32;
    font-weight: 800;
    text-decoration: none;
  }

  .before-after-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .image-panel {
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.1);
    min-height: 260px;
    overflow: hidden;
    position: relative;
  }

  .image-panel img {
    height: 100%;
    object-fit: cover;
    width: 100%;
  }

  .panel-label {
    border-radius: 999px;
    bottom: 1rem;
    color: #fff;
    font-size: 0.82rem;
    font-weight: 800;
    left: 1rem;
    padding: 0.45rem 0.75rem;
    position: absolute;
    text-transform: uppercase;
  }

  .panel-label.warning {
    background: rgba(198, 40, 40, 0.9);
  }

  .panel-label.success {
    background: rgba(46, 125, 50, 0.9);
  }

  .action-grid,
  .news-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .action-card {
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

  .action-card:hover {
    box-shadow: 0 14px 30px rgba(27, 42, 27, 0.16);
    transform: translateY(-3px);
  }

  .action-card img {
    aspect-ratio: 16 / 10;
    object-fit: cover;
    width: 100%;
  }

  .action-card div {
    display: grid;
    gap: 0.55rem;
    padding: 1rem;
  }

  .action-card span {
    color: #2e7d32;
    font-size: 0.78rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  .action-card h2 {
    color: #1b2a1b;
    font-size: 1.25rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0;
  }

  .action-card p {
    color: #506650;
    line-height: 1.55;
    margin: 0;
  }

  .contact-strip,
  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
  }

  .contact-strip {
    align-items: center;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    margin-top: 1.5rem;
    padding: 1.5rem;
  }

  .contact-strip p {
    color: #506650;
    margin: 0.45rem 0 0;
  }

  .state-card {
    color: #506650;
    padding: 1.5rem;
    text-align: center;
  }

  @media (max-width: 991px) {
    .home-hero,
    .before-after-grid,
    .action-grid,
    .news-grid {
      grid-template-columns: 1fr;
    }

    .hero-stats {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      max-width: 460px;
    }
  }

  @media (max-width: 640px) {
    .home-hero {
      min-height: 0;
    }

    .hero-stats,
    .row-heading,
    .contact-strip {
      align-items: stretch;
      flex-direction: column;
      grid-template-columns: 1fr;
    }
  }
</style>
