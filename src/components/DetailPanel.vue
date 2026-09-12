<script setup>
import { ref, watch } from "vue";

const props = defineProps({ place: Object, csrf: String });
const emit = defineEmits(["close"]);
const media = ref([]);
const review = ref(null);
const stars = ref("");
const content = ref("");
const loading = ref(false);
const error = ref("");
async function load() {
  if (!props.place) return;
  loading.value = true;
  error.value = "";
  try {
    const [m, r] = await Promise.all([
      fetch(`/api/media?placeId=${props.place.id}`, { credentials: "include" }),
      fetch(`/api/places/${props.place.id}/review`, { credentials: "include" }),
    ]);
    media.value = m.ok ? await m.json() : [];
    review.value = r.ok ? await r.json() : null;
    stars.value = review.value?.stars || "";
    content.value = review.value?.content || "";
  } catch {
    error.value = "加载失败";
  } finally {
    loading.value = false;
  }
}
async function saveReview() {
  if (!props.place) return;
  const r = await fetch(`/api/places/${props.place.id}/review`, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": props.csrf },
    body: JSON.stringify({
      stars: Number(stars.value) || null,
      content: content.value,
    }),
  });
  if (r.ok) review.value = await r.json();
}
async function removeMedia(id) {
  const r = await fetch(`/api/media/${id}`, {
    method: "DELETE",
    credentials: "include",
    headers: { "X-CSRF-TOKEN": props.csrf },
  });
  if (r.ok) media.value = media.value.filter((x) => x.id !== id);
}
watch(() => props.place, load, { immediate: true });
</script>
<template>
  <section v-if="place" class="modal vue-detail-panel">
    <button class="modal-close" @click="emit('close')">×</button>
    <div class="modal-kicker">TRAVEL LOG · DETAIL</div>
    <h2>{{ place.name }}</h2>
    <p class="modal-intro">
      {{ [place.city, place.country, place.date].filter(Boolean).join(" · ") }}
    </p>
    <div v-if="loading">加载中…</div>
    <div v-if="error" class="auth-error">{{ error }}</div>
    <div class="detail-gallery">
      <div v-for="item in media" :key="item.id" class="media-item">
        <a :href="item.originalUrl" target="_blank"
          ><img :src="item.thumbnailUrl" :alt="item.originalFilename" /></a
        ><button @click="removeMedia(item.id)">删除</button>
      </div>
      <span v-if="!media.length">暂无图片</span>
    </div>
    <p>{{ place.guide || "暂无攻略" }}</p>
    <form @submit.prevent="saveReview">
      <label>评分<input v-model="stars" type="number" min="1" max="5" /></label
      ><label>评价<textarea v-model="content" rows="3"></textarea></label
      ><button class="create-btn">保存评价</button>
    </form>
    <div v-if="review" class="detail-review">
      当前评分：{{ review.stars || "—" }} / 5<br />{{
        review.content || "暂无评价"
      }}
    </div>
  </section>
</template>
