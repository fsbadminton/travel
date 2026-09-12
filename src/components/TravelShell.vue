<script setup>
import { computed } from "vue";
import { useTravelStore } from "../stores/travel";
import DetailPanel from "./DetailPanel.vue";

const store = useTravelStore();
const accountLabel = computed(() => store.user?.username || "本地模式");
</script>

<template>
  <div class="vue-shell-status" aria-live="polite">
    <span>Vue</span><strong>{{ accountLabel }}</strong
    ><span v-if="store.uploadProgress">上传 {{ store.uploadProgress }}%</span>
  </div>
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
</template>
