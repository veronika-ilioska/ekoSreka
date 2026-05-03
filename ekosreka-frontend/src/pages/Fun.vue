<template>
  <section class="fun-page">
    <div class="fun-shell">
      <header class="fun-hero">
        <div class="hero-copy">
          <span class="hero-kicker">Eco activities</span>
          <h1>Explore, play, and learn in a greener way</h1>
          <p>
            Discover nature photos, short videos, eco horoscope tips, and home-friendly games
            collected in one calm little activity hub.
          </p>
        </div>

        <div class="hero-panel" aria-label="Fun content summary">
          <div class="hero-stat">
            <strong>{{ totalCount }}</strong>
            <span>total items</span>
          </div>
          <div class="hero-stat">
            <strong>{{ readyCount }}</strong>
            <span>sections ready</span>
          </div>
        </div>
      </header>

      <div class="activity-grid">
        <RouterLink
          v-for="activity in activities"
          :key="activity.key"
          :to="activity.to"
          class="activity-card"
          :aria-label="`Open ${activity.title}`"
        >
          <img class="activity-image" :src="activity.image" :alt="activity.title" />
          <div class="activity-content">
            <div class="activity-topline">
              <span class="activity-tag">{{ activity.tag }}</span>
              <span class="activity-count">{{ formatCount(activity.key) }}</span>
            </div>
            <h2>{{ activity.title }}</h2>
            <p>{{ activity.description }}</p>
            <span class="activity-action">Open section</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive } from 'vue';
  import { api } from '../api';
  import beforeImage from '../img/before1.jpg';
  import afterImage from '../img/after1.jpg';
  import gameImage from '../img/game.png';
  import quizImage from '../img/quiz.png';

  const ENDPOINTS = {
    photos: '/media/photos/count',
    videos: '/media/videos/count',
    horoscope: '/horoscope/count',
    games: '/games/count',
  };

  const counts = reactive({
    photos: 0,
    videos: 0,
    horoscope: 0,
    games: 0,
  });

  const activities = [
    {
      key: 'photos',
      to: '/photos',
      title: 'Photos',
      tag: 'Nature gallery',
      description: 'Browse visual stories from cleanups, landscapes, and everyday eco moments.',
      image: beforeImage,
    },
    {
      key: 'videos',
      to: '/videos',
      title: 'Videos',
      tag: 'Watch and learn',
      description: 'Short clips and useful eco content for quick inspiration between tasks.',
      image: afterImage,
    },
    {
      key: 'horoscope',
      to: '/horoscope',
      title: 'Eco horoscope',
      tag: 'Daily tip',
      description: 'A playful way to find a small sustainable action for your sign.',
      image: quizImage,
    },
    {
      key: 'games',
      to: '/games',
      title: 'Home games',
      tag: 'Challenges',
      description: 'Mini games and simple challenges that make eco habits easier to remember.',
      image: gameImage,
    },
  ];

  const totalCount = computed(() => Object.values(counts).reduce((sum, count) => sum + count, 0));

  const readyCount = computed(() => Object.values(counts).filter((count) => count > 0).length);

  function normalizeCount(data) {
    const value = typeof data === 'number' ? data : Number(data?.count ?? data?.total ?? 0);
    return Number.isFinite(value) ? value : 0;
  }

  function formatCount(key) {
    const count = counts[key] ?? 0;
    return `${count} ${count === 1 ? 'item' : 'items'}`;
  }

  async function fetchCount(key) {
    try {
      const { data } = await api.get(ENDPOINTS[key]);
      counts[key] = normalizeCount(data);
    } catch (error) {
      counts[key] = 0;
    }
  }

  onMounted(() => Promise.all(Object.keys(ENDPOINTS).map(fetchCount)));
</script>

<style scoped>
  .fun-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .fun-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .fun-hero {
    align-items: stretch;
    background:
      linear-gradient(135deg, rgba(69, 128, 81, 0.94), rgba(102, 187, 106, 0.86)),
      url('../img/after1.jpg') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 300px;
    margin-bottom: 1.5rem;
    min-height: 280px;
    overflow: hidden;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .hero-copy {
    align-self: end;
    max-width: 680px;
  }

  .hero-kicker {
    display: block;
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin-bottom: 0.75rem;
    text-transform: uppercase;
  }

  .hero-copy h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.02;
    margin: 0 0 1rem;
    max-width: 760px;
  }

  .hero-copy p {
    font-size: 1.05rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.94;
  }

  .hero-panel {
    align-self: end;
    display: grid;
    gap: 0.75rem;
  }

  .hero-stat {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 8px;
    padding: 1rem;
  }

  .hero-stat strong {
    display: block;
    font-size: 2rem;
    line-height: 1;
  }

  .hero-stat span {
    display: block;
    font-size: 0.82rem;
    font-weight: 700;
    margin-top: 0.35rem;
    opacity: 0.85;
    text-transform: uppercase;
  }

  .activity-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(12, 1fr);
  }

  .activity-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    color: inherit;
    display: grid;
    grid-column: span 6;
    grid-template-columns: 42% 1fr;
    min-height: 230px;
    overflow: hidden;
    text-decoration: none;
    transition:
      box-shadow 0.2s ease,
      transform 0.2s ease;
  }

  .activity-card:hover {
    box-shadow: 0 14px 30px rgba(27, 42, 27, 0.16);
    transform: translateY(-3px);
  }

  .activity-image {
    height: 100%;
    min-height: 230px;
    object-fit: cover;
    width: 100%;
  }

  .activity-content {
    display: flex;
    flex-direction: column;
    gap: 0.65rem;
    padding: 1.25rem;
  }

  .activity-topline {
    align-items: center;
    display: flex;
    gap: 0.75rem;
    justify-content: space-between;
  }

  .activity-tag,
  .activity-count {
    border-radius: 999px;
    font-size: 0.74rem;
    font-weight: 800;
    padding: 0.32rem 0.65rem;
    text-transform: uppercase;
  }

  .activity-tag {
    background: rgba(102, 187, 106, 0.16);
    color: #2e7d32;
  }

  .activity-count {
    color: #506650;
  }

  .activity-content h2 {
    color: #1b2a1b;
    font-size: 1.45rem;
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.15;
    margin: 0;
  }

  .activity-content p {
    color: #506650;
    line-height: 1.55;
    margin: 0;
  }

  .activity-action {
    color: #2e7d32;
    font-size: 0.86rem;
    font-weight: 800;
    margin-top: auto;
    text-transform: uppercase;
  }

  @media (max-width: 991px) {
    .fun-hero {
      grid-template-columns: 1fr;
    }

    .hero-panel {
      grid-template-columns: repeat(2, 1fr);
    }

    .activity-card {
      grid-column: span 12;
    }
  }

  @media (max-width: 640px) {
    .fun-hero {
      min-height: 0;
    }

    .hero-panel {
      grid-template-columns: 1fr;
    }

    .activity-card {
      grid-template-columns: 1fr;
    }

    .activity-image {
      aspect-ratio: 16 / 10;
      min-height: 0;
    }
  }
</style>
