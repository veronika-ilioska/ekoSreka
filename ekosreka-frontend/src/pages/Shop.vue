<template>
  <section class="shop-page">
    <div class="shop-shell">
      <header class="shop-hero">
        <div>
          <p class="eyebrow">Еко продавница</p>
          <h1>Корисни замени за почиста секојдневна рутина</h1>
          <p>
            Разгледај едноставни производи за повеќекратна употреба дома, на училиште и надвор.
            Страницата е подготвена за вистинска наплата кога ќе пристигне API за производи.
          </p>
        </div>
        <RouterLink class="btn hero-button" to="/news">Прочитај еко совети</RouterLink>
      </header>

      <div class="shop-toolbar">
        <div class="category-tabs" aria-label="Категории на производи">
          <button
            v-for="category in categories"
            :key="category.value"
            type="button"
            :class="{ active: activeCategory === category.value }"
            @click="activeCategory = category.value"
          >
            {{ category.label }}
          </button>
        </div>
        <span>{{ filteredProducts.length }} производи</span>
      </div>

      <div class="product-grid">
        <article v-for="product in filteredProducts" :key="product.id" class="product-card">
          <img :src="product.image" :alt="product.name" />
          <div class="product-body">
            <div class="product-meta">
              <span>{{ categoryLabel(product.category) }}</span>
              <strong>{{ product.price }}</strong>
            </div>
            <h2>{{ product.name }}</h2>
            <p>{{ product.description }}</p>
            <button class="btn eco-btn" type="button">Додај во кошничка</button>
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

  const categories = [
    { value: 'All', label: 'Сите' },
    { value: 'Kitchen', label: 'Кујна' },
    { value: 'Home', label: 'Дом' },
    { value: 'Outdoor', label: 'Надвор' },
    { value: 'Learning', label: 'Учење' },
  ];
  const activeCategory = ref('All');
  const products = [
    {
      id: 1,
      name: 'Торба за повеќекратна употреба',
      category: 'Kitchen',
      price: '$8',
      image: bagImage,
      description: 'Цврста секојдневна торба за намирници, книги и кратки обврски.',
    },
    {
      id: 2,
      name: 'Почетен сет за компост',
      category: 'Home',
      price: '$18',
      image: beforeImage,
      description: 'Сет погоден за почетници за собирање остатоци од храна и градинарски отпад.',
    },
    {
      id: 3,
      name: 'Ракавици за чистење',
      category: 'Outdoor',
      price: '$12',
      image: afterImage,
      description: 'Ракавици за повеќекратна употреба за локални чистења и активности надвор.',
    },
    {
      id: 4,
      name: 'Еко картички со предизвици',
      category: 'Learning',
      price: '$10',
      image: gameImage,
      description: 'Мали неделни предизвици за семејства, училници и клубови.',
    },
  ];
  const filteredProducts = computed(() =>
    activeCategory.value === 'All' ? products : products.filter((product) => product.category === activeCategory.value)
  );

  function categoryLabel(value) {
    return categories.find((category) => category.value === value)?.label || value;
  }
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
    background: linear-gradient(135deg, rgba(69, 128, 81, 0.94), rgba(102, 187, 106, 0.84)), url('../img/bag.png') center / cover;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) auto;
    margin-bottom: 1.25rem;
    min-height: 300px;
    padding: 2rem;
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
    color: #fff;
  }
  .product-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .product-card {
    background: var(--eco-card-bg);
    border-radius: 8px;
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
    display: flex;
    font-weight: 800;
    justify-content: space-between;
    text-transform: uppercase;
  }
  @media (max-width: 991px) {
    .shop-hero,
    .product-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
