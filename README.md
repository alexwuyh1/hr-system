# HR System - 人力资源管理系统

基于 Spring Boot 3.3.2 的企业级人力资源管理系统，支持员工管理、考勤打卡、薪资管理、人脸识别等功能。

## 🌟 功能特性

- **员工管理**：完整的员工信息 CRUD 操作，支持入职、离职、复职流程
- **人脸识别考勤**：基于 OpenCV 的人脸验证与打卡功能
- **考勤管理**：记录员工上下班时间，支持迟到、加班统计
- **薪资管理**：按月记录员工薪资，支持导出 Excel/CSV
- **排班管理**：灵活的班次安排与调休管理
- **请假/加班申请**：支持审批流程
- **数据报表**：实时统计 KPI 指标
- **权限控制**：基于角色的访问控制（RBAC）
- **数据导入导出**：支持 Excel 和 CSV 格式

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.3.2 |
| 语言 | Java 21 |
| 数据库 | SQLite |
| ORM | Spring Data JPA (Hibernate) |
| 模板引擎 | Thymeleaf |
| 安全 | Spring Security + JWT |
| 图像处理 | OpenCV 4.9.0 |
| Excel 处理 | Apache POI 5.2.5 |

## 📋 环境要求

- JDK 21+
- Maven 3.8+
- Linux/Windows/macOS

## 🚀 快速启动

### 本地运行

```bash
# 克隆项目
git clone <your-repo-url>
cd hr-system

# 编译打包
mvn clean package -DskipTests

# 运行应用
java -jar target/hr-system-0.0.1-SNAPSHOT.jar
```

访问 `http://localhost:18080`

### Docker 部署

```bash
# 构建镜像
docker build -t hr-system .

# 运行容器
docker run -p 18080:18080 hr-system
```

## 📁 项目结构

```
src/main/java/com/example/hr/
├── controller/     # REST API 控制器
├── service/        # 业务逻辑层
├── repository/     # 数据访问层
├── model/          # 实体模型
├── dto/            # 数据传输对象
├── config/         # 配置类
└── HrSystemApplication.java
```

## 🔌 API 接口

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 员工管理
- `GET /api/employees` - 获取员工列表
- `POST /api/employees` - 创建员工
- `PUT /api/employees/{id}` - 更新员工
- `DELETE /api/employees/{id}` - 删除员工
- `POST /api/employees/resign` - 员工离职
- `POST /api/employees/rehire` - 员工复职

### 考勤打卡
- `POST /api/face/verify` - 人脸验证
- `POST /api/face/checkin` - 上班打卡
- `POST /api/face/checkout` - 下班打卡

### 数据管理
- `POST /api/data/import/{type}` - 导入数据
- `GET /api/data/export/{type}` - 导出数据

### 报表
- `GET /api/reports/summary` - 获取统计报表

## 🔐 安全配置

- JWT Token 认证
- 基于角色的权限控制
- 敏感信息已从 Git 中排除（参考 `.gitignore`）

## 📝 注意事项

1. **敏感文件已排除**：数据库文件、配置文件等敏感信息不会提交到 Git
2. **首次启动**：应用会自动创建数据库并初始化基础数据
3. **人脸数据**：员工照片存储在本地 `data/avatars/` 目录

## 📄 许可证

MIT License

## 👥 开发者

HR System Team
