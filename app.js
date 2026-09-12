const seedPlaces = [
  {
    id: 1,
    type: "place",
    name: "清水寺",
    city: "京都市 · 日本",
    country: "日本",
    date: "2024-04-18",
    lat: 34.9949,
    lng: 135.785,
    guide: "清晨八点前到，人少光线也好。",
  },
  {
    id: 2,
    type: "place",
    name: "奈良公园",
    city: "奈良市 · 日本",
    country: "日本",
    date: "2024-04-16",
    lat: 34.6851,
    lng: 135.843,
    guide: "东大寺和春日大社建议分开半天。",
  },
  {
    id: 3,
    type: "food",
    name: "% Arabica Kyoto",
    city: "东山区 · 日本",
    country: "日本",
    date: "2024-04-18",
    lat: 34.9968,
    lng: 135.7785,
    guide: "八坂塔旁边的咖啡窗口。",
  },
  {
    id: 4,
    type: "place",
    name: "姬路城",
    city: "姬路市 · 日本",
    country: "日本",
    date: "2024-04-14",
    lat: 34.8394,
    lng: 134.6939,
    guide: "樱花季建议预约入场。",
  },
  {
    id: 5,
    type: "food",
    name: "京都胜牛",
    city: "京都市 · 日本",
    country: "日本",
    date: "2024-04-17",
    lat: 35.0035,
    lng: 135.7681,
    guide: "排队时间较长，错峰前往。",
  },
  {
    id: 6,
    type: "place",
    name: "白川乡合掌村",
    city: "岐阜县 · 日本",
    country: "日本",
    date: "2023-11-08",
    lat: 36.2575,
    lng: 136.906,
    guide: "巴士班次少，提前查好时间。",
  },
  {
    id: 7,
    type: "place",
    name: "台北 101",
    city: "信义区 · 中国台湾",
    country: "中国台湾",
    date: "2023-10-22",
    lat: 25.034,
    lng: 121.5645,
    guide: "日落前登顶可以看到城市灯亮起来。",
  },
  {
    id: 8,
    type: "food",
    name: "阜杭豆浆",
    city: "中正区 · 中国台湾",
    country: "中国台湾",
    date: "2023-10-21",
    lat: 25.0447,
    lng: 121.5254,
    guide: "早起排队，蛋饼和厚烧饼必点。",
  },
  {
    id: 9,
    type: "place",
    name: "乌尤尼盐沼",
    city: "波托西省 · 玻利维亚",
    country: "玻利维亚",
    date: "2022-08-06",
    lat: -20.1338,
    lng: -67.4891,
    guide: "旱季的星空和镜面都值得。",
  },
];
let places =
  JSON.parse(localStorage.getItem("travel_places") || "null") || seedPlaces;
window.travelPlaces = places;
let map,
  markers = [],
  route;
function normalizePlace(p, type) {
  return {
    ...p,
    type,
    name: p.name || "",
    city: p.city || "",
    country: p.country || "",
    date: p.visitDate || p.date || "",
    lat: Number(p.latitude ?? p.lat),
    lng: Number(p.longitude ?? p.lng),
    guide: p.guideContent || p.guide || "",
    status: p.status || "published",
  };
}
async function loadRemotePlaces() {
  try {
    const u = JSON.parse(localStorage.getItem("travel_user") || "null");
    if (!u?.username) return false;
    const [a, b] = await Promise.all([
      fetch("/api/places", { credentials: "include" }),
      fetch("/api/foods", { credentials: "include" }),
    ]);
    if (!a.ok || !b.ok) return false;
    const [ps, fs] = await Promise.all([a.json(), b.json()]);
    places = [
      ...ps.map((p) => normalizePlace(p, "place")),
      ...fs.map((p) => normalizePlace(p, "food")),
    ];
    window.travelStore?.setPlaces(places);
    renderList();
    renderManage();
    return true;
  } catch {
    return false;
  }
}
window.loadRemotePlaces = loadRemotePlaces;
const $ = (s) => document.querySelector(s);
const esc = (s) =>
  String(s || "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
function icon(type) {
  return L.divIcon({
    className: "custom-pin",
    html: `<span style="display:block;width:14px;height:14px;border-radius:50%;background:${type === "food" ? "#e17c45" : "#1d6a5b"};border:3px solid white;box-shadow:0 2px 8px rgba(20,50,40,.25)"></span>`,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
  });
}
function renderList() {
  window.travelPlaces = places;
  window.travelStore?.setPlaces(places);
  window.dispatchEvent(new CustomEvent("travel:places", { detail: places }));
  if (window.vueListMounted) {
    renderMarkers(
      places.filter((p) => (p.status || "published") === "published"),
    );
    return;
  }
  const q = $("#searchInput").value.toLowerCase();
  const shown = places.filter(
    (p) =>
      (p.status || "published") === "published" &&
      [p.name, p.city, p.country, p.guide].some((x) =>
        (x || "").toLowerCase().includes(q),
      ),
  );
  $("#visibleCount").textContent = String(shown.length).padStart(2, "0");
  $("#placeCount").textContent = String(
    places.filter((p) => p.type === "place").length,
  ).padStart(2, "0");
  $("#foodCount").textContent = String(
    places.filter((p) => p.type === "food").length,
  ).padStart(2, "0");
  $("#placeList").innerHTML = shown
    .sort((a, b) => b.date.localeCompare(a.date))
    .map(
      (p) =>
        `<article class="place-card" data-id="${p.id}"><div class="place-top"><i class="place-dot ${p.type}"></i><div><h3>${esc(p.name)}</h3><p>${esc(p.city)}</p></div></div><div class="place-meta"><span>${p.date.replaceAll("-", " / ")}</span><b>${p.type === "food" ? "美食" : "景点"}　›</b></div></article>`,
    )
    .join("");
  document
    .querySelectorAll(".place-card")
    .forEach((el) => (el.onclick = () => focusPlace(+el.dataset.id)));
  renderMarkers(shown);
}
function renderMarkers(shown) {
  markers.forEach((m) => m.remove());
  markers = [];
  if (route) route.remove();
  shown.forEach((p) => {
    const m = L.marker([p.lat, p.lng], { icon: icon(p.type) })
      .addTo(map)
      .bindPopup(
        `<div class="popup-title">${esc(p.name)}</div><div class="popup-meta">${esc(p.city)} · ${p.date}</div>`,
      );
    m.on("click", () =>
      document.querySelector(`[data-id="${p.id}"]`)?.classList.add("selected"),
    );
    markers.push(m);
  });
  if ($("#lineToggle").classList.contains("on") && shown.length > 1) {
    const sorted = [...shown].sort((a, b) => a.date.localeCompare(b.date));
    route = L.polyline(
      sorted.map((p) => [p.lat, p.lng]),
      { color: "#4b9582", weight: 2, dashArray: "5 7", opacity: 0.8 },
    ).addTo(map);
  }
}
function focusPlace(id) {
  const p = places.find((x) => x.id === id);
  if (!p) return;
  map.flyTo([p.lat, p.lng], Math.max(map.getZoom(), 10), { duration: 0.6 });
  const i = places
    .filter((x) => (x.status || "published") === "published")
    .findIndex((x) => x.id === id);
  if (markers[i]) markers[i].openPopup();
  document
    .querySelectorAll(".place-card")
    .forEach((x) => x.classList.toggle("selected", +x.dataset.id === id));
  window.travelStore?.select(p);
  if (!window.travelStore) openDetail(p);
}
window.focusPlace = focusPlace;
let detailPlace = null;
async function deleteMedia(id) {
  const token = await getCsrf();
  const response = await fetch(`/api/media/${id}`, {
    method: "DELETE",
    credentials: "include",
    headers: { "X-CSRF-TOKEN": token },
  });
  if (!response.ok) throw new Error("图片删除失败");
}
async function openDetail(p) {
  detailPlace = p;
  $("#detailTitle").textContent = p.name;
  $("#detailMeta").textContent = [p.city, p.country, p.date]
    .filter(Boolean)
    .join(" · ");
  $("#detailGuide").textContent = p.guide || "暂无攻略";
  $("#detailReview").textContent = "评价加载中…";
  $("#reviewHistory").textContent = "";
  $("#detailGallery").innerHTML = "";
  $("#detailBackdrop").classList.remove("hidden");
  document.querySelectorAll("[data-media-id]").forEach((button) => {
    button.onclick = async () => {
      try {
        await deleteMedia(button.dataset.mediaId);
        await openDetail(p);
      } catch (error) {
        alert(error.message);
      }
    };
  });
  try {
    const [m, r] = await Promise.all([
      fetch(`/api/media?placeId=${p.id}`, { credentials: "include" }),
      fetch(`/api/places/${p.id}/review`, { credentials: "include" }),
    ]);
    if (m.ok) {
      const ms = await m.json();
      $("#detailGallery").innerHTML =
        ms
          .map(
            (x) =>
              `<div class="media-item"><a href="${x.originalUrl}" target="_blank"><img src="${x.thumbnailUrl}" alt="${esc(x.originalFilename)}"></a><button type="button" data-media-id="${x.id}">删除</button></div>`,
          )
          .join("") || "<span>暂无图片</span>";
    }
    if (r.ok) {
      const v = await r.json();
      if (v) {
        $("#reviewForm [name=stars]").value = v.stars || "";
        $("#reviewForm [name=content]").value = v.content || "";
        $("#detailReview").innerHTML =
          `<div class="detail-review">当前评分：${v.stars || "—"} / 5<br>${esc(v.content || "暂无评价")}</div>`;
        const h = await fetch(`/api/reviews/${v.id}/versions`, {
          credentials: "include",
        });
        if (h.ok) {
          const hs = await h.json();
          $("#reviewHistory").innerHTML =
            "<h3>评价历史</h3>" +
            hs
              .map(
                (x) =>
                  `<p>版本 ${x.versionNo} · ${new Date(x.createdAt).toLocaleString()}</p>`,
              )
              .join("");
        }
      } else $("#detailReview").textContent = "暂无评价";
    }
  } catch {
    $("#detailReview").textContent = "评价暂不可用";
  }
}
function initMap() {
  if (window.vueMapMounted) return;
  map = L.map("map", {
    zoomControl: false,
    dragging: true,
    touchZoom: true,
    scrollWheelZoom: true,
    doubleClickZoom: true,
    boxZoom: true,
    keyboard: true,
  }).setView([30, 112], 3);
  map.dragging.enable();
  map.touchZoom.enable();
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors",
    maxZoom: 19,
  }).addTo(map);
  renderList();
  $("#zoomIn").onclick = () => map.zoomIn();
  $("#zoomOut").onclick = () => map.zoomOut();
  $("#locateBtn").onclick = () => map.setView([30, 112], 3);
}
function openModal(type = "place") {
  if (window.vueEntryMounted) {
    window.dispatchEvent(new CustomEvent("travel:entry", { detail: { type } }));
    return;
  }
  $("#modalTypeLabel").textContent = type === "food" ? "美食" : "景点";
  $("#modalTitle").textContent =
    type === "food" ? "记录一家美食" : "记录一处景点";
  $("#modalBackdrop").classList.remove("hidden");
  $("#entryForm").dataset.type = type;
  setTimeout(() => $("#entryForm input").focus(), 80);
}
function closeModal() {
  $("#modalBackdrop").classList.add("hidden");
  $("#entryForm").reset();
}
function showToast() {
  const t = $("#toast");
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 3000);
}
let csrfToken = "";
async function getCsrf() {
  if (csrfToken) return csrfToken;
  const r = await fetch("/api/auth/csrf", { credentials: "include" });
  if (!r.ok) throw new Error("无法获取安全令牌");
  const d = await r.json();
  csrfToken = d.token;
  return csrfToken;
}
async function createRemoteEntry(f, type) {
  const token = await getCsrf();
  const lat = +f.get("lat") || 35,
    lng = +f.get("lng") || 135;
  if (type === "food") {
    const r = await fetch("/api/foods", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": token },
      body: JSON.stringify({
        name: f.get("name"),
        address: f.get("address") || f.get("city") || "",
        longitude: lng,
        latitude: lat,
        status: f.get("status") || "draft",
        notes: f.get("guide") || null,
      }),
    });
    if (!r.ok) throw new Error("美食保存失败");
    return normalizePlace(await r.json(), "food");
  }
  const r = await fetch("/api/places", {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": token },
    body: JSON.stringify({
      name: f.get("name"),
      country: f.get("country"),
      city: f.get("city"),
      address: f.get("address"),
      longitude: lng,
      latitude: lat,
      visitDate: f.get("date"),
      status: f.get("status") || "draft",
      guideContent: f.get("guide"),
    }),
  });
  if (!r.ok) throw new Error("景点保存失败");
  return normalizePlace(await r.json(), "place");
}
async function uploadPhotos(files, placeId) {
  const valid = files
    .filter(
      (file) => file.type.startsWith("image/") && file.size <= 20 * 1024 * 1024,
    )
    .slice(0, 10);
  const status =
    document.querySelector("#uploadStatus") ||
    (() => {
      const el = document.createElement("div");
      el.id = "uploadStatus";
      document.querySelector("#entryForm")?.append(el);
      return el;
    })();
  if (files.length !== valid.length)
    status.textContent = "部分图片无效或超过 20MB/10 张限制";
  for (let i = 0; i < valid.length; i++) {
    const file = valid[i];
    const form = new FormData();
    form.append("file", file);
    form.append("placeId", placeId);
    const token = await getCsrf();
    const response = await fetch("/api/media", {
      method: "POST",
      credentials: "include",
      headers: { "X-CSRF-TOKEN": token },
      body: form,
    });
    status.textContent = `图片上传 ${i + 1}/${valid.length}`;
    if (!response.ok) status.textContent = `第 ${i + 1} 张图片上传失败`;
  }
}
const createRemoteEntryBase = createRemoteEntry;
createRemoteEntry = async (f, type) => {
  const entry = await createRemoteEntryBase(f, type);
  if (type === "place") await uploadPhotos(f.getAll("photos"), entry.id);
  return entry;
};
async function manageAction(id, action) {
  const p = places.find((x) => x.id === id);
  if (!p) return;
  if (action === "edit") {
    if (window.vueEntryMounted) {
      window.dispatchEvent(
        new CustomEvent("travel:entry", {
          detail: { type: p.type, id: p.id, place: p },
        }),
      );
      return;
    }
    openModal(p.type);
    const form = $("#entryForm");
    form.dataset.editId = id;
    for (const key of [
      "name",
      "country",
      "city",
      "address",
      "date",
      "guide",
      "status",
    ])
      if (form.elements[key]) form.elements[key].value = p[key] || "";
    form.elements.lat.value = p.lat;
    form.elements.lng.value = p.lng;
    return;
  }
  if (action === "delete" && !confirm("确定永久删除这条记录吗？")) return;
  try {
    const token = await getCsrf();
    let url = `/api/${p.type === "food" ? "foods" : "places"}/${id}`,
      method = "DELETE",
      body;
    if (action === "publish" || action === "archive") {
      url += p.type === "place" ? `/${action}` : "";
      method = p.type === "food" ? "PUT" : "POST";
      if (p.type === "food") {
        body = JSON.stringify({
          name: p.name,
          address: p.address || p.city || "",
          longitude: p.lng,
          latitude: p.lat,
          status: action === "publish" ? "published" : "archived",
          notes: p.guide || null,
        });
      }
    }
    const r = await fetch(url, {
      method,
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": token,
        ...(body ? { "Content-Type": "application/json" } : {}),
      },
      body,
    });
    if (!r.ok) throw new Error();
  } catch {}
  if (action === "delete") places = places.filter((x) => x.id !== id);
  else p.status = action === "publish" ? "published" : "archived";
  localStorage.setItem("travel_places", JSON.stringify(places));
  renderList();
  renderManage();
}
window.manageAction = manageAction;
function renderManage() {
  if (window.vueManageMounted) {
    window.travelPlaces = places;
    window.dispatchEvent(new CustomEvent("travel:places", { detail: places }));
    return;
  }
  const rows = places
    .map(
      (p) =>
        `<div class="table-row"><strong>${esc(p.name)}</strong><span>${esc(p.city)}</span><span><i class="tag ${p.type === "food" ? "food" : ""}">${p.type === "food" ? "美食" : "景点"}</i></span><span><i class="tag ${p.status === "draft" ? "draft" : ""}">${p.status === "draft" ? "草稿" : p.status === "archived" ? "已归档" : "已发布"}</i></span><span class="row-actions"><button data-action="publish" data-id="${p.id}">发布</button><button data-action="archive" data-id="${p.id}">归档</button><button data-action="delete" data-id="${p.id}">删除</button></span></div>`,
    )
    .join("");
  $("#manageTable").innerHTML =
    '<div class="table-row head"><span>名称</span><span>地区</span><span>类型</span><span>状态</span><span>操作</span></div>' +
    rows;
  document
    .querySelectorAll(".row-actions button")
    .forEach(
      (b) => (b.onclick = () => manageAction(+b.dataset.id, b.dataset.action)),
    );
  document
    .querySelectorAll(".table-row:not(.head) .row-actions")
    .forEach((el) => {
      const id = el.querySelector("button")?.dataset.id;
      if (id && !el.querySelector('[data-action="edit"]')) {
        const b = document.createElement("button");
        b.dataset.action = "edit";
        b.dataset.id = id;
        b.textContent = "编辑";
        b.onclick = () => manageAction(+id, "edit");
        el.prepend(b);
      }
    });
}
document.addEventListener("DOMContentLoaded", () => {
  $("#reviewForm").onsubmit = async (e) => {
    e.preventDefault();
    if (!detailPlace) return;
    try {
      const token = await getCsrf(),
        f = new FormData(e.target),
        r = await fetch(`/api/places/${detailPlace.id}/review`, {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": token,
          },
          body: JSON.stringify({
            stars: Number(f.get("stars")) || null,
            content: f.get("content"),
          }),
        });
      if (!r.ok) throw new Error();
      await openDetail(detailPlace);
    } catch {
      $("#detailReview").textContent = "评价保存失败";
    }
  };
  $("#detailClose").onclick = () =>
    $("#detailBackdrop").classList.add("hidden");
  $("#detailBackdrop").onclick = (e) => {
    if (e.target.matches("[data-media-id]")) {
      deleteMedia(e.target.dataset.mediaId)
        .then(() => openDetail(detailPlace))
        .catch((error) => alert(error.message));
      return;
    }
    if (e.target.id === "detailBackdrop")
      e.currentTarget.classList.add("hidden");
  };
  initMap();
  renderManage();
  $("#searchInput").oninput = renderList;
  $("#lineToggle").onclick = () => {
    $("#lineToggle").classList.toggle("on");
    renderList();
  };
  $("#newPlaceBtn").onclick = () => openModal("place");
  $("#newFoodBtn").onclick = () => openModal("food");
  $("#manageNewBtn").onclick = () => openModal("place");
  $("#modalClose").onclick = closeModal;
  $("#cancelBtn").onclick = closeModal;
  $("#modalBackdrop").onclick = (e) => {
    if (e.target.id === "modalBackdrop") closeModal();
  };
  $("#entryForm").onsubmit = async (e) => {
    e.preventDefault();
    const f = new FormData(e.target),
      type = e.target.dataset.type;
    try {
      const u = JSON.parse(localStorage.getItem("travel_user") || "null");
      if (u?.username) {
        if (e.target.dataset.editId) {
          const p = places.find((x) => x.id === +e.target.dataset.editId),
            token = await getCsrf();
          const payload = {
            name: f.get("name"),
            country: f.get("country"),
            city: f.get("city"),
            address: f.get("address"),
            longitude: +f.get("lng"),
            latitude: +f.get("lat"),
            visitDate: f.get("date"),
            status: f.get("status"),
            guideContent: f.get("guide"),
          };
          const url = `/api/${type === "food" ? "foods" : "places"}/${p.id}`;
          const response = await fetch(url, {
            method: "PUT",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
              "X-CSRF-TOKEN": token,
            },
            body: JSON.stringify(
              type === "food"
                ? {
                    name: payload.name,
                    address: payload.address || payload.city || "",
                    longitude: payload.longitude,
                    latitude: payload.latitude,
                    status: payload.status,
                    notes: payload.guideContent,
                  }
                : payload,
            ),
          });
          if (!response.ok) throw new Error();
          const updated = await response.json();
          places = places.map((x) =>
            x.id === p.id ? { ...x, ...normalizePlace(updated, type) } : x,
          );
          delete e.target.dataset.editId;
        } else {
          const p = await createRemoteEntry(f, type);
          places.push(p);
        }
      } else throw new Error("offline");
    } catch {
      const p = {
        id: Date.now(),
        type,
        name: f.get("name"),
        country: f.get("country"),
        city: f.get("city"),
        address: f.get("address"),
        date: f.get("date"),
        lat: +f.get("lat") || 35,
        lng: +f.get("lng") || 135,
        guide: f.get("guide"),
        status: f.get("status"),
      };
      places.push(p);
      localStorage.setItem("travel_places", JSON.stringify(places));
    }
    closeModal();
    renderList();
    renderManage();
    showToast();
  };
  if (!window.vueNavigationMounted)
    document.querySelectorAll(".nav-item").forEach(
      (n) =>
        (n.onclick = () => {
          document
            .querySelectorAll(".nav-item")
            .forEach((x) => x.classList.remove("active"));
          n.classList.add("active");
          const isMap = n.dataset.view === "map";
          $("#mapView").classList.toggle("hidden", !isMap);
          $("#manageView").classList.toggle("hidden", isMap);
          if (isMap) setTimeout(() => map.invalidateSize(), 50);
          else renderManage();
        }),
    );
  if (!window.vueNavigationMounted)
    $("#manageBtn").onclick = () =>
      document.querySelector('[data-view="manage"]').click();
});
let authRegister = false;
document.addEventListener("DOMContentLoaded", () => {
  const input = document.createElement("input");
  input.type = "file";
  input.name = "photos";
  input.accept = "image/*";
  input.multiple = true;
  input.title = "选择图片（每张不超过 20MB）";
  const guide = $("#entryForm")?.querySelector('textarea[name="guide"]');
  if (guide) guide.parentElement.before(input);
});
document.addEventListener("DOMContentLoaded", () => {
  loadRemotePlaces();
});
function openAuth() {
  $("#authBackdrop").classList.remove("hidden");
  $("#authError").textContent = "";
  $("#authForm").reset();
}
async function submitAuth(e) {
  e.preventDefault();
  const f = new FormData(e.target);
  const endpoint = authRegister ? "/api/auth/register" : "/api/auth/login";
  try {
    const r = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({
        username: f.get("username"),
        password: f.get("password"),
      }),
    });
    const data = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(data.message || "请求失败");
    csrfToken = "";
    localStorage.setItem("travel_user", JSON.stringify(data));
    $("#accountName").textContent = data.username || f.get("username");
    $("#authBackdrop").classList.add("hidden");
    await loadRemotePlaces();
    showToast();
  } catch (err) {
    $("#authError").textContent =
      err.message + "（后端未启动时可继续使用本地原型）";
  }
}
document.addEventListener("DOMContentLoaded", () => {
  $("#accountBtn").onclick = openAuth;
  $("#authClose").onclick = () => $("#authBackdrop").classList.add("hidden");
  $("#authBackdrop").onclick = (e) => {
    if (e.target.id === "authBackdrop") e.currentTarget.classList.add("hidden");
  };
  $("#authForm").onsubmit = submitAuth;
  $("#authSwitch").onclick = () => {
    authRegister = !authRegister;
    $("#authTitle").textContent = authRegister ? "注册足迹" : "登录足迹";
    $("#authSubmit").firstChild.textContent = authRegister ? "注册 " : "登录 ";
    $("#authSwitch").textContent = authRegister
      ? "已有账号，去登录"
      : "注册新账号";
  };
  const u = JSON.parse(localStorage.getItem("travel_user") || "null");
  if (u?.username) $("#accountName").textContent = u.username;
});
