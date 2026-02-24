# 部署指南

## 环境准备

### 1. 基础环境

- **操作系统**: Linux/Windows/MacOS
- **JDK**: 21+
- **Maven**: 3.8+
- **Node.js**: 18+
- **Docker**: 20.10+ (可选)
- **Docker Compose**: 2.0+ (可选)

### 2. 中间件

- **MySQL**: 8.0+
- **Redis**: 6.0+
- **MinIO**: 最新版
- **Nacos**: 2.0+

## 部署方式

### 方式一：Docker Compose 部署（推荐）

#### 1. 启动基础设施

Windows:
```bash
start-infrastructure.bat
```

Linux/MacOS:
```bash
chmod +x start-infrastructure.sh
./start-infrastructure.sh
```

#### 2. 验证基础设施

访问以下地址确认服务正常：
- Nacos: http://localhost:8848/nacos (nacos/nacos)
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

#### 3. 编译后端服务

```bash
mvn clean package -DskipTests
```

#### 4. 启动微服务

```bash
# 启动网关
cd ai-gateway
java -jar target/ai-gateway-1.0.0-SNAPSHOT.jar

# 启动认证服务
cd ai-auth-service
java -jar target/ai-auth-service-1.0.0-SNAPSHOT.jar

# 启动知识库服务
cd ai-knowledge-service
java -jar target/ai-knowledge-service-1.0.0-SNAPSHOT.jar
```

#### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 方式二：手动部署

#### 1. 安装 MySQL

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE ai_knowledge_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化脚本
mysql -u root -p ai_knowledge_platform < sql/init.sql
```

#### 2. 安装 Redis

```bash
# Linux
sudo apt-get install redis-server
sudo systemctl start redis

# MacOS
brew install redis
brew services start redis
```

#### 3. 安装 MinIO

下载并启动 MinIO:
```bash
# Linux
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
./minio server /data --console-address ":9001"

# Windows
# 下载 minio.exe 并运行
minio.exe server C:\data --console-address ":9001"
```

#### 4. 安装 Nacos

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos/bin

# 启动 Nacos (单机模式)
# Linux/MacOS
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

#### 5. 配置修改

修改各服务的 `application.yml` 配置文件，确保数据库、Redis、MinIO、Nacos 的连接信息正确。

#### 6. 启动服务

按照 Docker Compose 部署方式的步骤 3-5 执行。

## 生产环境部署建议

### 1. 数据库优化

- 使用主从复制提高可用性
- 配置连接池参数
- 定期备份数据

### 2. Redis 优化

- 配置持久化策略
- 设置合理的内存限制
- 使用 Redis Cluster 提高性能

### 3. 应用优化

- 使用 Nginx 作为反向代理
- 配置 HTTPS
- 启用 Gzip 压缩
- 配置日志轮转

### 4. 监控告警

- 使用 Prometheus + Grafana 监控
- 配置应用性能监控 (APM)
- 设置告警规则

### 5. 安全加固

- 修改默认密码
- 配置防火墙规则
- 启用 API 限流
- 定期更新依赖

## 常见问题

### 1. Nacos 连接失败

检查 Nacos 是否正常启动，确认配置文件中的地址正确。

### 2. 数据库连接失败

检查 MySQL 是否启动，确认用户名密码正确，检查防火墙设置。

### 3. MinIO 上传失败

检查 MinIO 是否启动，确认 bucket 是否创建，检查权限配置。

### 4. 前端无法访问后端

检查网关是否启动，确认跨域配置正确，检查浏览器控制台错误信息。

## 性能调优

### JVM 参数建议

```bash
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar app.jar
```

### 数据库索引优化

确保以下字段建立索引：
- 用户表: username, email
- 文档表: category_id, status, create_time
- 问答历史表: user_id, create_time

### 缓存策略

- 热点数据缓存到 Redis
- 设置合理的过期时间
- 使用缓存预热

## 备份与恢复

### 数据库备份

```bash
# 备份
mysqldump -u root -p ai_knowledge_platform > backup.sql

# 恢复
mysql -u root -p ai_knowledge_platform < backup.sql
```

### 文件备份

定期备份 MinIO 存储的文件数据。

## 升级指南

1. 备份数据库和文件
2. 停止所有服务
3. 更新代码
4. 执行数据库迁移脚本
5. 重新编译部署
6. 验证功能正常

