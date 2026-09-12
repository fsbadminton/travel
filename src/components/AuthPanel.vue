<script setup>
import { ref } from "vue";
import { useTravelStore } from "../stores/travel";
const store = useTravelStore();
const open = ref(false);
const register = ref(false);
const username = ref("");
const password = ref("");
const error = ref("");
async function submit() {
  error.value = "";
  try {
    const r = await fetch(
      `/api/auth/${register.value ? "register" : "login"}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          username: username.value,
          password: password.value,
        }),
      },
    );
    const d = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(d.message || "请求失败");
    store.setUser(d);
    open.value = false;
    window.loadRemotePlaces?.();
  } catch (e) {
    error.value = e.message;
  }
}
</script>
<template>
  <button class="user-btn vue-auth-trigger" @click="open = true">
    {{ store.user?.username || "登录" }}</button
  ><Teleport to="body"
    ><div v-if="open" class="modal-backdrop" @click.self="open = false">
      <section class="modal auth-modal">
        <button class="modal-close" @click="open = false">×</button>
        <h2>{{ register ? "注册足迹" : "登录足迹" }}</h2>
        <form @submit.prevent="submit">
          <label
            >用户名<input v-model="username" required minlength="4" /></label
          ><label
            >密码<input
              v-model="password"
              type="password"
              required
              minlength="8"
          /></label>
          <div class="auth-error">{{ error }}</div>
          <div class="modal-footer">
            <button
              type="button"
              class="text-btn"
              @click="register = !register"
            >
              {{ register ? "去登录" : "注册新账号" }}</button
            ><button class="create-btn">
              {{ register ? "注册" : "登录" }}
            </button>
          </div>
        </form>
      </section>
    </div></Teleport
  >
</template>
