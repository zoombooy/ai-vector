import { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Modal, message, Input, Select, Popconfirm, Switch } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import request from '../../utils/request'
import type { ColumnsType } from 'antd/es/table'

const { Search } = Input

interface Agent {
  id: number
  agentName: string
  agentCode: string
  agentType: string
  description: string
  endpointUrl: string
  status: number
  category: string
  version: string
  timeout: number
  createTime: string
  updateTime: string
}

const AgentList = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<Agent[]>([])
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
      const response: any = await request.get('/agent/page', {
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
      await request.delete(`/agent/${id}`)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, checked: boolean) => {
    try {
      await request.put(`/agent/${id}`, { status: checked ? 1 : 0 })
      message.success('状态更新成功')
      fetchData()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const columns: ColumnsType<Agent> = [
    {
      title: 'Agent名称',
      dataIndex: 'agentName',
      key: 'agentName',
      width: 150,
    },
    {
      title: 'Agent编码',
      dataIndex: 'agentCode',
      key: 'agentCode',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'agentType',
      key: 'agentType',
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'PYTHON' ? 'blue' : 'green'}>{type}</Tag>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120,
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
    },
    {
      title: '超时(秒)',
      dataIndex: 'timeout',
      key: 'timeout',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number, record: Agent) => (
        <Switch
          checked={status === 1}
          onChange={(checked) => handleStatusChange(record.id, checked)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_: any, record: Agent) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/agent/detail/${record.id}`)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<PlayCircleOutlined />}
            onClick={() => navigate(`/agent/execute/${record.id}`)}
          >
            执行
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/agent/edit/${record.id}`)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此Agent吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Search
            placeholder="搜索Agent名称或编码"
            allowClear
            style={{ width: 300 }}
            onSearch={setKeyword}
          />
          <Select
            placeholder="选择分类"
            allowClear
            style={{ width: 150 }}
            onChange={setCategory}
            options={[
              { label: '天气服务', value: '天气服务' },
              { label: '数据分析', value: '数据分析' },
              { label: '自定义', value: '自定义' },
            ]}
          />
        </Space>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/agent/add')}
        >
          新建Agent
        </Button>
      </div>

      <Table
        loading={loading}
        dataSource={dataSource}
        columns={columns}
        rowKey="id"
        scroll={{ x: 1200 }}
        pagination={{
          current: pageNum,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, size) => {
            setPageNum(page)
            setPageSize(size)
          },
        }}
      />
    </div>
  )
}

export default AgentList

