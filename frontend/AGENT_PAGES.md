# Agent前端页面说明

## 已创建的页面

### 1. Agent概览 (`/agent`)
- 文件: `src/pages/Agent/AgentDashboard.tsx`
- 功能: 展示Agent统计信息和最近执行记录
- 统计卡片: 总Agent数、启用Agent数、总执行次数、成功率

### 2. Agent列表 (`/agent/list`)
- 文件: `src/pages/Agent/AgentList.tsx`
- 功能: 分页展示所有Agent，支持搜索和筛选
- 操作: 新建、编辑、删除、查看详情、执行、启用/禁用

### 3. Agent表单 (`/agent/add`, `/agent/edit/:id`)
- 文件: `src/pages/Agent/AgentForm.tsx`
- 功能: 创建和编辑Agent
- 字段: 名称、编码、类型、描述、端点URL、认证配置、Schema等

### 4. Agent详情 (`/agent/detail/:id`)
- 文件: `src/pages/Agent/AgentDetail.tsx`
- 功能: 查看Agent的完整信息
- 展示: 基本信息、配置、Schema、能力列表等

### 5. Agent执行 (`/agent/execute/:id`)
- 文件: `src/pages/Agent/AgentExecute.tsx`
- 功能: 执行指定的Agent
- 特性: JSON编辑器、实时执行、结果展示

### 6. 执行历史 (`/agent/history`)
- 文件: `src/pages/Agent/AgentHistory.tsx`
- 功能: 查看所有Agent的执行历史
- 筛选: Agent编码、会话ID、状态
- 详情: 输入数据、输出数据、错误信息

### 7. Agent选择器组件
- 文件: `src/components/AgentSelector/index.tsx`
- 功能: 弹窗式Agent选择和执行组件
- 用途: 可在其他页面中集成Agent调用功能

## 菜单结构

```
Agent管理
├── Agent概览
├── Agent列表
└── 执行历史
```

## 路由配置

已在 `src/App.tsx` 中配置以下路由:

```typescript
<Route path="agent">
  <Route index element={<AgentDashboard />} />
  <Route path="list" element={<AgentList />} />
  <Route path="add" element={<AgentForm />} />
  <Route path="edit/:id" element={<AgentForm />} />
  <Route path="detail/:id" element={<AgentDetail />} />
  <Route path="execute/:id" element={<AgentExecute />} />
  <Route path="history" element={<AgentHistory />} />
</Route>
```

## API接口

所有页面通过 `src/utils/request.ts` 调用以下API:

- `GET /agent/page` - 分页查询Agent
- `GET /agent/{id}` - 获取Agent详情
- `GET /agent/code/{code}` - 根据编码获取Agent
- `POST /agent` - 创建Agent
- `PUT /agent/{id}` - 更新Agent
- `DELETE /agent/{id}` - 删除Agent
- `POST /agent/execute` - 执行Agent
- `GET /agent/available` - 获取可用Agent列表
- `GET /agent/history` - 获取执行历史

## 依赖

已添加到 `package.json`:

```json
{
  "@monaco-editor/react": "^4.6.0"
}
```

## 安装和运行

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:3000 启动

## 特性

1. **Monaco Editor**: 用于JSON输入编辑，提供语法高亮和格式化
2. **实时搜索**: Agent列表支持实时搜索和筛选
3. **状态管理**: 支持启用/禁用Agent
4. **执行历史**: 完整的执行记录追踪
5. **响应式设计**: 适配不同屏幕尺寸
6. **错误处理**: 完善的错误提示和处理

## 页面截图说明

### Agent概览
- 4个统计卡片
- 最近执行记录表格

### Agent列表
- 搜索框和筛选器
- 表格展示所有Agent
- 操作按钮: 详情、执行、编辑、删除

### Agent表单
- 完整的表单字段
- JSON Schema编辑
- 验证和提交

### Agent执行
- Agent信息展示
- Monaco JSON编辑器
- 执行结果展示

### 执行历史
- 筛选条件
- 执行记录表格
- 详情弹窗

