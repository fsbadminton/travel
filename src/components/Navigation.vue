<script setup>
import { useTravelStore } from "../stores/travel";
const store = useTravelStore();
function go(view) {
  store.setView(view);
  document
    .querySelector("#mapView")
    ?.classList.toggle("hidden", view !== "map");
  document
    .querySelector("#manageView")
    ?.classList.toggle("hidden", view === "map");
}
function add(type) {
  window.dispatchEvent(new CustomEvent("travel:entry", { detail: { type } }));
}
</script>
<template>
  <nav class="nav vue-nav">
    <button
      :class="['nav-item', store.view === 'map' ? 'active' : '']"
      @click="go('map')"
    >
      我的地图</button
    ><button
      :class="['nav-item', store.view === 'manage' ? 'active' : '']"
      @click="go('manage')"
    >
      内容管理</button
    ><button class="create-btn" @click="add('place')">新增景点</button
    ><button class="secondary-btn" @click="add('food')">新增美食</button>
  </nav>
</template>
