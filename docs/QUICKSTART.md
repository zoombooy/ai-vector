# 快速开始指南

本指南将帮助您在 10 分钟内快速启动 AI 知识库管理平台。

## 前置条件

确保您的系统已安装：
- ✅ Docker & Docker Compose
- ✅ JDK 21
- ✅ Maven 3.8+
- ✅ Node.js 18+

## 步骤 1: 克隆项目

```bash
git clone <repository-url>
cd ai-knowledge-platform
```

## 步骤 2: 启动基础设施

### Windows 用户

双击运行 `start-infrastructure.bat` 或在命令行执行：

```bash
start-infrastructure.bat
```

### Linux/MacOS 用户

```bash
chmod +x start-infrastructure.sh
./start-infrastructure.sh
```

这将启动以下服务：
- MySQL (端口 3306)
- Redis (端口 6379)
- MinIO (端口 9000, 9001)
- Nacos (端口 8848)

**等待约 30 秒，确保所有服务启动完成。**

## 步骤 3: 验证基础设施

访问以下地址确认服务正常：

1. **Nacos 控制台**: http://localhost:8848/nacos
   - 用户名: nacos
   - 密码: nacos

2. **MinIO 控制台**: http://localhost:9001
   - 用户名: minioadmin
   - 密码: minioadmin

## 步骤 4: 编译后端项目

```bash
mvn clean package -DskipTests
```

## 步骤 5: 启动微服务

### 方式一：使用 IDE (推荐开发环境)

使用 IntelliJ IDEA 或 Eclipse 依次启动以下服务：

1. `ai-gateway` - API 网关
2. `ai-auth-service` - 认证服务
3. `ai-knowledge-service` - 知识库服务

### 方式二：使用命令行

打开 3 个终端窗口，分别执行：

**终端 1 - 启动网关**
```bash
cd ai-gateway
mvn spring-boot:run
```

**终端 2 - 启动认证服务**
```bash
cd ai-auth-service
mvn spring-boot:run
```

**终端 3 - 启动知识库服务**
```bash
cd ai-knowledge-service
mvn spring-boot:run
```

**等待所有服务启动完成（看到 "Started XXXApplication" 日志）**

## 步骤 6: 启动前端

打开新的终端窗口：

```bash
cd frontend
npm install
npm run dev
```

## 步骤 7: 访问系统

浏览器访问: http://localhost:3000

**默认登录账号**:
- 用户名: `admin`
- 密码: `admin123`

## 功能验证

### 1. 登录系统

使用默认账号登录系统。

### 2. 上传文档

1. 点击左侧菜单 "知识库" -> "上传文档"
2. 填写文档标题
3. 选择分类
4. 上传文件（支持 PDF、Word、TXT、Markdown）
5. 点击上传

### 3. 查看文档列表

1. 点击左侧菜单 "知识库" -> "文档列表"
2. 查看已上传的文档
3. 可以搜索、查看、删除文档

### 4. AI 问答

1. 点击左侧菜单 "AI问答"
2. 输入问题并发送
3. 查看 AI 回复（当前为模拟回复，需要配置 AI 模型后才能使用真实的 AI 问答）

## 常见问题

### Q1: Nacos 启动失败

**解决方案**: 
- 检查 MySQL 是否正常启动
- 查看 Docker 日志: `docker logs ai-nacos`

### Q2: 微服务无法注册到 Nacos

**解决方案**:
- 确保 Nacos 已完全启动
- 检查服务配置文件中的 Nacos 地址是否正确
- 查看服务日志确认错误信息

### Q3: 前端无法访问后端

**解决方案**:
- 确保网关服务已启动（端口 8080）
- 检查浏览器控制台的网络请求
- 确认 Vite 代理配置正确

### Q4: 文件上传失败

**解决方案**:
- 确保 MinIO 已启动
- 检查 MinIO 配置是否正确
- 查看知识库服务日志

### Q5: 端口冲突

**解决方案**:
- 修改 `docker-compose.yml` 中的端口映射
- 修改各服务 `application.yml` 中的端口配置

## 下一步

- 📖 阅读 [部署指南](DEPLOYMENT.md) 了解生产环境部署
- 📖 阅读 [API 文档](API.md) 了解接口详情
- 🔧 配置 AI 模型以启用真实的 AI 问答功能
- 👥 创建更多用户和角色
- 📁 上传更多文档构建知识库

## 停止服务

### 停止基础设施

```bash
docker-compose down
```

### 停止微服务

在各个服务的终端窗口按 `Ctrl + C`

## 技术支持

如遇到问题，请：
1. 查看服务日志
2. 检查 Docker 容器状态: `docker-compose ps`
3. 提交 Issue 到项目仓库

---

**祝您使用愉快！** 🎉

