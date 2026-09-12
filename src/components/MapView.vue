<script setup>
import { nextTick, onMounted, onUnmounted, watch } from "vue";
import { useTravelStore } from "../stores/travel";

const store = useTravelStore();
let map;
let markers = [];
let route;
let resizeObserver;
function draw() {
  if (!map) return;
  markers.forEach((m) => m.remove());
  markers = [];
  if (route) route.remove();
  const shown = store.places.filter(
    (p) => (p.status || "published") === "published",
  );
  shown.forEach((p) => {
    const marker = L.marker([p.lat, p.lng])
      .addTo(map)
      .bindPopup(`<strong>${p.name}</strong><br>${p.city || ""}`);
    marker.on("click", () => store.select(p));
    markers.push(marker);
  });
  if (store.lineEnabled && shown.length > 1)
    route = L.polyline(
      [...shown]
        .sort((a, b) => (a.date || "").localeCompare(b.date || ""))
        .map((p) => [p.lat, p.lng]),
      { color: "#4b9582", dashArray: "5 7" },
    ).addTo(map);
}
onMounted(async () => {
  await nextTick();
  map = L.map("vue-map", { zoomControl: false }).setView([30, 112], 3);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors",
    maxZoom: 19,
  }).addTo(map);
  draw();
  resizeObserver = new ResizeObserver(() => map?.invalidateSize());
  resizeObserver.observe(map.getContainer());
});
onUnmounted(() => {
  resizeObserver?.disconnect();
  map?.remove();
});
watch(() => [store.places, store.lineEnabled], draw, { deep: true });
</script>
<template><div id="vue-map" class="vue-map"></div></template>
