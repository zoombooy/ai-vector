import { useState, useEffect } from 'react'
import { Form, Input, Select, InputNumber, Button, Card, message, Space } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import request from '../../utils/request'

const { TextArea } = Input

const AgentForm = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (id) {
      fetchAgentDetail()
    } else {
      form.setFieldsValue({
        agentType: 'PYTHON',
        endpointType: 'HTTP',
        authType: 'NONE',
        status: 1,
        timeout: 30,
        retryTimes: 0,
        maxConcurrent: 10,
        priority: 0,
      })
    }
  }, [id])

  const fetchAgentDetail = async () => {
    setLoading(true)
    try {
      const response: any = await request.get(`/agent/${id}`)
      form.setFieldsValue(response)
    } catch (error) {
      message.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setSubmitting(true)

      if (id) {
        await request.put(`/agent/${id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/agent', values)
        message.success('创建成功')
      }

      navigate('/agent/list')
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请填写完整信息')
      } else {
        message.error(id ? '更新失败' : '创建失败')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <Card title={id ? '编辑Agent' : '新建Agent'} loading={loading}>
        <Form
          form={form}
          layout="vertical"
          style={{ maxWidth: 800 }}
        >
          <Form.Item
            label="Agent名称"
            name="agentName"
            rules={[{ required: true, message: '请输入Agent名称' }]}
          >
            <Input placeholder="请输入Agent名称" />
          </Form.Item>

          <Form.Item
            label="Agent编码"
            name="agentCode"
            rules={[{ required: true, message: '请输入Agent编码' }]}
          >
            <Input placeholder="请输入Agent编码，如：weather_agent" disabled={!!id} />
          </Form.Item>

          <Form.Item
            label="Agent类型"
            name="agentType"
            rules={[{ required: true, message: '请选择Agent类型' }]}
          >
            <Select
              options={[
                { label: 'Python', value: 'PYTHON' },
                { label: 'Java', value: 'JAVA' },
                { label: 'HTTP', value: 'HTTP' },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="描述"
            name="description"
            rules={[{ required: true, message: '请输入描述' }]}
          >
            <TextArea rows={3} placeholder="请输入Agent功能描述" />
          </Form.Item>

          <Form.Item
            label="端点URL"
            name="endpointUrl"
            rules={[{ required: true, message: '请输入端点URL' }]}
          >
            <Input placeholder="http://localhost:5001/execute" />
          </Form.Item>

          <Form.Item
            label="端点类型"
            name="endpointType"
            rules={[{ required: true, message: '请选择端点类型' }]}
          >
            <Select
              options={[
                { label: 'HTTP', value: 'HTTP' },
                { label: 'GRPC', value: 'GRPC' },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="认证类型"
            name="authType"
          >
            <Select
              options={[
                { label: '无认证', value: 'NONE' },
                { label: 'Bearer Token', value: 'BEARER' },
                { label: 'API Key', value: 'API_KEY' },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="认证配置"
            name="authConfig"
            tooltip='JSON格式，如：{"apiKey": "xxx"}'
          >
            <TextArea rows={2} placeholder='{"apiKey": "xxx"}' />
          </Form.Item>

          <Form.Item
            label="输入Schema"
            name="inputSchema"
            tooltip="JSON Schema格式"
          >
            <TextArea rows={4} placeholder='{"type": "object", "properties": {...}}' />
          </Form.Item>

          <Form.Item
            label="输出Schema"
            name="outputSchema"
            tooltip="JSON Schema格式"
          >
            <TextArea rows={4} placeholder='{"type": "object", "properties": {...}}' />
          </Form.Item>

          <Form.Item
            label="能力列表"
            name="capabilities"
            tooltip="JSON数组格式"
          >
            <TextArea rows={2} placeholder='["能力1", "能力2"]' />
          </Form.Item>

          <Form.Item label="超时时间(秒)" name="timeout">
            <InputNumber min={1} max={300} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="重试次数" name="retryTimes">
            <InputNumber min={0} max={5} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="最大并发数" name="maxConcurrent">
            <InputNumber min={1} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="分类" name="category">
            <Input placeholder="如：天气服务、数据分析" />
          </Form.Item>

          <Form.Item label="标签" name="tags">
            <Input placeholder="多个标签用逗号分隔" />
          </Form.Item>

          <Form.Item label="版本" name="version">
            <Input placeholder="如：1.0.0" />
          </Form.Item>

          <Form.Item label="优先级" name="priority">
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="状态" name="status">
            <Select
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSubmit} loading={submitting}>
                {id ? '更新' : '创建'}
              </Button>
              <Button onClick={() => navigate('/agent/list')}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default AgentForm

