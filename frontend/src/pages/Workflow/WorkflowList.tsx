import React, { useState, useEffect } from 'react'
import { Table, Button, Space, message, Popconfirm, Tag, Input, Modal, Form } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import request from '../../utils/request'
import MonacoEditor from '@monaco-editor/react'

const { Search } = Input
const { TextArea } = Input

const WorkflowList: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [executeModalVisible, setExecuteModalVisible] = useState(false)
  const [currentWorkflow, setCurrentWorkflow] = useState<any>(null)
  const [executeInput, setExecuteInput] = useState('{}')
  const [executeResult, setExecuteResult] = useState<any>(null)
  const [executing, setExecuting] = useState(false)

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize, keyword])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await request.get('/workflow/page', {
        params: { pageNum, pageSize, keyword }
      })
      setDataSource(response.list || [])
      setTotal(response.total || 0)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/workflow/${id}`)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleExecute = (record: any) => {
    setCurrentWorkflow(record)
    setExecuteInput('{}')
    setExecuteResult(null)
    setExecuteModalVisible(true)
  }

  const doExecute = async () => {
    try {
      setExecuting(true)
      const input = JSON.parse(executeInput)
      const response: any = await request.post('/workflow/execute', {
        workflowCode: currentWorkflow.workflowCode,
        sessionId: new Date().getTime().toString(),
        input
      })
      setExecuteResult(response)
      if (response.status === 'SUCCESS') {
        message.success('执行成功')
      } else {
        message.error('执行失败: ' + response.errorMessage)
      }
    } catch (error: any) {
      message.error('执行失败: ' + error.message)
    } finally {
      setExecuting(false)
    }
  }

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80
    },
    {
      title: '工作流名称',
      dataIndex: 'workflowName',
      width: 200
    },
    {
      title: '编码',
      dataIndex: 'workflowCode',
      width: 150
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 100
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '版本',
      dataIndex: 'version',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 180
    },
    {
      title: '操作',
      width: 280,
      render: (_: any, record: any) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/workflow/detail/${record.id}`)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/workflow/edit/${record.id}`)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<PlayCircleOutlined />}
            onClick={() => handleExecute(record)}
          >
            执行
          </Button>
          <Popconfirm
            title="确定删除？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Search
          placeholder="搜索工作流名称、编码或描述"
          allowClear
          style={{ width: 300 }}
          onSearch={setKeyword}
        />
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/workflow/add')}
        >
          创建工作流
        </Button>
      </div>

      <Table
        loading={loading}
        dataSource={dataSource}
        columns={columns}
        rowKey="id"
        pagination={{
          current: pageNum,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, size) => {
            setPageNum(page)
            setPageSize(size)
          }
        }}
      />

      <Modal
        title={`执行工作流: ${currentWorkflow?.workflowName || ''}`}
        open={executeModalVisible}
        onCancel={() => setExecuteModalVisible(false)}
        width={800}
        footer={[
          <Button key="cancel" onClick={() => setExecuteModalVisible(false)}>
            关闭
          </Button>,
          <Button key="execute" type="primary" loading={executing} onClick={doExecute}>
            执行
          </Button>
        ]}
      >
        <div style={{ marginBottom: 16 }}>
          <div style={{ marginBottom: 8 }}>输入参数（JSON格式）：</div>
          <MonacoEditor
            height="200px"
            language="json"
            value={executeInput}
            onChange={(value) => setExecuteInput(value || '{}')}
            options={{
              minimap: { enabled: false },
              fontSize: 14
            }}
          />
        </div>

        {executeResult && (
          <div>
            <div style={{ marginBottom: 8 }}>执行结果：</div>
            <div style={{
              padding: 12,
              background: executeResult.status === 'SUCCESS' ? '#f6ffed' : '#fff2f0',
              border: `1px solid ${executeResult.status === 'SUCCESS' ? '#b7eb8f' : '#ffccc7'}`,
              borderRadius: 4
            }}>
              <div><strong>状态：</strong>{executeResult.status}</div>
              {executeResult.errorMessage && (
                <div style={{ color: '#ff4d4f' }}>
                  <strong>错误：</strong>{executeResult.errorMessage}
                </div>
              )}
              {executeResult.output && (
                <div>
                  <strong>输出：</strong>
                  <pre style={{ marginTop: 8, background: '#fff', padding: 8, borderRadius: 4 }}>
                    {JSON.stringify(executeResult.output, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default WorkflowList

