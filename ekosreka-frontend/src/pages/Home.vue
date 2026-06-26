<template>
  <section class="eco-section eco-section--alt eco-ba">
    <div class="container">
      <div class="row align-items-center gy-4">
        <div class="col-12 col-md-5">
          <article class="eco-card ba-card">
            <div class="ba-img-wrap">
              <img src="../img/before1.jpg" class="ba-img" alt="Реалност" />
              <span class="ba-label ba-label-danger">Реалност</span>
            </div>
          </article>
        </div>

        <div class="col-2 d-none d-md-flex justify-content-center">
          <img src="../img/arrow.png" alt="" class="ba-arrow animate-arrow" />
        </div>

        <div class="col-12 col-md-5">
          <article class="eco-card ba-card">
            <div class="ba-img-wrap">
              <img src="../img/after1.jpg" class="ba-img" alt="Цел" />
              <span class="ba-label ba-label-success">Цел</span>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>

  <section class="eco-section">
    <div class="container">
      <div class="d-flex justify-content-between align-items-end mb-3">
        <h2 class="eco-title mb-0">Најнови вести</h2>
        <RouterLink to="/news" class="link-success nounder">Сите вести</RouterLink>
      </div>

      <div v-if="latest.length" class="row g-4">
        <div v-for="n in latest" :key="n.id" class="col-12 col-sm-6 col-lg-4">
          <NewsCard :item="n" />
        </div>
      </div>

      <div v-else class="row g-4">
        <div class="col-12">
          <article class="eco-card p-4 text-center text-muted">Моментално нема објави.</article>
        </div>
      </div>
    </div>
  </section>

  <section class="eco-cta-band eco-cta-band--cute text-white">
    <div class="container-fluid px-0">
      <div class="row g-0 text-center">
        <div class="col-12 col-md-4">
          <RouterLink to="/quizzes" class="cta-tile cta-tile--quiz">
            <div class="cta-inner">
              <div class="cta-icon">
                <img src="../img/quiz.png" alt="Квиз" class="cta-icon-img" />
              </div>
              <h3 class="cta-title">Квизови</h3>
              <p class="cta-subtitle">Испробај го твоето еко знаење</p>
            </div>
          </RouterLink>
        </div>
        <div class="col-12 col-md-4">
          <RouterLink to="/shop" class="cta-tile cta-tile--shop">
            <div class="cta-inner">
              <div class="cta-icon">
                <img src="../img/bag.png" alt="Продавница" class="cta-icon-img" />
              </div>
              <h3 class="cta-title">Онлајн продавница</h3>
              <p class="cta-subtitle">Еко производи за секој ден</p>
            </div>
          </RouterLink>
        </div>
        <div class="col-12 col-md-4">
          <RouterLink to="/fun" class="cta-tile cta-tile--fun">
            <div class="cta-inner">
              <div class="cta-icon">
                <img src="../img/game.png" alt="Забава" class="cta-icon-img" />
              </div>
              <h3 class="cta-title">Забава</h3>
              <p class="cta-subtitle">Позитивна еко содржина</p>
            </div>
          </RouterLink>
        </div>
      </div>
    </div>
  </section>

  <section class="eco-section">
    <div class="container">
      <div class="eco-card p-4 d-flex flex-column flex-lg-row align-items-center justify-content-between">
        <div>
          <h4 class="mb-1">Сакате соработка или имате приказна?</h4>
          <p class="text-muted mb-0">Пишете ни - заедно правиме промена.</p>
        </div>
        <a href="#contact" class="btn eco-btn mt-3 mt-lg-0 px-4">Контакт</a>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { api } from '../api';
  import NewsCard from '../components/NewsCard.vue';

  const latest = ref([]);

  onMounted(async () => {
    const { data } = await api.get('/news', { params: { page: 0, size: 6 } });
    latest.value = data.content || [];
  });
</script>

<style scoped>
  .cta-icon-img {
    height: 60px;
    object-fit: contain;
    width: 60px;
  }
</style>
