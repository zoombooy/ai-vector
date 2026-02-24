import { useState, useEffect } from 'react'
import { Card, Descriptions, Tag, Button, Space, Table, message, Switch, Spin, Tabs, Modal, Input } from 'antd'
import { EditOutlined, SyncOutlined, ArrowLeftOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import request from '../../utils/request'
import type { ColumnsType } from 'antd/es/table'

const { TextArea } = Input

interface McpServer {
  id: number
  serverName: string
  serverCode: string
  description: string
  mcpUrl: string
  serverType: string
  authType: string
  source: string
  category: string
  tags: string
  priority: number
  status: number
  timeout: number
  healthStatus: string
  lastHealthCheck: string
  createTime: string
  updateTime: string
  tools?: McpTool[]
}

interface McpTool {
  id: number
  toolName: string
  toolCode: string
  description: string
  inputSchema: string
  enabled: number
}

const McpServerDetail = () => {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [loading, setLoading] = useState(false)
  const [server, setServer] = useState<McpServer | null>(null)
  const [testModalVisible, setTestModalVisible] = useState(false)
  const [selectedTool, setSelectedTool] = useState<McpTool | null>(null)
  const [testArgs, setTestArgs] = useState('')
  const [testResult, setTestResult] = useState('')
  const [testing, setTesting] = useState(false)

  useEffect(() => {
    fetchServerDetail()
  }, [id])

  const fetchServerDetail = async () => {
    setLoading(true)
    try {
      const response: any = await request.get(`/mcp/servers/${id}`)
      setServer(response)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  const handleDiscoverTools = async () => {
    try {
      message.loading({ content: '正在发现工具...', key: 'discover' })
      await request.post(`/mcp/servers/${id}/discover-tools`)
      message.success({ content: '工具发现完成', key: 'discover' })
      fetchServerDetail()
    } catch (error) {
      message.error({ content: '发现工具失败', key: 'discover' })
    }
  }

  const handleToolStatusChange = async (toolId: number, enabled: boolean) => {
    try {
      await request.put(`/mcp/tools/${toolId}/status`, null, { params: { enabled: enabled ? 1 : 0 } })
      message.success('状态更新成功')
      fetchServerDetail()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleTestTool = (tool: McpTool) => {
    setSelectedTool(tool)
    setTestArgs('{}')
    setTestResult('')
    setTestModalVisible(true)
  }

  const executeTest = async () => {
    if (!selectedTool || !server) return
    setTesting(true)
    try {
      const args = JSON.parse(testArgs)
      const response: any = await request.post('/mcp/call-tool', {
        serverCode: server.serverCode,
        toolName: selectedTool.toolName,
        arguments: args,
      })
      setTestResult(JSON.stringify(response, null, 2))
    } catch (error: any) {
      setTestResult(`错误: ${error.message}`)
    } finally {
      setTesting(false)
    }
  }

  const getHealthStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      'HEALTHY': { color: 'green', text: '健康' },
      'UNHEALTHY': { color: 'red', text: '异常' },
      'UNKNOWN': { color: 'default', text: '未知' },
    }
    const config = statusMap[status] || statusMap['UNKNOWN']
    return <Tag color={config.color}>{config.text}</Tag>
  }

  const toolColumns: ColumnsType<McpTool> = [
    { title: '工具名称', dataIndex: 'toolName', key: 'toolName', width: 150 },
    { title: '工具编码', dataIndex: 'toolCode', key: 'toolCode', width: 150 },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '启用', dataIndex: 'enabled', key: 'enabled', width: 80,
      render: (enabled: number, record: McpTool) => <Switch checked={enabled === 1} onChange={(checked) => handleToolStatusChange(record.id, checked)} />,
    },
    {
      title: '操作', key: 'action', width: 100,
      render: (_, record) => <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={() => handleTestTool(record)}>测试</Button>,
    },
  ]

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  if (!server) return <div>Server不存在</div>

  return (
    <>
      <Card title={<Space><ArrowLeftOutlined onClick={() => navigate('/mcp/servers')} style={{ cursor: 'pointer' }} /><span>{server.serverName}</span></Space>}
        extra={<Space><Button icon={<EditOutlined />} onClick={() => navigate(`/mcp/servers/${id}/edit`)}>编辑</Button><Button icon={<SyncOutlined />} onClick={handleDiscoverTools}>刷新工具</Button></Space>}>
        <Tabs items={[
          { key: 'info', label: '基本信息', children: (
            <Descriptions bordered column={2}>
              <Descriptions.Item label="Server名称">{server.serverName}</Descriptions.Item>
              <Descriptions.Item label="Server编码">{server.serverCode}</Descriptions.Item>
              <Descriptions.Item label="MCP URL" span={2}>{server.mcpUrl}</Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>{server.description || '-'}</Descriptions.Item>
              <Descriptions.Item label="类型"><Tag>{server.serverType}</Tag></Descriptions.Item>
              <Descriptions.Item label="认证类型"><Tag>{server.authType}</Tag></Descriptions.Item>
              <Descriptions.Item label="健康状态">{getHealthStatusTag(server.healthStatus)}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={server.status === 1 ? 'green' : 'default'}>{server.status === 1 ? '启用' : '禁用'}</Tag></Descriptions.Item>
              <Descriptions.Item label="分类">{server.category || '-'}</Descriptions.Item>
              <Descriptions.Item label="标签">{server.tags || '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{server.createTime}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{server.updateTime}</Descriptions.Item>
            </Descriptions>
          )},
          { key: 'tools', label: `工具列表 (${server.tools?.length || 0})`, children: <Table columns={toolColumns} dataSource={server.tools || []} rowKey="id" pagination={false} /> },
        ]} />
      </Card>
      <Modal title={`测试工具: ${selectedTool?.toolName}`} open={testModalVisible} onCancel={() => setTestModalVisible(false)} footer={null} width={700}>
        <div style={{ marginBottom: 16 }}><strong>输入参数 (JSON):</strong><TextArea rows={4} value={testArgs} onChange={(e) => setTestArgs(e.target.value)} placeholder='{"param1": "value1"}' /></div>
        <Button type="primary" onClick={executeTest} loading={testing} style={{ marginBottom: 16 }}>执行测试</Button>
        {testResult && <div><strong>执行结果:</strong><pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, maxHeight: 300, overflow: 'auto' }}>{testResult}</pre></div>}
      </Modal>
    </>
  )
}

export default McpServerDetail

