<template>
  <section class="shop-page">
    <div class="shop-shell">
      <header class="shop-hero">
        <div>
          <p class="eyebrow">Eco shop</p>
          <h1>Useful swaps for a cleaner everyday routine</h1>
          <p>
            Browse simple, reusable picks for home, school, and outdoor days. This shop page is
            ready for real checkout integration when the product API arrives.
          </p>
        </div>
        <RouterLink class="btn hero-button" to="/news">Read eco tips</RouterLink>
      </header>

      <div class="shop-toolbar">
        <div class="category-tabs" aria-label="Product categories">
          <button
            v-for="category in categories"
            :key="category"
            type="button"
            :class="{ active: activeCategory === category }"
            @click="activeCategory = category"
          >
            {{ category }}
          </button>
        </div>
        <span>{{ filteredProducts.length }} products</span>
      </div>

      <div class="product-grid">
        <article v-for="product in filteredProducts" :key="product.id" class="product-card">
          <img :src="product.image" :alt="product.name" />
          <div class="product-body">
            <div class="product-meta">
              <span>{{ product.category }}</span>
              <strong>{{ product.price }}</strong>
            </div>
            <h2>{{ product.name }}</h2>
            <p>{{ product.description }}</p>
            <button class="btn eco-btn" type="button">Add to basket</button>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { RouterLink } from 'vue-router';
  import bagImage from '../img/bag.png';
  import beforeImage from '../img/before1.jpg';
  import afterImage from '../img/after1.jpg';
  import gameImage from '../img/game.png';

  const categories = ['All', 'Kitchen', 'Home', 'Outdoor', 'Learning'];
  const activeCategory = ref('All');

  const products = [
    {
      id: 1,
      name: 'Reusable market bag',
      category: 'Kitchen',
      price: '$8',
      image: bagImage,
      description: 'A sturdy everyday bag for groceries, books, and quick errands.',
    },
    {
      id: 2,
      name: 'Compost starter kit',
      category: 'Home',
      price: '$18',
      image: beforeImage,
      description: 'A beginner-friendly set for collecting food scraps and garden waste.',
    },
    {
      id: 3,
      name: 'Cleanup gloves set',
      category: 'Outdoor',
      price: '$12',
      image: afterImage,
      description: 'Reusable gloves for neighborhood cleanups and outdoor projects.',
    },
    {
      id: 4,
      name: 'Eco challenge cards',
      category: 'Learning',
      price: '$10',
      image: gameImage,
      description: 'Small weekly challenges for families, classrooms, and clubs.',
    },
  ];

  const filteredProducts = computed(() => {
    if (activeCategory.value === 'All') return products;
    return products.filter((product) => product.category === activeCategory.value);
  });
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
      linear-gradient(135deg, rgba(69, 128, 81, 0.94), rgba(102, 187, 106, 0.84)),
      url('../img/bag.png') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) auto;
    margin-bottom: 1.25rem;
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

  .shop-hero h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.02;
    margin: 0 0 1rem;
    max-width: 760px;
  }

  .shop-hero p {
    font-size: 1.04rem;
    line-height: 1.7;
    margin: 0;
    max-width: 650px;
    opacity: 0.94;
  }

  .hero-button {
    background: rgba(255, 255, 255, 0.18);
    border: 1px solid rgba(255, 255, 255, 0.55);
    color: #fff;
    font-weight: 800;
  }

  .shop-toolbar {
    align-items: center;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    margin-bottom: 1rem;
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
    padding: 0.6rem 0.95rem;
  }

  .category-tabs button.active {
    background: #458051;
    border-color: #458051;
    color: #fff;
  }

  .shop-toolbar > span {
    color: #506650;
    font-weight: 800;
  }

  .product-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .product-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    overflow: hidden;
  }

  .product-card img {
    aspect-ratio: 4 / 3;
    object-fit: cover;
    width: 100%;
  }

  .product-body {
    display: grid;
    gap: 0.65rem;
    padding: 1rem;
  }

  .product-meta {
    align-items: center;
    color: #506650;
    display: flex;
    font-size: 0.84rem;
    font-weight: 800;
    justify-content: space-between;
    text-transform: uppercase;
  }

  .product-meta strong {
    color: #2e7d32;
  }

  .product-card h2 {
    color: #1b2a1b;
    font-size: 1.15rem;
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.18;
    margin: 0;
  }

  .product-card p {
    color: #506650;
    line-height: 1.55;
    margin: 0;
  }

  @media (max-width: 991px) {
    .shop-hero {
      grid-template-columns: 1fr;
    }

    .hero-button {
      justify-self: start;
    }

    .product-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    .shop-toolbar {
      align-items: stretch;
      flex-direction: column;
    }

    .product-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
