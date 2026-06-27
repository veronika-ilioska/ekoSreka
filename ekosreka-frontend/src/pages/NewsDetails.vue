<template>
  <section class="news-details">
    <RouterLink class="back-link" to="/news">
      <i class="bi bi-arrow-left me-2"></i>
      Назад кон вести
    </RouterLink>

    <div v-if="loading" class="text-center py-5">Веста се вчитува...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <article v-else-if="article" class="article-shell">
      <img v-if="article.coverUrl" :src="article.coverUrl" class="article-cover" :alt="article.title" />
      <div class="article-body">
        <div class="article-meta">
          <span v-if="article.category" class="category-badge">{{ article.category }}</span>
          <time :datetime="article.createdAt">{{ createdDate }}</time>
        </div>
        <h1>{{ article.title }}</h1>
        <div class="content">
          <p v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import  api  from '../api';

  const route = useRoute();
  const article = ref(null);
  const loading = ref(false);
  const error = ref('');
  const createdDate = computed(() => {
    if (!article.value?.createdAt) return 'Непознат датум';
    return new Date(article.value.createdAt).toLocaleDateString('mk-MK', { year: 'numeric', month: 'long', day: 'numeric' });
  });
  const paragraphs = computed(() =>
    (article.value?.content || '')
      .split(/\n+/)
      .map((paragraph) => paragraph.trim())
      .filter(Boolean)
  );
  async function loadArticle() {
    loading.value = true;
    error.value = '';
    try {
      const { data } = await api.get(`/news/${route.params.id}`);
      article.value = data;
    } catch (err) {
      error.value = err.response?.status === 404 ? 'Веста не е пронајдена.' : 'Веста не може да се вчита.';
    } finally {
      loading.value = false;
    }
  }
  onMounted(loadArticle);
</script>

<style scoped>
  .news-details {
    color: var(--eco-text-dark);
    margin: 0 auto;
    max-width: 980px;
    padding: 1rem 0 4rem;
  }
  .back-link {
    color: #2e7d32;
    display: inline-flex;
    font-weight: 700;
    margin-bottom: 1rem;
    text-decoration: none;
  }
  .article-shell {
    background: var(--eco-card-bg);
    border-radius: 8px;
    overflow: hidden;
  }
  .article-cover {
    aspect-ratio: 16 / 7;
    object-fit: cover;
    width: 100%;
  }
  .article-body {
    padding: 2rem;
  }
  .category-badge {
    background: rgba(102, 187, 106, 0.16);
    border-radius: 999px;
    color: #2e7d32;
    font-weight: 800;
    padding: 0.35rem 0.7rem;
    text-transform: uppercase;
  }
  h1 {
    color: #1b2a1b;
    font-size: clamp(2rem, 5vw, 3.4rem);
    font-weight: 800;
  }
  .content {
    color: #263826;
    font-size: 1.08rem;
    line-height: 1.75;
  }
</style>
