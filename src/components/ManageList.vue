<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useTravelStore } from "../stores/travel";
const store = useTravelStore();

const rows = ref([]);
function sync(e) {
  rows.value = e.detail || [];
}
function action(id, name) {
  const item = store.places.find((x) => x.id === id);
  if (!item) return;
  if (name === "edit") {
    window.dispatchEvent(
      new CustomEvent("travel:entry", {
        detail: { type: item.type, id: item.id, place: item },
      }),
    );
    return;
  }
  const resource = item.type === "food" ? "foods" : "places";
  const method =
    name === "delete" ? "DELETE" : item.type === "place" ? "POST" : "PUT";
  const url =
    name === "delete"
      ? `/api/${resource}/${id}`
      : `/api/${resource}/${id}/${name}`;
  fetch(url, { method, credentials: "include" }).then(() =>
    window.loadRemotePlaces?.(),
  );
}
onMounted(() => {
  window.addEventListener("travel:places", sync);
  sync({ detail: window.travelPlaces || [] });
});
onUnmounted(() => window.removeEventListener("travel:places", sync));
</script>
<template>
  <div class="manage-table-vue">
    <div class="table-row head">
      <span>名称</span><span>地区</span><span>类型</span><span>状态</span
      ><span>操作</span>
    </div>
    <div v-for="item in store.places" :key="item.id" class="table-row">
      <strong>{{ item.name }}</strong
      ><span>{{ item.city }}</span
      ><span
        ><i :class="['tag', item.type === 'food' ? 'food' : '']">{{
          item.type === "food" ? "美食" : "景点"
        }}</i></span
      ><span
        ><i :class="['tag', item.status === 'draft' ? 'draft' : '']">{{
          item.status === "draft"
            ? "草稿"
            : item.status === "archived"
              ? "已归档"
              : "已发布"
        }}</i></span
      ><span class="row-actions"
        ><button @click="action(item.id, 'edit')">编辑</button
        ><button @click="action(item.id, 'publish')">发布</button
        ><button @click="action(item.id, 'archive')">归档</button
        ><button @click="action(item.id, 'delete')">删除</button></span
      >
    </div>
  </div>
</template>
