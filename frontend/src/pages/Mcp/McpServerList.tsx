import { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, message, Input, Select, Popconfirm, Switch, Card, Tooltip } from 'antd'
import { PlusOutlined, DeleteOutlined, SyncOutlined, EyeOutlined, ApiOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import request from '../../utils/request'
import type { ColumnsType } from 'antd/es/table'

const { Search } = Input

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
}

const McpServerList = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<McpServer[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState<string>()

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize, keyword, category])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await request.get('/mcp/servers', {
        params: { pageNum, pageSize, keyword, category }
      })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/mcp/servers/${id}`)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, checked: boolean) => {
    try {
      await request.put(`/mcp/servers/${id}/status`, null, { params: { status: checked ? 1 : 0 } })
      message.success('状态更新成功')
      fetchData()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleDiscoverTools = async (id: number) => {
    try {
      message.loading({ content: '正在发现工具...', key: 'discover' })
      const tools: any = await request.post(`/mcp/servers/${id}/discover-tools`)
      message.success({ content: `发现 ${tools.length} 个工具`, key: 'discover' })
      fetchData()
    } catch (error) {
      message.error({ content: '发现工具失败', key: 'discover' })
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

  const columns: ColumnsType<McpServer> = [
    { title: 'Server名称', dataIndex: 'serverName', key: 'serverName', width: 150 },
    { title: '编码', dataIndex: 'serverCode', key: 'serverCode', width: 120 },
    { title: '描述', dataIndex: 'description', key: 'description', width: 200, ellipsis: true },
    {
      title: 'MCP URL', dataIndex: 'mcpUrl', key: 'mcpUrl', width: 200, ellipsis: true,
      render: (url: string) => <Tooltip title={url}><span>{url}</span></Tooltip>,
    },
    { title: '类型', dataIndex: 'serverType', key: 'serverType', width: 100, render: (type: string) => <Tag>{type}</Tag> },
    { title: '健康状态', dataIndex: 'healthStatus', key: 'healthStatus', width: 100, render: (status: string) => getHealthStatusTag(status) },
    {
      title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number, record: McpServer) => <Switch checked={status === 1} onChange={(checked) => handleStatusChange(record.id, checked)} />,
    },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/mcp/servers/${record.id}`)}>详情</Button>
          <Button type="link" size="small" icon={<SyncOutlined />} onClick={() => handleDiscoverTools(record.id)}>刷新</Button>
          <Popconfirm title="确定删除?" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Card title={<Space><ApiOutlined /><span>MCP Server管理</span></Space>}
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/mcp/servers/create')}>添加MCP Server</Button>}>
      <Space style={{ marginBottom: 16 }}>
        <Search placeholder="搜索Server名称/编码" allowClear onSearch={(value) => { setKeyword(value); setPageNum(1) }} style={{ width: 250 }} />
        <Select placeholder="选择分类" allowClear style={{ width: 150 }} onChange={(value) => { setCategory(value); setPageNum(1) }}
          options={[{ value: 'search', label: '搜索' }, { value: 'database', label: '数据库' }, { value: 'file', label: '文件' }, { value: 'api', label: 'API' }, { value: 'ai', label: 'AI' }, { value: 'other', label: '其他' }]} />
      </Space>
      <Table columns={columns} dataSource={dataSource} rowKey="id" loading={loading} scroll={{ x: 1200 }}
        pagination={{ current: pageNum, pageSize, total, showSizeChanger: true, showQuickJumper: true, showTotal: (t) => `共 ${t} 条`, onChange: (p, s) => { setPageNum(p); setPageSize(s) } }} />
    </Card>
  )
}

export default McpServerList

