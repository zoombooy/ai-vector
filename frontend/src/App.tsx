import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import DocumentList from './pages/Document/DocumentList'
import DocumentDetail from './pages/Document/DocumentDetail'
import DocumentUpload from './pages/Document/DocumentUpload'
import KnowledgeQA from './pages/KnowledgeQA'
import ModelManagement from './pages/Model/ModelManagement'
import UserManagement from './pages/User/UserManagement'
import RoleManagement from './pages/User/RoleManagement'
import OrganizationManagement from './pages/User/OrganizationManagement'
import FunctionList from './pages/Function/FunctionList'
import FunctionForm from './pages/Function/FunctionForm'
import FunctionDetail from './pages/Function/FunctionDetail'
import AgentList from './pages/Agent/AgentList'
import AgentForm from './pages/Agent/AgentForm'
import AgentDetail from './pages/Agent/AgentDetail'
import AgentExecute from './pages/Agent/AgentExecute'
import AgentHistory from './pages/Agent/AgentHistory'
import AgentDashboard from './pages/Agent/AgentDashboard'
import AgentDebug from './pages/Agent/AgentDebug'
import WorkflowList from './pages/Workflow/WorkflowList'
import WorkflowEditor from './pages/Workflow/WorkflowEditor'
import McpServerList from './pages/Mcp/McpServerList'
import McpServerForm from './pages/Mcp/McpServerForm'
import McpServerDetail from './pages/Mcp/McpServerDetail'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="document">
          <Route path="list" element={<DocumentList />} />
          <Route path="detail/:id" element={<DocumentDetail />} />
          <Route path="upload" element={<DocumentUpload />} />
        </Route>
        <Route path="qa" element={<KnowledgeQA />} />
        <Route path="model" element={<ModelManagement />} />
        <Route path="function">
          <Route path="list" element={<FunctionList />} />
          <Route path="add" element={<FunctionForm />} />
          <Route path="edit/:id" element={<FunctionForm />} />
          <Route path="detail/:id" element={<FunctionDetail />} />
        </Route>
        <Route path="agent">
          <Route index element={<AgentDashboard />} />
          <Route path="list" element={<AgentList />} />
          <Route path="add" element={<AgentForm />} />
          <Route path="edit/:id" element={<AgentForm />} />
          <Route path="detail/:id" element={<AgentDetail />} />
          <Route path="execute/:id" element={<AgentExecute />} />
          <Route path="history" element={<AgentHistory />} />
          <Route path="debug" element={<AgentDebug />} />
        </Route>
        <Route path="workflow">
          <Route path="list" element={<WorkflowList />} />
          <Route path="add" element={<WorkflowEditor />} />
          <Route path="edit/:id" element={<WorkflowEditor />} />
        </Route>
        <Route path="mcp">
          <Route path="servers" element={<McpServerList />} />
          <Route path="servers/create" element={<McpServerForm />} />
          <Route path="servers/:id" element={<McpServerDetail />} />
          <Route path="servers/:id/edit" element={<McpServerForm />} />
        </Route>
        <Route path="user">
          <Route path="list" element={<UserManagement />} />
          <Route path="role" element={<RoleManagement />} />
          <Route path="organization" element={<OrganizationManagement />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App

