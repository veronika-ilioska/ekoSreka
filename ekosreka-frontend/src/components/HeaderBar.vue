<template>
  <nav class="navbar navbar-expand-lg navbar-eco sticky-top">
    <div class="container">
     
      <RouterLink class="navbar-brand d-flex align-items-center gap-2" to="/">
        <!-- Ако сликата е во public/img, користи /img/logo4.png -->
        <img src="../img/logo4.png" alt="ЕкоСреќа" style="height:40px;width:auto;" />
        
      </RouterLink>

      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ecoNav"
              aria-controls="ecoNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="ecoNav">
        <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{active: route.path.startsWith('/news')}" to="/news">Вести</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{active: route.path.startsWith('/fun')}" to="/fun">Забава</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{active: route.path.startsWith('/about')}" to="/about">За нас</RouterLink>
          </li>
        </ul>

        <div class="ms-lg-3 mt-2 mt-lg-0">
          <RouterLink to="/quizzes" class="btn btn-sm eco-btn text-white px-3">Започни квиз</RouterLink>
        </div>
      </div>
    </div>
  </nav>
</template>
<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
