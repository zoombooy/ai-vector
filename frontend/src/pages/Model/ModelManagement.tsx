import { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  message,
  Tag,
  Popconfirm,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../../utils/request'

const { Option } = Select
const { TextArea } = Input

interface ModelConfig {
  id?: number
  configId?: number
  modelName: string
  modelCode: string
  modelType: string
  modelVersion?: string
  provider?: string
  status: number
  description?: string
  apiUrl: string
  apiKey: string
  maxTokens?: number
  temperature?: number
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
  timeout?: number
  retryTimes?: number
  configJson?: string
  createTime?: string
  updateTime?: string
}

const ModelManagement = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<ModelConfig[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('') // 用于输入框的临时值
  const [modelType, setModelType] = useState<string | undefined>()
  const [modalVisible, setModalVisible] = useState(false)
  const [editingModel, setEditingModel] = useState<ModelConfig | null>(null)
  const [form] = Form.useForm()

  // 模型类型选项
  const modelTypes = [
    { label: 'OpenAI', value: 'openai' },
    { label: '百度文心', value: 'baidu' },
    { label: '讯飞星火', value: 'xunfei' },
    { label: '阿里通义', value: 'aliyun' },
    { label: '腾讯混元', value: 'tencent' },
    { label: '智谱AI', value: 'zhipu' },
    { label: '其他', value: 'other' },
  ]

  // 初始化和依赖变化时加载数据
  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize, keyword, modelType])

  const fetchData = async () => {
    setLoading(true)
    try {
      const params: any = { pageNum, pageSize }
      if (keyword) params.keyword = keyword
      if (modelType) params.modelType = modelType

      const response: any = await request.get('/model/page', { params })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      message.error('获取模型列表失败')
    } finally {
      setLoading(false)
    }
  }

  // 处理搜索
  const handleSearch = () => {
    setKeyword(searchKeyword)
    setPageNum(1)
  }

  // 处理重置
  const handleReset = () => {
    setSearchKeyword('')
    setKeyword('')
    setModelType(undefined)
    setPageNum(1)
  }

  const handleAdd = () => {
    setEditingModel(null)
    form.resetFields()
    form.setFieldsValue({
      status: 1,
      timeout: 30,
      retryTimes: 3,
      temperature: 0.7,
      topP: 1.0,
    })
    setModalVisible(true)
  }

  const handleEdit = async (record: ModelConfig) => {
    try {
      // request 拦截器已经返回了 data，直接使用
      const response: any = await request.get(`/model/${record.id}`)
      setEditingModel(response)
      form.setFieldsValue(response)
      setModalVisible(true)
    } catch (error) {
      message.error('获取模型详情失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/model/${id}`)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (id: number, status: number) => {
    try {
      await request.put(`/model/${id}/status`, null, {
        params: { status: status ? 1 : 0 },
      })
      message.success('状态更新成功')
      fetchData()
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data = {
        ...values,
        id: editingModel?.id,
        configId: editingModel?.configId,
      }

      if (editingModel) {
        await request.put('/model', data)
      } else {
        await request.post('/model', data)
      }

      message.success(editingModel ? '更新成功' : '创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请填写完整信息')
      } else {
        message.error(editingModel ? '更新失败' : '创建失败')
      }
    }
  }

  const columns = [
    {
      title: '模型名称',
      dataIndex: 'modelName',
      key: 'modelName',
      width: 150,
    },
    {
      title: '模型编码',
      dataIndex: 'modelCode',
      key: 'modelCode',
      width: 120,
    },
    {
      title: '模型类型',
      dataIndex: 'modelType',
      key: 'modelType',
      width: 100,
      render: (type: string) => {
        const typeObj = modelTypes.find((t) => t.value === type)
        return typeObj ? typeObj.label : type
      },
    },
    {
      title: '版本',
      dataIndex: 'modelVersion',
      key: 'modelVersion',
      width: 100,
    },
    {
      title: '提供商',
      dataIndex: 'provider',
      key: 'provider',
      width: 100,
    },
    {
      title: 'API地址',
      dataIndex: 'apiUrl',
      key: 'apiUrl',
      width: 200,
      ellipsis: true,
    },
    {
      title: 'API密钥',
      dataIndex: 'apiKey',
      key: 'apiKey',
      width: 150,
      render: (key: string) => <span style={{ fontFamily: 'monospace' }}>{key}</span>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number, record: ModelConfig) => (
        <Switch
          checked={status === 1}
          onChange={(checked) => handleStatusChange(record.id!, checked ? 1 : 0)}
          checkedChildren="启用"
          unCheckedChildren="禁用"
        />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: ModelConfig) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个模型吗？"
            onConfirm={() => handleDelete(record.id!)}
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
    <div>
      <h1 style={{ marginBottom: 24 }}>模型管理</h1>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索模型名称或编码"
            allowClear
            style={{ width: 250 }}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
          />
          <Select
            placeholder="选择模型类型"
            allowClear
            style={{ width: 150 }}
            value={modelType}
            onChange={(value) => {
              setModelType(value)
              setPageNum(1)
            }}
          >
            {modelTypes.map((type) => (
              <Option key={type.value} value={type.value}>
                {type.label}
              </Option>
            ))}
          </Select>
          <Button type="primary" onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增模型
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1500 }}
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
        title={editingModel ? '编辑模型' : '新增模型'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={800}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="模型名称"
            name="modelName"
            rules={[{ required: true, message: '请输入模型名称' }]}
          >
            <Input placeholder="例如：GPT-4" />
          </Form.Item>

          <Form.Item
            label="模型编码"
            name="modelCode"
            rules={[{ required: true, message: '请输入模型编码' }]}
          >
            <Input placeholder="例如：gpt-4" disabled={!!editingModel} />
          </Form.Item>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item
              label="模型类型"
              name="modelType"
              rules={[{ required: true, message: '请选择模型类型' }]}
              style={{ width: 200 }}
            >
              <Select placeholder="选择类型">
                {modelTypes.map((type) => (
                  <Option key={type.value} value={type.value}>
                    {type.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item label="模型版本" name="modelVersion" style={{ width: 200 }}>
              <Input placeholder="例如：v1.0" />
            </Form.Item>

            <Form.Item label="提供商" name="provider" style={{ width: 200 }}>
              <Input placeholder="例如：OpenAI" />
            </Form.Item>
          </Space>

          <Form.Item
            label="API地址"
            name="apiUrl"
            rules={[{ required: true, message: '请输入API地址' }]}
          >
            <Input placeholder="https://api.openai.com/v1/chat/completions" />
          </Form.Item>

          <Form.Item
            label="API密钥"
            name="apiKey"
            rules={[{ required: true, message: '请输入API密钥' }]}
          >
            <Input.Password placeholder="sk-..." />
          </Form.Item>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item label="最大Tokens" name="maxTokens" style={{ width: 150 }}>
              <InputNumber min={1} max={100000} placeholder="4096" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item label="温度参数" name="temperature" style={{ width: 150 }}>
              <InputNumber min={0} max={2} step={0.1} placeholder="0.7" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item label="Top P" name="topP" style={{ width: 150 }}>
              <InputNumber min={0} max={1} step={0.1} placeholder="1.0" style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item label="超时时间(秒)" name="timeout" style={{ width: 150 }}>
              <InputNumber min={1} max={300} placeholder="30" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item label="重试次数" name="retryTimes" style={{ width: 150 }}>
              <InputNumber min={0} max={10} placeholder="3" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item
              label="状态"
              name="status"
              valuePropName="checked"
              style={{ width: 150 }}
            >
              <Switch checkedChildren="启用" unCheckedChildren="禁用" />
            </Form.Item>
          </Space>

          <Form.Item label="描述" name="description">
            <TextArea rows={3} placeholder="模型描述信息" />
          </Form.Item>

          <Form.Item label="其他配置(JSON)" name="configJson">
            <TextArea rows={4} placeholder='{"key": "value"}' />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ModelManagement

