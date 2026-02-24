# 开发指南

## 开发环境配置

### IDE 推荐

- **后端**: IntelliJ IDEA Ultimate
- **前端**: VS Code

### IDEA 插件推荐

- Lombok
- MyBatis X
- Maven Helper
- GitToolBox
- Rainbow Brackets

### VS Code 插件推荐

- ESLint
- Prettier
- Vetur
- Auto Import
- GitLens

## 代码规范

### Java 代码规范

遵循阿里巴巴 Java 开发手册：

1. **命名规范**
   - 类名使用大驼峰: `UserService`
   - 方法名使用小驼峰: `getUserById`
   - 常量使用全大写: `MAX_SIZE`
   - 包名使用小写: `com.aimanager.auth`

2. **注释规范**
   - 类和接口必须有 JavaDoc 注释
   - 公共方法必须有注释说明
   - 复杂逻辑必须有行内注释

3. **异常处理**
   - 使用自定义业务异常 `BusinessException`
   - 不要捕获 `Exception`，应捕获具体异常
   - 必须记录异常日志

### 前端代码规范

遵循 Airbnb JavaScript Style Guide：

1. **命名规范**
   - 组件名使用大驼峰: `UserList`
   - 变量名使用小驼峰: `userName`
   - 常量使用全大写: `API_BASE_URL`

2. **组件规范**
   - 使用函数式组件和 Hooks
   - Props 必须定义类型
   - 使用 TypeScript

3. **样式规范**
   - 使用 CSS Modules 或 styled-components
   - 避免内联样式
   - 使用 Ant Design 主题变量

## Git 工作流

### 分支管理

- `main`: 主分支，保护分支
- `develop`: 开发分支
- `feature/*`: 功能分支
- `bugfix/*`: 修复分支
- `release/*`: 发布分支

### 提交规范

使用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**:
```
feat(auth): 添加 JWT 认证功能

- 实现 JWT token 生成
- 实现 token 验证
- 添加认证过滤器

Closes #123
```

## 数据库开发规范

### 表设计规范

1. **命名规范**
   - 表名使用 `t_` 前缀
   - 字段名使用下划线分隔
   - 主键统一使用 `id`

2. **字段规范**
   - 必须有 `create_time` 和 `update_time`
   - 必须有 `create_by` 和 `update_by`
   - 使用逻辑删除 `deleted`

3. **索引规范**
   - 主键自动创建索引
   - 外键必须创建索引
   - 查询条件字段建议创建索引

### SQL 编写规范

1. 使用 MyBatis Plus 提供的方法
2. 复杂查询使用 XML 方式
3. 避免使用 `SELECT *`
4. 必须使用参数化查询，防止 SQL 注入

## API 开发规范

### RESTful 规范

- `GET`: 查询资源
- `POST`: 创建资源
- `PUT`: 更新资源（全量）
- `PATCH`: 更新资源（部分）
- `DELETE`: 删除资源

### 接口设计

1. **URL 设计**
   ```
   GET    /api/users          # 获取用户列表
   GET    /api/users/{id}     # 获取单个用户
   POST   /api/users          # 创建用户
   PUT    /api/users/{id}     # 更新用户
   DELETE /api/users/{id}     # 删除用户
   ```

2. **请求参数**
   - 使用 DTO 接收参数
   - 必须进行参数校验
   - 使用 `@Valid` 注解

3. **响应格式**
   - 统一使用 `Result<T>` 包装
   - 必须包含 code、message、data
   - 错误信息要清晰明确

## 测试规范

### 单元测试

1. 使用 JUnit 5
2. 测试覆盖率要求 > 70%
3. 测试方法命名: `should_xxx_when_xxx`

### 集成测试

1. 使用 `@SpringBootTest`
2. 使用 H2 内存数据库
3. 测试完整的业务流程

## 日志规范

### 日志级别

- `ERROR`: 错误信息，需要立即处理
- `WARN`: 警告信息，需要关注
- `INFO`: 重要的业务流程信息
- `DEBUG`: 调试信息

### 日志内容

```java
// 好的日志
log.info("用户登录成功, userId={}, username={}", userId, username);

// 不好的日志
log.info("登录成功");
```

## 性能优化建议

1. **数据库优化**
   - 使用索引
   - 避免 N+1 查询
   - 使用分页查询
   - 使用缓存

2. **代码优化**
   - 避免循环中查询数据库
   - 使用批量操作
   - 合理使用异步处理
   - 避免大对象传输

3. **缓存策略**
   - 热点数据缓存
   - 设置合理的过期时间
   - 使用缓存预热
   - 防止缓存穿透

## 常用命令

### Maven 命令

```bash
# 编译
mvn clean compile

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 安装到本地仓库
mvn clean install
```

### Docker 命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart [service-name]
```

### Git 命令

```bash
# 创建功能分支
git checkout -b feature/xxx

# 提交代码
git add .
git commit -m "feat: xxx"

# 推送代码
git push origin feature/xxx

# 合并代码
git checkout develop
git merge feature/xxx
```

## 问题排查

### 常见问题

1. **服务无法启动**
   - 检查端口是否被占用
   - 检查配置文件是否正确
   - 查看启动日志

2. **数据库连接失败**
   - 检查数据库是否启动
   - 检查连接配置
   - 检查防火墙设置

3. **Nacos 注册失败**
   - 检查 Nacos 是否启动
   - 检查网络连接
   - 查看服务日志

## 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [React 官方文档](https://reactjs.org/)
- [Ant Design 官方文档](https://ant.design/)

