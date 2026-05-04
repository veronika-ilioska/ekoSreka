<template>
  <section class="news-page">
    <div class="news-shell">
      <header class="news-hero">
        <div>
          <p class="eyebrow">Еко вести</p>
          <h1>Приказни, идеи за позелени секојдневни одлуки</h1>
          <p>
            Follow practical environmental news across recycling, energy, nature, and climate.
          </p>
        </div>
        <div class="hero-stat">
          <strong>{{ total }}</strong>
          <span>{{ categoryLabel }} приказни</span>
        </div>
      </header>

      <section class="news-toolbar">
        <div class="category-tabs" aria-label="News categories">
          <button
            v-for="cat in categories"
            :key="cat.value || 'all'"
            type="button"
            :class="{ active: category === cat.value }"
            @click="setCategory(cat.value)"
          >
            {{ cat.label }}
          </button>
        </div>
      </section>

      <div v-if="loading" class="state-card">Се вчитуваат вестите...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>
      <div v-else-if="items.length === 0" class="state-card">Нема вести за оваа категорија.</div>

      <div v-else class="news-grid">
        <NewsCard v-for="item in items" :key="item.id" :item="item" />
      </div>

      <footer class="pager" v-if="total > size">
        <button class="btn btn-outline-success" :disabled="page === 0 || loading" @click="goPage(page - 1)">
          Предходно
        </button>
        <span>Страна {{ page + 1 }} од {{ totalPages }}</span>
        <button
          class="btn btn-outline-success"
          :disabled="page >= totalPages - 1 || loading"
          @click="goPage(page + 1)"
        >
          Следно
        </button>
      </footer>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { api } from '../api';
  import NewsCard from '../components/NewsCard.vue';

  const route = useRoute();
  const router = useRouter();

  const categories = [
    { value: undefined, label: 'All' },
    { value: 'recycling', label: 'Recycling' },
    { value: 'energy', label: 'Energy' },
    { value: 'nature', label: 'Nature' },
    { value: 'climate', label: 'Climate' },
  ];

  const items = ref([]);
  const loading = ref(false);
  const loadError = ref('');
  const total = ref(0);
  const page = ref(0);
  const size = ref(12);
  const category = ref(route.query.category ? String(route.query.category) : undefined);

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
  const categoryLabel = computed(() => {
    return categories.find((item) => item.value === category.value)?.label || 'All';
  });

  async function load() {
    loading.value = true;
    loadError.value = '';
    const params = { page: page.value, size: size.value };
    if (category.value) params.category = category.value;

    try {
      const { data } = await api.get('/news', { params });
      items.value = data?.content ?? [];
      total.value = data?.totalElements ?? 0;
    } catch (error) {
      loadError.value = 'Could not load news.';
      items.value = [];
      total.value = 0;
    } finally {
      loading.value = false;
    }
  }

  function setCategory(cat) {
    category.value = cat;
    page.value = 0;
    router.replace({ query: cat ? { category: cat } : {} });
    load();
  }

  function goPage(nextPage) {
    page.value = Math.max(0, nextPage);
    load();
  }

  watch(
    () => route.query.category,
    (q) => {
      category.value = q ? String(q) : undefined;
      page.value = 0;
      load();
    }
  );

  onMounted(load);
</script>

<style scoped>
  .news-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .news-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .news-hero {
    align-items: end;
    background:
      linear-gradient(135deg, rgba(36, 77, 43, 0.94), rgba(69, 128, 81, 0.84)),
      url('../img/before1.jpg') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 220px;
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

  .news-hero h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.02;
    margin: 0 0 1rem;
    max-width: 780px;
  }

  .news-hero p {
    font-size: 1.04rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.94;
  }

  .hero-stat {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.3);
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
    font-size: 0.78rem;
    font-weight: 800;
    margin-top: 0.35rem;
    text-transform: uppercase;
  }

  .news-toolbar {
    margin: 1.25rem 0;
  }

  .category-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .category-tabs button {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.25);
    border-radius: 999px;
    color: #2e7d32;
    font-weight: 800;
    padding: 0.65rem 1rem;
  }

  .category-tabs button.active {
    background: #458051;
    border-color: #458051;
    color: #fff;
  }

  .news-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    color: #506650;
    padding: 1.5rem;
    text-align: center;
  }

  .pager {
    align-items: center;
    display: flex;
    gap: 0.75rem;
    justify-content: center;
    margin-top: 1.5rem;
  }

  .pager span {
    color: #506650;
    font-weight: 800;
  }

  @media (max-width: 991px) {
    .news-hero {
      grid-template-columns: 1fr;
    }

    .hero-stat {
      max-width: 240px;
    }

    .news-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    .news-hero,
    .news-grid {
      grid-template-columns: 1fr;
    }

    .pager {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
