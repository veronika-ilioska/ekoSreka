<template>
  <section class="news-details">
    <RouterLink class="back-link" to="/news">
      <i class="bi bi-arrow-left me-2"></i>
      Back to news
    </RouterLink>

    <div v-if="loading" class="text-center py-5">Loading news...</div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <article v-else-if="article" class="article-shell">
      <img
        v-if="article.coverUrl"
        :src="article.coverUrl"
        class="article-cover"
        :alt="article.title"
      />

      <div class="article-body">
        <div class="article-meta">
          <span v-if="article.category" class="category-badge">{{ article.category }}</span>
          <time :datetime="article.createdAt">{{ createdDate }}</time>
        </div>

        <h1>{{ article.title }}</h1>

        <div class="content">
          <p v-for="(paragraph, index) in paragraphs" :key="index">
            {{ paragraph }}
          </p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { api } from '../api';

  const route = useRoute();
  const article = ref(null);
  const loading = ref(false);
  const error = ref('');

  const createdDate = computed(() => {
    if (!article.value?.createdAt) return 'Unknown date';

    return new Date(article.value.createdAt).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  });

  const paragraphs = computed(() => {
    return (article.value?.content || '')
      .split(/\n+/)
      .map((paragraph) => paragraph.trim())
      .filter(Boolean);
  });

  async function loadArticle() {
    loading.value = true;
    error.value = '';

    try {
      const { data } = await api.get(`/news/${route.params.id}`);
      article.value = data;
    } catch (err) {
      error.value = err.response?.status === 404 ? 'News article was not found.' : 'Could not load news.';
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
    align-items: center;
    color: #2e7d32;
    display: inline-flex;
    font-weight: 700;
    margin-bottom: 1rem;
    text-decoration: none;
  }

  .back-link:hover {
    color: #1f3b1f;
  }

  .article-shell {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.1);
    overflow: hidden;
  }

  .article-cover {
    aspect-ratio: 16 / 7;
    display: block;
    object-fit: cover;
    width: 100%;
  }

  .article-body {
    padding: 2rem;
  }

  .article-meta {
    align-items: center;
    color: #506650;
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }

  .category-badge {
    background: rgba(102, 187, 106, 0.16);
    border: 1px solid rgba(46, 125, 50, 0.22);
    border-radius: 999px;
    color: #2e7d32;
    font-size: 0.78rem;
    font-weight: 800;
    padding: 0.35rem 0.7rem;
    text-transform: uppercase;
  }

  h1 {
    color: #1b2a1b;
    font-size: clamp(2rem, 5vw, 3.4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.05;
    margin: 0 0 1.5rem;
  }

  .content {
    color: #263826;
    font-size: 1.08rem;
    line-height: 1.75;
  }

  .content p {
    margin: 0 0 1.15rem;
  }

  @media (max-width: 576px) {
    .article-body {
      padding: 1.25rem;
    }

    .article-cover {
      aspect-ratio: 16 / 10;
    }
  }
</style>
