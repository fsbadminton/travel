# 足迹 · 个人旅行地图 MVP

当前版本是可直接运行的前端原型，数据使用浏览器 `localStorage` 保存，地图底图使用 Leaflet + OpenStreetMap。项目已补充 Vite 工程配置，后续可逐步迁移为 Vue 组件和真实 API。

## 运行

安装依赖并启动开发服务器：

```bash
npm install
npm run dev
```

也可以直接打开 `index.html` 预览静态原型。

原型已包含：地图点位、关键词搜索、日期连线开关、景点/美食新增、内容管理列表和本地持久化。后续接入 Spring Boot 时，将 `app.js` 中的本地数据读写替换为需求文档中的 `/api` 接口即可。

## 后端认证服务

`backend/` 提供 Spring Boot 3 + MySQL + Flyway 的认证基础：注册、登录、登出、修改密码、当前用户和管理员账号状态管理。启动数据库后配置管理员环境变量：

```bash
docker compose up -d mysql
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD='change-this-password'
cd backend
mvn spring-boot:run
```

认证接口使用服务端会话和 CSRF token。前端请求写接口前先调用 `GET /api/auth/csrf`，再将返回的 token 放进指定请求头。
