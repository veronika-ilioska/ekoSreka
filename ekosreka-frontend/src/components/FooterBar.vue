<template>
  <footer class="footer-eco mt-5 pt-5 pb-4" id="contact">
    <div class="container">
      <div class="row g-4">
        <!-- Kontakt-->
        <div class="col-md-6">
          <h5 class="mb-3">Контакт</h5>
          <p class="mb-1">ЕкоСреќа • Скопје, Македонија</p>
          <p class="mb-1 nounder">
            Е-пошта: <a href="mailto:ekosrekja@gmail.com" class="nounder">ekosrekja@gmail.com</a>
          </p>
          <p class="mb-0 nounder">
            Телефон: <a href="tel:+38970123456" class="nounder">+389 70 123 456</a>
          </p>

          <!-- socijalni mrezi -->
          <div class="d-flex align-items-center gap-3 mt-3">
            <a
              href="https://www.instagram.com/ekosrekja"
              target="_blank"
              rel="noopener"
              aria-label="Instagram"
              class="text-decoration-none"
            >
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect
                  x="2"
                  y="2"
                  width="20"
                  height="20"
                  rx="5"
                  stroke="currentColor"
                  stroke-width="1.5"
                />
                <circle cx="12" cy="12" r="4.5" stroke="currentColor" stroke-width="1.5" />
                <circle cx="17.5" cy="6.5" r="1.25" fill="currentColor" />
              </svg>
              <span class="ms-1">Instagram</span>
            </a>

            <a
              href="https://www.facebook.com/ekosrekja"
              target="_blank"
              rel="noopener"
              aria-label="Facebook"
              class="text-decoration-none"
            >
              <svg
                width="28"
                height="28"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  d="M22 12.06C22 6.49 17.52 2 12 2S2 6.49 2 12.06c0 5.02 3.66 9.18 8.44 9.94v-7.03H8.08v-2.91h2.36V9.41c0-2.33 1.39-3.62 3.52-3.62.99 0 2.03.18 2.03.18v2.24h-1.14c-1.12 0-1.47.7-1.47 1.42v1.71h2.51l-.4 2.91h-2.11v7.03C18.34 21.24 22 17.08 22 12.06Z"
                />
              </svg>
              <span class="ms-1">Facebook</span>
            </a>
          </div>
        </div>

        <!-- poraka-->
        <div class="col-md-6">
          <h5 class="mb-3">Пратете ни порака!</h5>
          <form class="row gy-2" @submit.prevent>
            <div class="col-12 col-md-6">
              <input type="text" class="form-control" placeholder="Вашето име" required />
            </div>
            <div class="col-12 col-md-6">
              <input type="email" class="form-control" placeholder=" Вашата е-пошта" required />
            </div>
            <div class="col-12">
              <textarea rows="3" class="form-control" placeholder="Порака"></textarea>
            </div>
            <div class="col-12">
              <button class="btn eco-btn text-white px-4" type="submit">Испрати</button>
            </div>
          </form>
        </div>
      </div>

      <hr class="border-light border-opacity-25 my-4" />

      <div class="d-flex justify-content-between flex-wrap gap-2">
        <small>© {{ year }} ЕкоСреќа</small>
        <small>Направено со љубов кон природата ♻️</small>
      </div>
    </div>
  </footer>
</template>
<script setup>
  const year = new Date().getFullYear();
</script>
