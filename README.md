# HR System

基于 Spring Boot 3 + Vue 3 的轻量人事管理系统，SQLite 存储，开箱即用。

## 技术栈

**后端** Spring Boot 3.3、Spring Data JPA、SQLite、Flyway
**前端** Vue 3、Pinia、Vue Router、Chart.js、Axios、Vite

## 功能

- 员工管理：入职、离职、复职、员工编号校验、手机号校验
- 组织架构：多层级树形结构维护
- 考勤管理：打卡签到、考勤规则配置、人脸识别打卡
- 薪酬管理：薪资记录与查询
- 权限控制：角色与权限配置，支持 JWT 认证
- 数据看板：可视化图表展示核心指标
- 导入导出：支持 Excel 格式数据批量操作

## 快速启动

```bash
# 启动后端（默认端口 18080）
./start.sh

# 启动前端开发服务器
npm install
npm run dev
```

启动后访问 `http://localhost:18080`，前端开发模式默认代理到后端 API。
