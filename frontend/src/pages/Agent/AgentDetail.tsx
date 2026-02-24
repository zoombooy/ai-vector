import { useState, useEffect } from 'react'
import { Card, Descriptions, Tag, Button, Space, message, Spin, Empty } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeftOutlined, EditOutlined, PlayCircleOutlined } from '@ant-design/icons'
import request from '../../utils/request'
import { formatJsonField, parseCapabilities, parseTags } from '../../utils/agentHelper'

interface AgentDetail {
  id: number
  agentName: string
  agentCode: string
  agentType: string
  description: string
  endpointUrl: string
  endpointType: string
  authType: string
  authConfig: string
  inputSchema: string
  outputSchema: string
  capabilities: string
  timeout: number
  retryTimes: number
  maxConcurrent: number
  status: number
  category: string
  tags: string
  version: string
  priority: number
  createTime: string
  updateTime: string
}

const AgentDetail = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [loading, setLoading] = useState(false)
  const [agent, setAgent] = useState<AgentDetail | null>(null)

  useEffect(() => {
    fetchDetail()
  }, [id])

  const fetchDetail = async () => {
    setLoading(true)
    try {
      const response: any = await request.get(`/agent/${id}`)
      setAgent(response)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }



  if (loading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" tip="加载Agent详情..." />
      </div>
    )
  }

  if (!agent) {
    return (
      <div style={{ padding: 24 }}>
        <Card>
          <Empty description="Agent不存在">
            <Button type="primary" onClick={() => navigate('/agent/list')}>返回列表</Button>
          </Empty>
        </Card>
      </div>
    )
  }

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/agent/list')}
            />
            <span>Agent详情</span>
          </Space>
        }
        extra={
          <Space>
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              onClick={() => navigate(`/agent/execute/${id}`)}
            >
              执行
            </Button>
            <Button
              icon={<EditOutlined />}
              onClick={() => navigate(`/agent/edit/${id}`)}
            >
              编辑
            </Button>
          </Space>
        }
      >
        <Descriptions bordered column={2}>
          <Descriptions.Item label="Agent名称">{agent.agentName}</Descriptions.Item>
          <Descriptions.Item label="Agent编码">{agent.agentCode}</Descriptions.Item>
          <Descriptions.Item label="Agent类型">
            <Tag color={agent.agentType === 'PYTHON' ? 'blue' : 'green'}>
              {agent.agentType}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={agent.status === 1 ? 'success' : 'error'}>
              {agent.status === 1 ? '启用' : '禁用'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="描述" span={2}>
            {agent.description}
          </Descriptions.Item>
          <Descriptions.Item label="端点URL" span={2}>
            {agent.endpointUrl}
          </Descriptions.Item>
          <Descriptions.Item label="端点类型">{agent.endpointType}</Descriptions.Item>
          <Descriptions.Item label="认证类型">{agent.authType}</Descriptions.Item>
          <Descriptions.Item label="分类">{agent.category}</Descriptions.Item>
          <Descriptions.Item label="版本">{agent.version}</Descriptions.Item>
          <Descriptions.Item label="超时时间">{agent.timeout}秒</Descriptions.Item>
          <Descriptions.Item label="重试次数">{agent.retryTimes}</Descriptions.Item>
          <Descriptions.Item label="最大并发">{agent.maxConcurrent}</Descriptions.Item>
          <Descriptions.Item label="优先级">{agent.priority}</Descriptions.Item>
          <Descriptions.Item label="标签" span={2}>
            {parseTags(agent.tags).map((tag, index) => (
              <Tag key={index}>{tag}</Tag>
            ))}
          </Descriptions.Item>
          <Descriptions.Item label="能力列表" span={2}>
            {parseCapabilities(agent.capabilities).map((cap, index) => (
              <Tag key={index} color="blue">{cap}</Tag>
            ))}
          </Descriptions.Item>
          <Descriptions.Item label="输入Schema" span={2}>
            <pre style={{ background: '#f5f5f5', padding: 8, borderRadius: 4, overflow: 'auto' }}>
              {formatJsonField(agent.inputSchema)}
            </pre>
          </Descriptions.Item>
          <Descriptions.Item label="输出Schema" span={2}>
            <pre style={{ background: '#f5f5f5', padding: 8, borderRadius: 4, overflow: 'auto' }}>
              {formatJsonField(agent.outputSchema)}
            </pre>
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">{agent.createTime}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{agent.updateTime}</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  )
}

export default AgentDetail

