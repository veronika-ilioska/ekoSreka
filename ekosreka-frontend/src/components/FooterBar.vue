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
          <form class="row gy-2" :aria-busy="sending" @submit.prevent="sendMessage">
            <div class="col-12 col-md-6">
              <input v-model.trim="form.name" type="text" class="form-control" placeholder="Вашето име" aria-label="Вашето име" autocomplete="name" maxlength="100" :disabled="sending" required />
            </div>
            <div class="col-12 col-md-6">
              <input v-model.trim="form.email" type="email" class="form-control" placeholder="Вашата е-пошта" aria-label="Вашата е-пошта" autocomplete="email" maxlength="254" :disabled="sending" required />
            </div>
            <div class="col-12">
              <textarea v-model.trim="form.message" rows="3" class="form-control" placeholder="Порака" aria-label="Порака" maxlength="5000" :disabled="sending" required></textarea>
            </div>
            <div class="col-12">
              <button class="btn eco-btn text-white px-4" type="submit" :disabled="sending">{{ sending ? 'Се испраќа...' : 'Испрати' }}</button>
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
  <Teleport to="body">
    <div class="contact-notification-region" aria-live="polite" aria-atomic="true">
      <Transition name="contact-toast">
        <div v-if="notification" class="contact-toast" :class="{ 'contact-toast--error': notification.error }">
          <span class="contact-toast-icon" aria-hidden="true">{{ notification.error ? '!' : '✓' }}</span>
          <span>{{ notification.message }}</span>
          <button type="button" aria-label="Затвори известување" @click="dismissNotification">×</button>
        </div>
      </Transition>
    </div>
  </Teleport>
</template>
<script setup>
  import { onBeforeUnmount, reactive, ref } from 'vue';
  import api from '../api';

  const year = new Date().getFullYear();
  const form = reactive({ name: '', email: '', message: '' });
  const sending = ref(false);
  const notification = ref(null);
  let notificationTimer;

  function dismissNotification() {
    clearTimeout(notificationTimer);
    notification.value = null;
  }
  function notify(message, error = false) {
    dismissNotification();
    notification.value = { message, error };
    if (!error) notificationTimer = setTimeout(dismissNotification, 6000);
  }
  async function sendMessage() {
    if (sending.value) return;
    if (!form.name || !form.email || !form.message) {
      notify('Пополнете ги сите полиња.', true);
      return;
    }
    sending.value = true;
    dismissNotification();
    try {
      await api.post('/contact', { ...form });
      Object.assign(form, { name: '', email: '', message: '' });
      notify('Пораката е успешно испратена! Ви благодариме.');
    } catch (error) {
      notify(error.response?.status === 503
        ? 'Испраќањето моментално не е достапно. Пишете ни на ekosrekja@gmail.com.'
        : 'Пораката не е испратена. Обидете се повторно.', true);
    } finally {
      sending.value = false;
    }
  }
  onBeforeUnmount(() => clearTimeout(notificationTimer));
</script>

<style scoped>
  .contact-notification-region { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); z-index: 3000; width: min(540px, calc(100% - 32px)); pointer-events: none; }
  .contact-toast { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 6px; background: #2e7d32; color: #fff; box-shadow: 0 4px 18px #0003; font-size: 15px; line-height: 1.5; pointer-events: auto; }
  .contact-toast--error { background: #b3261e; }
  .contact-toast-icon { display: grid; place-items: center; border: 2px solid currentColor; border-radius: 50%; width: 24px; height: 24px; flex-shrink: 0; font-weight: bold; }
  .contact-toast button { margin-left: auto; flex-shrink: 0; background: transparent; border: 0; color: inherit; font-size: 26px; min-width: 44px; min-height: 44px; border-radius: 50%; }
  .contact-toast button:hover { background: #ffffff20; }
  .contact-toast button:focus-visible { outline: 2px solid #fff; outline-offset: 2px; }
  .contact-toast-enter-active, .contact-toast-leave-active { transition: opacity .2s ease, transform .2s ease; }
  .contact-toast-enter-from, .contact-toast-leave-to { opacity: 0; transform: translateY(12px); }
  @media (prefers-reduced-motion: reduce) { .contact-toast-enter-active, .contact-toast-leave-active { transition: none; } }
</style>
