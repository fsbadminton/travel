<script setup>
import Navigation from "./components/Navigation.vue";
import PlaceList from "./components/PlaceList.vue";
import ManageList from "./components/ManageList.vue";
import MapView from "./components/MapView.vue";
import AuthPanel from "./components/AuthPanel.vue";
import EntryForm from "./components/EntryForm.vue";
import DetailPanel from "./components/DetailPanel.vue";
import { useTravelStore } from "./stores/travel";
import { ref, watch } from "vue";
const store = useTravelStore();
const loadError = ref("");
async function load() {
  if (!store.user) return;
  loadError.value = "";
  const me = await fetch("/api/auth/me", { credentials: "include" });
  if (!me.ok) {
    loadError.value = "登录会话已过期，请重新登录";
    return;
  }
  const [a, b] = await Promise.all([
    fetch("/api/places", { credentials: "include" }),
    fetch("/api/foods", { credentials: "include" }),
  ]);
  if (a.ok && b.ok) {
    const ps = await a.json(),
      fs = await b.json();
    store.setPlaces([
      ...ps.map((p) => ({
        ...p,
        type: "place",
        date: p.visitDate,
        lat: p.latitude,
        lng: p.longitude,
        guide: p.guideContent,
      })),
      ...fs.map((p) => ({
        ...p,
        type: "food",
        date: "",
        lat: p.latitude,
        lng: p.longitude,
        guide: p.notes,
      })),
    ]);
  } else loadError.value = `内容加载失败（${a.status}/${b.status}）`;
}
window.loadRemotePlaces = load;
load();
watch(() => store.user, load);
</script>
<template>
  <div class="app-shell vue-app">
    <aside class="sidebar">
      <div class="brand"><strong>足迹</strong><span>TRAVEL LOG</span></div>
      <Navigation />
    </aside>
    <main class="main-content">
      <header class="topbar">
        <h1>{{ store.view === "map" ? "我的旅行地图" : "内容管理" }}</h1>
        <AuthPanel />
      </header>
      <section v-if="store.view === 'map'" class="workspace map-workspace">
        <p v-if="loadError" class="load-error">{{ loadError }}</p>
        <PlaceList />
        <div class="map-panel"><MapView /></div>
      </section>
      <section v-else class="workspace"><ManageList /></section>
      <EntryForm />
      <Teleport to="body"
        ><div
          v-if="store.selected"
          class="modal-backdrop"
          @click.self="store.select(null)"
        >
          <DetailPanel
            :place="store.selected"
            :csrf="store.csrf"
            @close="store.select(null)"
          /></div
      ></Teleport>
    </main>
  </div>
</template>

<style scoped>
.map-workspace {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.map-workspace > .map-panel {
  flex: 1 1 0;
  height: auto;
  min-height: 0;
}

.map-workspace :deep(.vue-map) {
  position: absolute;
  inset: 0;
  min-height: 0;
}
</style>
