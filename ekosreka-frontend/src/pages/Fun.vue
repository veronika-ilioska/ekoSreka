<template>
  <section class="eco-section eco-fun">
    <div class="container">
      <div class="fun-grid">
        <!-- Фотографии -->
        <RouterLink to="/photos" class="fun-card" aria-label="Отвори фотографии">
          <div class="fun-card__bg fun-card__bg--photos" />
          <div class="fun-card__content">
            <span class="fun-icon" aria-hidden="true" v-html="icons.camera"></span>
            <h3 class="fun-title">Фотографии</h3>
            <p class="fun-sub">Приказни во слики од природата</p>
            <div class="fun-meta">
              <span class="badge">{{ counts.photos }} записи</span>
              <span class="cta">Отвори</span>
            </div>
          </div>
        </RouterLink>

        <!-- Видеа -->
        <RouterLink to="/videos" class="fun-card" aria-label="Отвори видеа">
          <div class="fun-card__bg fun-card__bg--videos" />
          <div class="fun-card__content">
            <span class="fun-icon" aria-hidden="true" v-html="icons.video"></span>
            <h3 class="fun-title">Видеа</h3>
            <p class="fun-sub">Кратки клипови и документарци</p>
            <div class="fun-meta">
              <span class="badge">{{ counts.videos }} записи</span>
              <span class="cta">Отвори</span>
            </div>
          </div>
        </RouterLink>

        <!-- Хороскоп -->
        <RouterLink to="/horoscope" class="fun-card" aria-label="Отвори хороскоп">
          <div class="fun-card__bg fun-card__bg--horoscope" />
          <div class="fun-card__content">
            <span class="fun-icon" aria-hidden="true" v-html="icons.zodiac"></span>
            <h3 class="fun-title">Хороскоп</h3>
            <p class="fun-sub">Еко совети според знакот</p>
            <div class="fun-meta">
              <span class="badge">{{ counts.horoscope }} записи</span>
              <span class="cta">Отвори</span>
            </div>
          </div>
        </RouterLink>

        <!-- Забавни игри дома -->
        <RouterLink to="/games" class="fun-card" aria-label="Отвори игри">
          <div class="fun-card__bg fun-card__bg--games" />
          <div class="fun-card__content">
            <span class="fun-icon" aria-hidden="true" v-html="icons.gamepad"></span>
            <h3 class="fun-title">Забавни игри за дома</h3>
            <p class="fun-sub">Мини-игри и предизвици за секого</p>
            <div class="fun-meta">
              <span class="badge">{{ counts.games }} записи</span>
              <span class="cta">Отвори</span>
            </div>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, reactive } from 'vue';

  const ENDPOINTS = {
    photos: 'api/media/photos/count',
    videos: 'api/media/videos/count',
    horoscope: 'api/horoscope/count',
    games: 'api/games/count',
  };

  const counts = reactive({ photos: 0, videos: 0, horoscope: 0, games: 0 });

  async function fetchCount(key) {
    try {
      const res = await fetch(ENDPOINTS[key], { headers: { Accept: 'application/json' } });
      if (!res.ok) {
        // види точно зошто е 400
        const t = await res.text();
        console.error(key, res.status, t);
        throw new Error(`HTTP ${res.status}`);
      }
      const data = await res.json();
      const n = typeof data === 'number' ? data : Number(data?.count ?? data?.total ?? 0);
      counts[key] = Number.isFinite(n) ? n : 0;
    } catch (e) {
      counts[key] = 0;
    }
  }

  onMounted(() => Promise.all(Object.keys(ENDPOINTS).map(fetchCount)));
  // Минимални SVG икони
  const icons = {
    camera: `
    <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
      <path d="M9.5 4h5l1 2H20a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l1.5-2z"/>
      <circle cx="12" cy="13" r="4" fill="white"/><circle cx="12" cy="13" r="2.5"/>
    </svg>`,
    video: `
    <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
      <path d="M3 6h12a2 2 0 0 1 2 2v1.5l4-2.5v10l-4-2.5V16a2 2 0 0 1-2 2H3z"/>
    </svg>`,
    zodiac: `
    <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 3v18M3 12h18" stroke="white" stroke-width="1.5"/>
    </svg>`,
    gamepad: `
    <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
      <path d="M6 9h12a4 4 0 1 1-2.8 6.8L14 15H10l-1.2.8A4 4 0 1 1 6 9z"/>
      <path d="M8 12h2M9 11v2M16 11.5h1M18 13.5h1" stroke="white" stroke-width="1.5" />
    </svg>`,
  };
</script>

<style scoped>
  .eco-section {
    padding: clamp(28px, 3.5vw, 48px) 0;
  }

  .eco-section-title {
    font-size: clamp(24px, 3.2vw, 36px);
    margin-bottom: 8px;
    line-height: 1.2;
  }

  .eco-muted {
    color: var(--eco-muted, #6b7280);
    margin-bottom: 24px;
  }

  .container {
    max-width: 1120px;
    margin: 0 auto;
    padding: 0 16px;
  }

  .fun-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 18px;
  }

  .fun-card {
    grid-column: span 12;
    position: relative;
    display: block;
    overflow: hidden;
    border-radius: 18px;
    min-height: 170px;
    color: #fff;
    text-decoration: none;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
    transition:
      transform 0.25s ease,
      box-shadow 0.25s ease;
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
    transform: translateY(-4px);
    box-shadow: 0 14px 28px rgba(0, 0, 0, 0.22);
  }

  .fun-card__bg {
    position: absolute;
    inset: 0;
    background-size: cover;
    background-position: center;
    filter: brightness(0.85);
    z-index: 0;
  }

  .fun-card__bg--photos {
    background-image: linear-gradient(135deg, #0ea5e9, #22c55e);
  }
  .fun-card__bg--videos {
    background-image: linear-gradient(135deg, #8b5cf6, #ef4444);
  }
  .fun-card__bg--horoscope {
    background-image: linear-gradient(135deg, #06b6d4, #3b82f6);
  }
  .fun-card__bg--games {
    background-image: linear-gradient(135deg, #f59e0b, #84cc16);
  }

  .fun-card__content {
    position: relative;
    z-index: 1;
    display: grid;
    gap: 6px;
    padding: 18px;
  }

  .fun-icon {
    display: inline-flex;
    padding: 10px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.18);
    backdrop-filter: blur(4px);
    line-height: 0;
    width: 44px;
    height: 44px;
  }

  .fun-title {
    margin: 6px 0 0;
    font-weight: 700;
  }

  .fun-sub {
    margin: 0;
    opacity: 0.95;
    font-size: 0.95rem;
  }

  .fun-meta {
    margin-top: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .badge {
    font-size: 0.86rem;
    padding: 6px 10px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.16);
  }

  .cta {
    font-weight: 700;
    text-transform: uppercase;
    font-size: 0.8rem;
    letter-spacing: 0.6px;
  }
</style>
