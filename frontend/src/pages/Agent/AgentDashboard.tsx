import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Table, Tag, Button } from 'antd'
import { RobotOutlined, CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import request from '../../utils/request'
import type { ColumnsType } from 'antd/es/table'

interface AgentStats {
  totalAgents: number
  activeAgents: number
  totalExecutions: number
  successRate: number
}

interface RecentExecution {
  id: number
  agentCode: string
  agentName: string
  status: string
  executionTime: number
  createTime: string
}

const AgentDashboard = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [stats, setStats] = useState<AgentStats>({
    totalAgents: 0,
    activeAgents: 0,
    totalExecutions: 0,
    successRate: 0,
  })
  const [recentExecutions, setRecentExecutions] = useState<RecentExecution[]>([])

  useEffect(() => {
    fetchStats()
    fetchRecentExecutions()
  }, [])

  const fetchStats = async () => {
    try {
      const agentsResponse: any = await request.get('/agent/page', {
        params: { pageNum: 1, pageSize: 1000 }
      })
      const agents = agentsResponse.records || []
      
      const historyResponse: any = await request.get('/agent/history', {
        params: { pageNum: 1, pageSize: 1000 }
      })
      const history = historyResponse.records || []
      
      const successCount = history.filter((h: any) => h.status === 'SUCCESS').length
      
      setStats({
        totalAgents: agents.length,
        activeAgents: agents.filter((a: any) => a.status === 1).length,
        totalExecutions: history.length,
        successRate: history.length > 0 ? Math.round((successCount / history.length) * 100) : 0,
      })
    } catch (error) {
      console.error('加载统计数据失败', error)
    }
  }

  const fetchRecentExecutions = async () => {
    setLoading(true)
    try {
      const response: any = await request.get('/agent/history', {
        params: { pageNum: 1, pageSize: 10 }
      })
      setRecentExecutions(response.records || [])
    } catch (error) {
      console.error('加载最近执行记录失败', error)
    } finally {
      setLoading(false)
    }
  }

  const columns: ColumnsType<RecentExecution> = [
    {
      title: 'Agent名称',
      dataIndex: 'agentName',
      key: 'agentName',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const colorMap: any = {
          SUCCESS: 'success',
          FAILED: 'error',
          TIMEOUT: 'warning',
        }
        return <Tag color={colorMap[status] || 'default'}>{status}</Tag>
      },
    },
    {
      title: '执行时间(ms)',
      dataIndex: 'executionTime',
      key: 'executionTime',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总Agent数"
              value={stats.totalAgents}
              prefix={<RobotOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="启用Agent数"
              value={stats.activeAgents}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总执行次数"
              value={stats.totalExecutions}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="成功率"
              value={stats.successRate}
              suffix="%"
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: stats.successRate >= 80 ? '#52c41a' : '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="最近执行记录"
        extra={
          <Button type="link" onClick={() => navigate('/agent/history')}>
            查看全部
          </Button>
        }
      >
        <Table
          loading={loading}
          dataSource={recentExecutions}
          columns={columns}
          rowKey="id"
          pagination={false}
        />
      </Card>
    </div>
  )
}

export default AgentDashboard

