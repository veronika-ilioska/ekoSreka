<template>
  <section class="eco-section fun-page">
    <div class="container">
      <header class="fun-hero">
        <span class="fun-kicker">Еко забава</span>
        <h1>Забавни содржини за зелени навики</h1>
        <p>
          Истражи фотографии, видеа, хороскопски објави и игри кои ја прават грижата за природата
          полесна и поинтересна.
        </p>
      </header>

      <div class="fun-grid">
        <RouterLink v-for="item in sections" :key="item.to" :to="item.to" class="fun-card">
          <span class="fun-icon" v-html="item.icon" aria-hidden="true"></span>
          <span class="fun-count">{{ counts[item.key] }} записи</span>
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
          <span class="fun-action">Отвори</span>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, reactive } from 'vue';
  import  api  from '../api';

  const counts = reactive({ photos: 0, videos: 0, horoscope: 0, games: 0, quizzes: 0 });

  const endpoints = {
    photos: '/media/photos/count',
    videos: '/media/videos/count',
    horoscope: '/horoscope/count',
    games: '/games/count',
    quizzes: '/quizzes?size=1',
  };

  const icons = {
    photos:
      '<svg viewBox="0 0 24 24"><path d="M5 6h3l1.2-2h5.6L16 6h3a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Zm7 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z"/></svg>',
    videos:
      '<svg viewBox="0 0 24 24"><path d="M4 6h10a3 3 0 0 1 3 3v.7l4-2.2v9l-4-2.2v.7a3 3 0 0 1-3 3H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z"/></svg>',
    horoscope:
      '<svg viewBox="0 0 24 24"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm1 5v4h4v2h-4v4h-2v-4H7v-2h4V7h2Z"/></svg>',
    games:
      '<svg viewBox="0 0 24 24"><path d="M7 9h10a5 5 0 0 1 3.8 8.3 3.2 3.2 0 0 1-4.5.3L14.8 16H9.2l-1.5 1.6a3.2 3.2 0 0 1-4.5-.3A5 5 0 0 1 7 9Zm1 3v2h2v-2H8Zm1-1h2v-2H9v2Zm8.5.2a1.1 1.1 0 1 0 0 2.2 1.1 1.1 0 0 0 0-2.2Z"/></svg>',
    quizzes:
      '<svg viewBox="0 0 24 24"><path d="M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Zm2 4v2h6V7H9Zm0 4v2h6v-2H9Zm0 4v2h4v-2H9Z"/></svg>',
  };

  const sections = [
    {
      key: 'photos',
      to: '/photos',
      title: 'Фотографии',
      description: 'Приказни во слики од природата, акциите и малите зелени моменти.',
      icon: icons.photos,
    },
    {
      key: 'videos',
      to: '/videos',
      title: 'Видеа',
      description: 'Кратки клипови и документарци за идеи што лесно се применуваат.',
      icon: icons.videos,
    },
    {
      key: 'horoscope',
      to: '/horoscope',
      title: 'Хороскоп',
      description: 'Сите еко хороскопски објави собрани на едно место.',
      icon: icons.horoscope,
    },
    {
      key: 'games',
      to: '/games',
      title: 'Забавни игри',
      description: 'Мини-игри и предизвици за учење преку игра.',
      icon: icons.games,
    },
    {
      key: 'quizzes',
      to: '/quizzes',
      title: 'Квизови',
      description: 'Провери го твоето еко знаење и следи ги резултатите.',
      icon: icons.quizzes,
    },
  ];

  async function fetchCount(key) {
    try {
      const { data } = await api.get(endpoints[key]);
      const value = typeof data === 'number' ? data : Number(data?.count ?? data?.totalElements ?? data?.total ?? 0);
      counts[key] = Number.isFinite(value) ? value : 0;
    } catch {
      counts[key] = 0;
    }
  }

  onMounted(() => Promise.all(Object.keys(endpoints).map(fetchCount)));
</script>

<style scoped>
  .fun-page {
    background:
      linear-gradient(180deg, rgba(226, 239, 229, 0.95), rgba(255, 255, 255, 0.65)),
      radial-gradient(circle at 12% 18%, rgba(129, 199, 132, 0.35), transparent 28%);
  }

  .container {
    max-width: 1120px;
    margin: 0 auto;
    padding: 0 16px;
  }

  .fun-hero {
    max-width: 760px;
    margin-bottom: 28px;
  }

  .fun-kicker {
    display: inline-flex;
    margin-bottom: 10px;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(69, 128, 81, 0.14);
    color: #2e6b3d;
    font-weight: 800;
  }

  .fun-hero h1 {
    margin: 0;
    color: #16351f;
    font-size: clamp(30px, 5vw, 54px);
    line-height: 1.04;
  }

  .fun-hero p {
    margin: 14px 0 0;
    color: #49624d;
    font-size: 1.05rem;
    line-height: 1.65;
  }

  .fun-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 18px;
  }

  .fun-card {
    grid-column: span 12;
    min-height: 250px;
    padding: 22px;
    border: 1px solid rgba(69, 128, 81, 0.18);
    border-radius: 14px;
    background: #fffcfc;
    color: #1b2a1b;
    text-decoration: none;
    box-shadow: 0 12px 26px rgba(31, 59, 31, 0.12);
    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease,
      border-color 0.2s ease;
  }

  @media (min-width: 640px) {
    .fun-card {
      grid-column: span 6;
    }
  }

  @media (min-width: 992px) {
    .fun-card {
      grid-column: span 3;
    }
  }

  .fun-card:hover {
    transform: translateY(-5px);
    border-color: rgba(46, 125, 50, 0.42);
    box-shadow: 0 18px 34px rgba(31, 59, 31, 0.18);
  }

  .fun-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 54px;
    height: 54px;
    margin-bottom: 16px;
    border-radius: 16px;
    background: linear-gradient(135deg, #458051, #81c784);
    color: #fff;
  }

  .fun-icon :deep(svg) {
    width: 28px;
    height: 28px;
    fill: currentColor;
  }

  .fun-count {
    display: block;
    width: fit-content;
    margin-bottom: 12px;
    padding: 5px 10px;
    border-radius: 999px;
    background: rgba(129, 199, 132, 0.2);
    color: #2e6b3d;
    font-size: 0.85rem;
    font-weight: 800;
  }

  .fun-card h2 {
    margin: 0 0 10px;
    color: #16351f;
    font-size: 1.28rem;
  }

  .fun-card p {
    margin: 0;
    min-height: 76px;
    color: #536a56;
    line-height: 1.55;
  }

  .fun-action {
    display: inline-flex;
    margin-top: 18px;
    color: #2e7d32;
    font-weight: 900;
  }
</style>
