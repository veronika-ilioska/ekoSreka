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
          <template v-for="(block, index) in contentBlocks" :key="index">
            <figure v-if="block.type === 'image'" class="inline-article-image">
              <img :src="block.src" :alt="block.alt" loading="lazy" />
              <figcaption v-if="block.alt">{{ block.alt }}</figcaption>
            </figure>
            <p v-else>
              <span
                v-for="(segment, segmentIndex) in block.segments"
                :key="segmentIndex"
                :class="{ 'rich-bold': segment.bold, 'rich-italic': segment.italic }"
                :style="segment.color ? { color: segment.color } : null"
              >
                {{ segment.text }}
              </span>
            </p>
          </template>
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
  const contentBlocks = computed(() => parseArticleContent(article.value?.content || ''));

  function parseArticleContent(content) {
    const blocks = [];
    const imagePattern = /!\[([^\]]*)\]\(([^)\s]+)\)/g;
    let cursor = 0;
    let match;

    while ((match = imagePattern.exec(content)) !== null) {
      addTextBlocks(blocks, content.slice(cursor, match.index));
      if (isSafeImageUrl(match[2])) {
        blocks.push({ type: 'image', alt: match[1].trim(), src: match[2].trim() });
      } else {
        addTextBlocks(blocks, match[0]);
      }
      cursor = match.index + match[0].length;
    }

    addTextBlocks(blocks, content.slice(cursor));
    return blocks;
  }
  function addTextBlocks(blocks, text) {
    text
      .split(/\n+/)
      .map((paragraph) => paragraph.trim())
      .filter(Boolean)
      .forEach((paragraph) => blocks.push({ type: 'text', segments: parseInlineText(paragraph) }));
  }
  function parseInlineText(text, marks = {}) {
    const value = String(text || '');
    const colorMatch = value.match(/\[color=(#[0-9a-f]{6})\]([\s\S]*?)\[\/color\]/i);
    const boldMatch = value.match(/\*\*([\s\S]+?)\*\*/);
    const italicMatch = value.match(/_([\s\S]+?)_/);
    const matches = [
      colorMatch && { match: colorMatch, type: 'color', index: colorMatch.index },
      boldMatch && { match: boldMatch, type: 'bold', index: boldMatch.index },
      italicMatch && { match: italicMatch, type: 'italic', index: italicMatch.index },
    ].filter(Boolean).sort((left, right) => left.index - right.index);

    if (!matches.length) {
      return value ? [{ text: value, ...marks }] : [];
    }

    const next = matches[0];
    const [fullMatch, firstGroup, secondGroup] = next.match;
    const before = value.slice(0, next.index);
    const innerText = next.type === 'color' ? secondGroup : firstGroup;
    const after = value.slice(next.index + fullMatch.length);
    const nextMarks = {
      ...marks,
      bold: marks.bold || next.type === 'bold',
      italic: marks.italic || next.type === 'italic',
      color: next.type === 'color' ? normalizeTextColor(firstGroup) : marks.color,
    };

    return [
      ...parseInlineText(before, marks),
      ...parseInlineText(innerText, nextMarks),
      ...parseInlineText(after, marks),
    ];
  }
  function normalizeTextColor(color) {
    return /^#[0-9a-f]{6}$/i.test(color || '') ? color : '';
  }
  function isSafeImageUrl(url) {
    const value = String(url || '').trim();
    return /^(https?:\/\/|\/)/i.test(value);
  }
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
  .content p {
    text-align: justify;
    text-align-last: left;
    text-justify: inter-word;
  }
  .inline-article-image {
    margin: 1.5rem 0;
  }
  .inline-article-image img {
    border-radius: 8px;
    display: block;
    max-height: 560px;
    object-fit: cover;
    width: 100%;
  }
  .inline-article-image figcaption {
    color: #506650;
    font-size: 0.9rem;
    line-height: 1.5;
    margin-top: 0.5rem;
    text-align: center;
  }
  .rich-bold {
    font-weight: 800;
  }
  .rich-italic {
    font-style: italic;
  }
</style>
