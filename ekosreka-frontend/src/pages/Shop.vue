<template>
  <section class="shop-page">
    <div class="shop-shell">
      <header class="shop-hero">
        <div class="hero-copy">
          <p class="eyebrow">Еко продавница</p>
          <h1>Наскоро доаѓа</h1>
          <p>
            Подготвуваме внимателно избрани еко производи за дом, училиште и активности надвор.
            Продавницата сè уште не е отворена, но идејата расте.
          </p>
          <div class="hero-actions">
            <RouterLink class="btn hero-button" to="/news">Прочитај еко совети</RouterLink>
            <RouterLink class="btn btn-outline-light" to="/quizzes">Реши квиз</RouterLink>
          </div>
        </div>

        <div class="soon-card">
          <span>Статус</span>
          <strong>Coming soon</strong>
          <p>Онлајн купување ќе биде достапно кога ќе ја завршиме селекцијата на производи.</p>
        </div>
      </header>

      <div class="preview-grid">
        <article class="preview-card">
          <span>01</span>
          <h2>Производи за повеќекратна употреба</h2>
          <p>Торби, шишиња и мали секојдневни замени што го намалуваат отпадот.</p>
        </article>

        <article class="preview-card">
          <span>02</span>
          <h2>Еко сетови за учење</h2>
          <p>Материјали и предизвици за ученици, семејства и клубови.</p>
        </article>

        <article class="preview-card">
          <span>03</span>
          <h2>Практични идеи за дома</h2>
          <p>Едноставни алатки за поорганизирана, почиста и позелена рутина.</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { RouterLink } from 'vue-router';
</script>

<style scoped>
  .shop-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .shop-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .shop-hero {
    align-items: end;
    background:
      linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.82)),
      url('../img/guide.svg') right 2rem center / 180px no-repeat;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 280px;
    margin-bottom: 1.5rem;
    min-height: 260px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .hero-copy {
    max-width: 680px;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 900;
    letter-spacing: 0.08em;
    margin: 0 0 0.75rem;
    text-transform: uppercase;
  }

  .shop-hero h1 {
    font-size: clamp(2.2rem, 6vw, 4.6rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
  }

  .shop-hero p {
    font-size: 1rem;
    line-height: 1.7;
    margin: 0;
    opacity: 0.92;
  }

  .hero-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-top: 1.25rem;
  }

  .hero-button {
    background: #fff;
    border: none;
    color: #24552d;
    font-weight: 900;
  }

  .hero-actions .btn {
    min-height: 44px;
   }

  .soon-card {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.28);
    border-radius: 8px;
    display: grid;
    gap: 0.65rem;
    padding: 1rem;
  }

  .soon-card span,
  .preview-card span {
    color: rgba(255, 255, 255, 0.76);
    font-size: 0.76rem;
    font-weight: 900;
    text-transform: uppercase;
  }

  .soon-card strong {
    font-size: 1.7rem;
    font-weight: 900;
    line-height: 1;
  }

  .preview-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .preview-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    display: grid;
    gap: 0.65rem;
    padding: 1.5rem;
  }

  .preview-card span {
    color: #2e7d32;
  }

  .preview-card h2 {
    color: #1b2a1b;
    font-size: 1.2rem;
    font-weight: 900;
    margin: 0;
  }

  .preview-card p {
    color: #506650;
    line-height: 1.6;
    margin: 0;
  }

  @media (max-width: 991px) {
    .shop-hero,
    .preview-grid {
      grid-template-columns: 1fr;
    }

    .shop-hero {
      background:
        linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.84)),
        url('../img/guide.svg') right 1rem bottom 1rem / 120px no-repeat;
    }
  }
</style>
