import { useState, useEffect } from 'react'
import { Table, Card, Tag, Input, Select, Space, Button, Modal } from 'antd'
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons'
import request from '../../utils/request'
import type { ColumnsType } from 'antd/es/table'

const { Search } = Input

interface ExecutionHistory {
  id: number
  agentCode: string
  agentName: string
  sessionId: string
  userId: number
  inputData: string
  outputData: string
  status: string
  errorMessage: string
  executionTime: number
  startTime: string
  endTime: string
  createTime: string
}

const AgentHistory = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<ExecutionHistory[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [agentCode, setAgentCode] = useState<string>()
  const [sessionId, setSessionId] = useState<string>()
  const [status, setStatus] = useState<string>()
  const [detailVisible, setDetailVisible] = useState(false)
  const [selectedRecord, setSelectedRecord] = useState<ExecutionHistory | null>(null)

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize, agentCode, sessionId, status])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await request.get('/agent/history', {
        params: { pageNum, pageSize, agentCode, sessionId, status }
      })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      console.error('加载失败', error)
    } finally {
      setLoading(false)
    }
  }

  const showDetail = (record: ExecutionHistory) => {
    setSelectedRecord(record)
    setDetailVisible(true)
  }

  const formatJson = (jsonStr: string) => {
    try {
      return JSON.stringify(JSON.parse(jsonStr), null, 2)
    } catch {
      return jsonStr
    }
  }

  const columns: ColumnsType<ExecutionHistory> = [
    {
      title: '执行ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Agent编码',
      dataIndex: 'agentCode',
      key: 'agentCode',
      width: 150,
    },
    {
      title: 'Agent名称',
      dataIndex: 'agentName',
      key: 'agentName',
      width: 150,
    },
    {
      title: '会话ID',
      dataIndex: 'sessionId',
      key: 'sessionId',
      width: 200,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const colorMap: any = {
          SUCCESS: 'success',
          FAILED: 'error',
          TIMEOUT: 'warning',
          RUNNING: 'processing',
        }
        return <Tag color={colorMap[status] || 'default'}>{status}</Tag>
      },
    },
    {
      title: '执行时间(ms)',
      dataIndex: 'executionTime',
      key: 'executionTime',
      width: 120,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      fixed: 'right',
      render: (_: any, record: ExecutionHistory) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => showDetail(record)}
        >
          详情
        </Button>
      ),
    },
  ]

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <Card
        title="执行历史"
        extra={
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchData}
          >
            刷新
          </Button>
        }
      >
        <div style={{ marginBottom: 16 }}>
          <Space>
            <Search
              placeholder="Agent编码"
              allowClear
              style={{ width: 200 }}
              onSearch={setAgentCode}
            />
            <Search
              placeholder="会话ID"
              allowClear
              style={{ width: 200 }}
              onSearch={setSessionId}
            />
            <Select
              placeholder="状态"
              allowClear
              style={{ width: 150 }}
              onChange={setStatus}
              options={[
                { label: '成功', value: 'SUCCESS' },
                { label: '失败', value: 'FAILED' },
                { label: '超时', value: 'TIMEOUT' },
                { label: '运行中', value: 'RUNNING' },
              ]}
            />
          </Space>
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
      </Card>

      <Modal
        title="执行详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {selectedRecord && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <strong>执行ID：</strong>{selectedRecord.id}
              <Tag color="blue" style={{ marginLeft: 8 }}>{selectedRecord.status}</Tag>
            </div>
            <div style={{ marginBottom: 16 }}>
              <strong>Agent：</strong>{selectedRecord.agentName} ({selectedRecord.agentCode})
            </div>
            <div style={{ marginBottom: 16 }}>
              <strong>执行时间：</strong>{selectedRecord.executionTime}ms
            </div>
            <div style={{ marginBottom: 16 }}>
              <strong>开始时间：</strong>{selectedRecord.startTime}
            </div>
            <div style={{ marginBottom: 16 }}>
              <strong>结束时间：</strong>{selectedRecord.endTime}
            </div>
            
            <div style={{ marginBottom: 8 }}><strong>输入数据：</strong></div>
            <pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, overflow: 'auto', marginBottom: 16 }}>
              {selectedRecord.inputData && formatJson(selectedRecord.inputData)}
            </pre>

            {selectedRecord.status === 'SUCCESS' ? (
              <>
                <div style={{ marginBottom: 8 }}><strong>输出数据：</strong></div>
                <pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, overflow: 'auto' }}>
                  {selectedRecord.outputData && formatJson(selectedRecord.outputData)}
                </pre>
              </>
            ) : (
              <>
                <div style={{ marginBottom: 8 }}><strong>错误信息：</strong></div>
                <pre style={{ background: '#fff2f0', padding: 12, borderRadius: 4, color: '#cf1322' }}>
                  {selectedRecord.errorMessage}
                </pre>
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default AgentHistory

