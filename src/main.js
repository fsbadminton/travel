import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";

const pinia = createPinia();
document.querySelector("#app").innerHTML = "";
createApp(App).use(pinia).mount("#app");
