<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useTravelStore } from "../stores/travel";
const store = useTravelStore();
const open = ref(false);
const type = ref("place");
const editId = ref(null);
const error = ref("");
const form = ref({
  name: "",
  country: "",
  city: "",
  address: "",
  date: "",
  lat: 35.0116,
  lng: 135.7681,
  guide: "",
  status: "published",
});
function start(e) {
  type.value = e.detail.type;
  editId.value = e.detail.id || null;
  form.value = { ...form.value, ...(e.detail.place || {}) };
  open.value = true;
}
async function submit() {
  try {
    const token =
      store.csrf ||
      (await (await fetch("/api/auth/csrf", { credentials: "include" })).json())
        .token;
    const p = form.value;
    const payload =
      type.value === "food"
        ? {
            name: p.name,
            address: p.address || p.city,
            longitude: +p.lng,
            latitude: +p.lat,
            status: p.status,
            notes: p.guide,
          }
        : {
            name: p.name,
            country: p.country,
            city: p.city,
            address: p.address,
            longitude: +p.lng,
            latitude: +p.lat,
            visitDate: p.date,
            status: p.status,
            guideContent: p.guide,
          };
    const r = await fetch(
      `/api/${type.value === "food" ? "foods" : "places"}${editId.value ? `/${editId.value}` : ""}`,
      {
        method: editId.value ? "PUT" : "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": token },
        body: JSON.stringify(payload),
      },
    );
    if (!r.ok) throw new Error("保存失败");
    open.value = false;
    window.loadRemotePlaces?.();
  } catch (e) {
    error.value = e.message;
  }
}
onMounted(() => window.addEventListener("travel:entry", start));
onUnmounted(() => window.removeEventListener("travel:entry", start));
</script>
<template>
  <Teleport to="body"
    ><div v-if="open" class="modal-backdrop" @click.self="open = false">
      <section class="modal">
        <button class="modal-close" @click="open = false">×</button>
        <h2>{{ editId ? "编辑记录" : "新增记录" }}</h2>
        <form @submit.prevent="submit">
          <label>名称<input v-model="form.name" required /></label
          ><label>城市<input v-model="form.city" /></label
          ><label>地址<input v-model="form.address" /></label>
          <div class="form-row">
            <label>日期<input v-model="form.date" type="date" required /></label
            ><label
              >状态<select v-model="form.status">
                <option value="published">已发布</option>
                <option value="draft">草稿</option>
                <option value="archived">已归档</option>
              </select></label
            >
          </div>
          <label>攻略<textarea v-model="form.guide"></textarea></label>
          <div class="auth-error">{{ error }}</div>
          <div class="modal-footer">
            <button type="button" class="text-btn" @click="open = false">
              取消</button
            ><button class="create-btn">保存</button>
          </div>
        </form>
      </section>
    </div></Teleport
  >
</template>
