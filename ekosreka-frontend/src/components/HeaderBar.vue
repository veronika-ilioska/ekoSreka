<template>
  <nav class="navbar navbar-expand-lg navbar-eco sticky-top">
    <div class="container">
      <RouterLink class="navbar-brand d-flex align-items-center gap-2" to="/">
        <!-- Ако сликата е во public/img, користи /img/logo4.png -->
        <img src="../img/logo4.png" alt="ЕкоСреќа" style="height: 40px; width: auto" />
      </RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#ecoNav"
        aria-controls="ecoNav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="ecoNav">
        <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink
              class="nav-link"
              :class="{ active: route.path.startsWith('/news') }"
              to="/news"
              >Вести</RouterLink
            >
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link"
              :class="{ active: route.path.startsWith('/fun') }"
              to="/fun"
              >Забава</RouterLink
            >
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link"
              :class="{ active: route.path.startsWith('/about') }"
              to="/about"
              >За нас</RouterLink
            >
          </li>
        </ul>

        <!-- Auth Section -->
        <div class="ms-lg-3 mt-2 mt-lg-0 d-flex gap-2 align-items-center flex-wrap">
          <template v-if="authStore.isAuthenticated">
            <!-- User Dropdown -->
            <div class="dropdown">
              <button
                class="btn btn-sm eco-btn dropdown-toggle text-white px-3"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-person-circle me-1"></i>
                {{ authStore.userName }}
              </button>
              <ul class="dropdown-menu dropdown-menu-end">
                <li>
                  <RouterLink class="dropdown-item" to="/profile">
                    <i class="bi bi-person me-2"></i>Мој профил
                  </RouterLink>
                </li>
                <li>
                  <RouterLink class="dropdown-item" to="/settings">
                    <i class="bi bi-gear me-2"></i>Поставки
                  </RouterLink>
                </li>
                <li>
                  <RouterLink class="dropdown-item" to="/admin">
                    <i class="bi bi-shield-check me-2"></i>Admin
                  </RouterLink>
                </li>
                <li><hr class="dropdown-divider" /></li>
                <li>
                  <button class="dropdown-item text-danger" @click="handleLogout">
                    <i class="bi bi-box-arrow-right me-2"></i>Одјави се
                  </button>
                </li>
              </ul>
            </div>
          </template>

          <template v-else>
            <!-- Login/Signup Buttons -->
            <RouterLink
              to="/login"
              class="btn btn-sm btn-outline-light text-white px-3"
              style="border-color: currentColor"
            >
              Пријави се
            </RouterLink>
            <RouterLink to="/signup" class="btn btn-sm eco-btn text-white px-3">
              Регистрирај се
            </RouterLink>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>
<script setup>
  import { useRoute, useRouter } from 'vue-router';
  import { useAuthStore } from '../stores/authStore';

  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();

  const handleLogout = () => {
    authStore.logout();
    router.push('/');
  };
</script>

<style scoped>
  .dropdown-menu {
    border: 1px solid rgba(76, 175, 80, 0.2);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .dropdown-item {
    transition: all 0.2s ease;
  }

  .dropdown-item:hover {
    background: rgba(76, 175, 80, 0.1);
    color: var(--eco-accent) !important;
  }

  .btn-outline-light {
    border-radius: 8px;
    font-weight: 600;
  }

  .btn-outline-light:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  @media (max-width: 991px) {
    .navbar-collapse {
      margin-top: 1rem;
    }

    .d-flex {
      margin-top: 1rem;
    }
  }
</style>
