import { defineStore } from "pinia";

export const useTravelStore = defineStore("travel", {
  state: () => ({
    user: JSON.parse(localStorage.getItem("travel_user") || "null"),
    selected: null,
    uploadProgress: 0,
    csrf: "",
    view: "map",
    places: [],
    lineEnabled: false,
  }),
  actions: {
    setUser(user) {
      this.user = user;
      localStorage.setItem("travel_user", JSON.stringify(user));
    },
    select(place) {
      this.selected = place;
    },
    setUploadProgress(value) {
      this.uploadProgress = value;
    },
    setCsrf(token) {
      this.csrf = token;
    },
    setView(view) {
      this.view = view;
    },
    setPlaces(places) {
      this.places = places;
    },
    toggleLine() {
      this.lineEnabled = !this.lineEnabled;
    },
  },
});
