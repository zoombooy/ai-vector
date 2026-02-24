import { useState, useEffect } from 'react'
import { Form, Input, Button, Card, Select, InputNumber, message, Space, Switch } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import request from '../../utils/request'

const { TextArea } = Input

interface McpServerFormData {
  serverName: string
  serverCode: string
  description: string
  mcpUrl: string
  serverType: string
  authType: string
  authConfig: string
  envConfig: string
  source: string
  category: string
  tags: string
  priority: number
  timeout: number
  status: number
}

const McpServerForm = () => {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const isEdit = !!id

  useEffect(() => {
    if (isEdit) {
      fetchServerDetail()
    }
  }, [id])

  const fetchServerDetail = async () => {
    setLoading(true)
    try {
      const response: any = await request.get(`/mcp/servers/${id}`)
      form.setFieldsValue(response)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (values: McpServerFormData) => {
    setSubmitting(true)
    try {
      if (isEdit) {
        await request.put(`/mcp/servers/${id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/mcp/servers', values)
        message.success('创建成功')
      }
      navigate('/mcp/servers')
    } catch (error) {
      message.error(isEdit ? '更新失败' : '创建失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title={isEdit ? '编辑MCP Server' : '添加MCP Server'} loading={loading}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} style={{ maxWidth: 800 }}
        initialValues={{ serverType: 'HTTP_STREAM', authType: 'NONE', source: 'MANUAL', category: 'other', priority: 0, timeout: 30000, status: 1 }}>
        <Form.Item name="serverName" label="Server名称" rules={[{ required: true, message: '请输入Server名称' }]}>
          <Input placeholder="请输入Server名称" />
        </Form.Item>
        <Form.Item name="serverCode" label="Server编码" rules={[{ required: true, message: '请输入Server编码' }, { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '编码必须以字母开头，只能包含字母、数字和下划线' }]}>
          <Input placeholder="请输入Server编码（唯一标识）" disabled={isEdit} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <TextArea rows={3} placeholder="请输入描述" />
        </Form.Item>
        <Form.Item name="mcpUrl" label="MCP URL" rules={[{ required: true, message: '请输入MCP URL' }, { type: 'url', message: '请输入有效的URL' }]}>
          <Input placeholder="例如: https://mcp-server.example.com/mcp" />
        </Form.Item>
        <Form.Item name="serverType" label="Server类型" rules={[{ required: true }]}>
          <Select options={[{ value: 'HTTP_STREAM', label: 'HTTP Stream' }, { value: 'STDIO', label: 'STDIO' }, { value: 'SSE', label: 'SSE' }]} />
        </Form.Item>
        <Form.Item name="authType" label="认证类型" rules={[{ required: true }]}>
          <Select options={[{ value: 'NONE', label: '无认证' }, { value: 'BEARER', label: 'Bearer Token' }, { value: 'API_KEY', label: 'API Key' }]} />
        </Form.Item>
        <Form.Item name="authConfig" label="认证配置" tooltip="JSON格式，如: {&quot;token&quot;: &quot;xxx&quot;} 或 {&quot;apiKey&quot;: &quot;xxx&quot;}">
          <TextArea rows={2} placeholder='{"token": "your-token"} 或 {"apiKey": "your-api-key"}' />
        </Form.Item>
        <Form.Item name="envConfig" label="环境变量配置" tooltip="JSON格式的环境变量配置">
          <TextArea rows={2} placeholder='{"ENV_VAR": "value"}' />
        </Form.Item>
        <Form.Item name="category" label="分类">
          <Select options={[{ value: 'search', label: '搜索' }, { value: 'database', label: '数据库' }, { value: 'file', label: '文件' }, { value: 'api', label: 'API' }, { value: 'ai', label: 'AI' }, { value: 'other', label: '其他' }]} />
        </Form.Item>
        <Form.Item name="tags" label="标签" tooltip="多个标签用逗号分隔">
          <Input placeholder="标签1,标签2,标签3" />
        </Form.Item>
        <Form.Item name="priority" label="优先级">
          <InputNumber min={0} max={100} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="timeout" label="超时时间(ms)">
          <InputNumber min={1000} max={300000} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="status" label="状态" valuePropName="checked" getValueFromEvent={(checked) => checked ? 1 : 0} getValueProps={(value) => ({ checked: value === 1 })}>
          <Switch checkedChildren="启用" unCheckedChildren="禁用" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>{isEdit ? '更新' : '创建'}</Button>
            <Button onClick={() => navigate('/mcp/servers')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  )
}

export default McpServerForm

