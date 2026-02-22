<template>
  <div class="news-details-container">
    <!-- Back Button -->
    <section class="eco-section">
      <div class="container">
        <RouterLink to="/news" class="btn btn-sm btn-outline-success mb-4">
          <i class="bi bi-arrow-left"></i> Назад до вести
        </RouterLink>
      </div>
    </section>

    <!-- Article Header -->
    <section v-if="article" class="article-header">
      <div class="article-cover">
        <img :src="article.coverUrl || '/cover-placeholder.jpg'" :alt="article.title" class="cover-image" />
        <div class="cover-overlay">
          <div class="container">
            <div class="header-content">
              <span v-if="article.category" class="article-category">{{ article.category }}</span>
              <h1 class="article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <span v-if="article.publishedAt" class="meta-item">
                  <i class="bi bi-calendar"></i>
                  {{ formatDate(article.publishedAt) }}
                </span>
                <span class="meta-item">
                  <i class="bi bi-clock"></i>
                  {{ estimateReadTime(article.content) }} мин. читање
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Loading State -->
    <section v-if="loading" class="eco-section">
      <div class="container text-center">
        <div class="spinner-border text-success" role="status">
          <span class="visually-hidden">Се вчитува...</span>
        </div>
      </div>
    </section>

    <!-- Article Content -->
    <section v-else-if="article" class="eco-section">
      <div class="container">
        <div class="row">
          <!-- Main Content -->
          <div class="col-12 col-lg-8">
            <article class="article-content">
              <div class="article-text">
                <p v-for="(paragraph, index) in paragraphs" :key="index" class="article-paragraph">
                  {{ paragraph }}
                </p>
              </div>

              <!-- Article Footer -->
              <div class="article-footer">
                <div class="tags-section">
                  <span v-if="article.category" class="tag tag-category">{{ article.category }}</span>
                </div>

                <div class="share-section">
                  <p class="share-label">Поделете ја оваа вест:</p>
                  <div class="share-buttons">
                    <button class="share-btn share-btn-facebook" @click="shareOn('facebook')">
                      <i class="bi bi-facebook"></i>
                    </button>
                    <button class="share-btn share-btn-twitter" @click="shareOn('twitter')">
                      <i class="bi bi-twitter"></i>
                    </button>
                    <button class="share-btn share-btn-whatsapp" @click="shareOn('whatsapp')">
                      <i class="bi bi-whatsapp"></i>
                    </button>
                    <button class="share-btn share-btn-copy" @click="copyLink">
                      <i class="bi bi-link-45deg"></i>
                    </button>
                  </div>
                </div>
              </div>
            </article>
          </div>

          <!-- Sidebar -->
          <div class="col-12 col-lg-4">
            <!-- Related Articles -->
            <div class="sidebar-card">
              <h3 class="sidebar-title">Други вести</h3>
              <div class="related-articles">
                <RouterLink
                  v-for="item in relatedArticles"
                  :key="item.id"
                  :to="`/news/${item.id}`"
                  class="related-item"
                >
                  <img :src="item.coverUrl || '/cover-placeholder.jpg'" :alt="item.title" class="related-img" />
                  <div class="related-info">
                    <h5 class="related-title">{{ item.title }}</h5>
                    <small class="related-date">{{ formatDate(item.publishedAt) }}</small>
                  </div>
                </RouterLink>
              </div>
            </div>

            <!-- Newsletter Signup -->
            <div class="sidebar-card newsletter-card">
              <h3 class="sidebar-title">Претплати се</h3>
              <p class="newsletter-text">Добивај еко вести директно во твој џебдан.</p>
              <form @submit.prevent="subscribeNewsletter" class="newsletter-form">
                <input
                  v-model="email"
                  type="email"
                  class="form-control"
                  placeholder="твоја е-пошта"
                  required
                />
                <button type="submit" class="btn btn-success w-100 mt-2">Претплати се</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Error State -->
    <section v-else class="eco-section">
      <div class="container text-center">
        <div class="alert alert-warning">Веста не е пронајдена.</div>
        <RouterLink to="/news" class="btn btn-success">Назад до вести</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api';

const route = useRoute();
const router = useRouter();

const article = ref(null);
const loading = ref(true);
const email = ref('');
const allArticles = ref([]);

// Fetch article details
onMounted(async () => {
  try {
    loading.value = true;
    const { data } = await api.get(`/news/${route.params.id}`);
    article.value = data;

    // Fetch all articles for related articles
    const { data: newsData } = await api.get('/news', { params: { page: 0, size: 50 } });
    allArticles.value = newsData.content || [];
  } catch (error) {
    console.error('Error fetching article:', error);
    article.value = null;
  } finally {
    loading.value = false;
  }
});

// Computed properties
const paragraphs = computed(() => {
  if (!article.value?.content) return [];
  return article.value.content.split('\n\n').filter(p => p.trim());
});

const relatedArticles = computed(() => {
  return allArticles.value
    .filter(item => item.id !== article.value?.id && item.category === article.value?.category)
    .slice(0, 5);
});

// Helper functions
function formatDate(dateString) {
  if (!dateString) return '';
  try {
    return new Date(dateString).toLocaleDateString('mk-MK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch {
    return dateString;
  }
}

function estimateReadTime(content) {
  if (!content) return 0;
  const wordsPerMinute = 200;
  const wordCount = content.split(/\s+/).length;
  return Math.ceil(wordCount / wordsPerMinute);
}

function shareOn(platform) {
  const url = window.location.href;
  const title = article.value?.title || 'Погледај ја оваа вест';

  let shareUrl = '';
  switch (platform) {
    case 'facebook':
      shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
      break;
    case 'twitter':
      shareUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`;
      break;
    case 'whatsapp':
      shareUrl = `https://wa.me/?text=${encodeURIComponent(title + ' ' + url)}`;
      break;
  }

  if (shareUrl) {
    window.open(shareUrl, '_blank', 'width=600,height=400');
  }
}

function copyLink() {
  navigator.clipboard.writeText(window.location.href).then(() => {
    alert('Линкот е копиран!');
  });
}

async function subscribeNewsletter() {
  // This would connect to a backend newsletter service
  alert(`Благодарам за претплатата на ${email.value}!`);
  email.value = '';
}
</script>

<style scoped>
/* ============ CONTAINER ============ */
.news-details-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f8f2 0%, #f9fbf6 100%);
}

/* ============ ARTICLE HEADER ============ */
.article-header {
  padding: 0;
  margin: 0;
}

.article-cover {
  position: relative;
  height: 400px;
  overflow: hidden;
  background: linear-gradient(135deg, #5a8d3a 0%, #7aa84a 100%);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.7);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.5));
  display: flex;
  align-items: flex-end;
  padding: 3rem 0;
}

.header-content {
  width: 100%;
  color: white;
}

.article-category {
  display: inline-block;
  background: rgba(122, 168, 74, 0.9);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 1rem;
}

.article-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 1rem 0;
  line-height: 1.2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.article-meta {
  display: flex;
  gap: 2rem;
  margin-top: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  opacity: 0.95;
}

.meta-item i {
  font-size: 1.1rem;
}

/* ============ ARTICLE CONTENT ============ */
.article-content {
  background: white;
  border-radius: 16px;
  padding: 3rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.article-text {
  margin-bottom: 3rem;
}

.article-paragraph {
  font-size: 1.1rem;
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  text-align: justify;
}

.article-paragraph:last-child {
  margin-bottom: 0;
}

/* ============ ARTICLE FOOTER ============ */
.article-footer {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 2px solid #f0f0f0;
}

.tags-section {
  margin-bottom: 2rem;
}

.tag {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #f5f8f2 0%, #f9fbf6 100%);
  color: #5a8d3a;
  border: 2px solid #7aa84a;
}

.share-section {
  text-align: center;
}

.share-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.share-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.share-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.share-btn-facebook {
  background: #1877f2;
}

.share-btn-facebook:hover {
  background: #0a66c2;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(24, 119, 242, 0.4);
}

.share-btn-twitter {
  background: #1da1f2;
}

.share-btn-twitter:hover {
  background: #1a8cd8;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(29, 161, 242, 0.4);
}

.share-btn-whatsapp {
  background: #25d366;
}

.share-btn-whatsapp:hover {
  background: #20ba5c;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(37, 211, 102, 0.4);
}

.share-btn-copy {
  background: #7aa84a;
}

.share-btn-copy:hover {
  background: #5a8d3a;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(122, 168, 74, 0.4);
}

/* ============ SIDEBAR ============ */
.sidebar-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
}

.sidebar-title {
  font-size: 1.3rem;
  font-weight: 800;
  color: #5a8d3a;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 3px solid #7aa84a;
}

.related-articles {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.related-item {
  display: flex;
  gap: 1rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  padding: 0.75rem;
  border-radius: 8px;
}

.related-item:hover {
  background: #f9fbf6;
  transform: translateX(5px);
}

.related-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.related-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.3;
  margin: 0;
}

.related-date {
  color: #7f8c8d;
  font-size: 0.8rem;
}

/* ============ NEWSLETTER CARD ============ */
.newsletter-card {
  background: linear-gradient(135deg, #f5f8f2 0%, #f9fbf6 100%);
  border: 2px solid #7aa84a;
}

.newsletter-text {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.newsletter-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.newsletter-form .form-control {
  border-radius: 8px;
  border: 2px solid #ddd;
  padding: 0.75rem;
}

.newsletter-form .form-control:focus {
  border-color: #7aa84a;
  box-shadow: 0 0 0 0.2rem rgba(122, 168, 74, 0.15);
}

/* ============ RESPONSIVE ============ */
@media (max-width: 992px) {
  .article-cover {
    height: 300px;
  }

  .article-title {
    font-size: 2rem;
  }

  .article-content {
    padding: 2rem;
  }
}

@media (max-width: 768px) {
  .article-cover {
    height: 250px;
  }

  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    gap: 1.5rem;
    flex-wrap: wrap;
  }

  .article-content {
    padding: 1.5rem;
  }

  .article-paragraph {
    font-size: 1rem;
    text-align: left;
  }

  .sidebar-card {
    padding: 1.5rem;
  }

  .sidebar-title {
    font-size: 1.1rem;
  }

  .share-buttons {
    gap: 0.75rem;
  }

  .share-btn {
    width: 45px;
    height: 45px;
    font-size: 1rem;
  }
}

@media (max-width: 576px) {
  .article-cover {
    height: 200px;
  }

  .article-title {
    font-size: 1.25rem;
  }

  .header-content {
    padding: 1rem;
  }

  .article-content {
    padding: 1rem;
  }

  .article-paragraph {
    font-size: 0.95rem;
  }

  .share-buttons {
    justify-content: center;
  }
}
</style>
