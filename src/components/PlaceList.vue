<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useTravelStore } from "../stores/travel";

const store = useTravelStore();
const places = ref([]);
const query = ref("");
const visible = computed(() =>
  store.places
    .filter(
      (p) =>
        (p.status || "published") === "published" &&
        [p.name, p.city, p.country, p.guide].some((x) =>
          (x || "").toLowerCase().includes(query.value.toLowerCase()),
        ),
    )
    .sort((a, b) => (b.date || "").localeCompare(a.date || "")),
);
function sync(e) {
  places.value = e.detail || [];
}
function select(place) {
  store.select(place);
  window.focusPlace?.(place.id);
}
onMounted(() => {
  window.addEventListener("travel:places", sync);
  sync({ detail: window.travelPlaces || [] });
});
onUnmounted(() => window.removeEventListener("travel:places", sync));
</script>
<template>
  <div class="place-list-vue">
    <div class="place-search-row">
      <input
        v-model="query"
        class="vue-list-search"
        aria-label="搜索地点"
        placeholder="搜索地点…"
      />
      <p v-if="!visible.length" class="place-summary" role="status">
        {{ query.trim() ? "没有匹配的地点" : "暂无已发布地点" }}
      </p>
      <p v-else class="place-summary" role="status">
        {{ visible.length }} 个地点
      </p>
    </div>
    <div v-if="visible.length" class="place-results">
      <article
        v-for="place in visible"
        :key="place.id"
        class="place-card"
        :data-id="place.id"
        @click="select(place)"
      >
        <div class="place-top">
          <i :class="['place-dot', place.type]"></i>
          <div>
            <h3>{{ place.name }}</h3>
            <p>{{ place.city }}</p>
          </div>
        </div>
        <div class="place-meta">
          <span>{{ (place.date || "").replaceAll("-", " / ") }}</span
          ><b>{{ place.type === "food" ? "美食" : "景点" }}　›</b>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.place-list-vue {
  flex: 0 0 auto;
  min-width: 0;
  padding: 10px 18px;
  border-bottom: 1px solid var(--line);
  background: var(--paper);
}

.place-search-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.vue-list-search {
  flex: 1;
  min-width: 0;
  max-width: 420px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--white);
  font: inherit;
}

.place-summary {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
  white-space: nowrap;
}

.place-results {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-top: 8px;
  padding-bottom: 4px;
}

.place-results .place-card {
  flex: 0 0 220px;
  min-width: 0;
  padding: 10px;
}

.place-card h3,
.place-card p {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 175px;
}

@media (max-width: 700px) {
  .place-list-vue {
    padding: 8px 12px;
  }

  .place-search-row {
    gap: 8px;
  }
}
</style>
