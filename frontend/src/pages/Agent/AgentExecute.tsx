import { useState, useEffect } from 'react'
import { Card, Form, Button, Space, message, Spin, Descriptions, Tag, Alert, Empty } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeftOutlined, PlayCircleOutlined } from '@ant-design/icons'
import MonacoEditor from '@monaco-editor/react'
import request from '../../utils/request'
import { formatJsonField, generateExampleFromSchema } from '../../utils/agentHelper'

interface Agent {
  id: number
  agentName: string
  agentCode: string
  agentType: string
  description: string
  inputSchema: string
  outputSchema: string
}

const AgentExecute = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [executing, setExecuting] = useState(false)
  const [agent, setAgent] = useState<Agent | null>(null)
  const [inputJson, setInputJson] = useState('{\n  \n}')
  const [result, setResult] = useState<any>(null)
  const [error, setError] = useState<string>('')

  useEffect(() => {
    fetchAgent()
  }, [id])

  const fetchAgent = async () => {
    setLoading(true)
    setError('')
    try {
      const response: any = await request.get(`/agent/${id}`)
      console.log('Agent data received:', response)

      if (!response) {
        setError('Agent不存在')
        message.error('Agent不存在')
        return
      }

      setAgent(response)

      if (response.inputSchema) {
        try {
          const example = generateExampleFromSchema(response.inputSchema)
          setInputJson(JSON.stringify(example, null, 2))
        } catch (e) {
          console.error('生成示例输入失败', e)
        }
      }
    } catch (error: any) {
      console.error('加载Agent失败:', error)
      setError(error.message || '加载失败')
      message.error('加载失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const handleExecute = async () => {
    try {
      const input = JSON.parse(inputJson)
      setExecuting(true)
      setResult(null)

      const response: any = await request.post('/agent/execute', {
        agentCode: agent?.agentCode,
        input: input,
      })

      setResult(response)
      
      if (response.status === 'SUCCESS') {
        message.success('执行成功')
      } else {
        message.error('执行失败')
      }
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        message.error('输入JSON格式错误')
      } else {
        message.error('执行失败')
        setResult({
          status: 'ERROR',
          errorMessage: error.message || '执行失败'
        })
      }
    } finally {
      setExecuting(false)
    }
  }

  if (loading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" tip="加载Agent信息..." />
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ padding: 24 }}>
        <Card>
          <Empty
            description={
              <div>
                <p>{error}</p>
                <Space>
                  <Button onClick={() => navigate('/agent/list')}>返回列表</Button>
                  <Button type="primary" onClick={fetchAgent}>重试</Button>
                </Space>
              </div>
            }
          />
        </Card>
      </div>
    )
  }

  if (!agent) {
    return (
      <div style={{ padding: 24 }}>
        <Card>
          <Empty description="Agent数据为空">
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
            <span>执行Agent - {agent.agentName}</span>
          </Space>
        }
      >
        <Descriptions bordered column={2} style={{ marginBottom: 24 }}>
          <Descriptions.Item label="Agent编码">{agent.agentCode}</Descriptions.Item>
          <Descriptions.Item label="Agent类型">
            <Tag color="blue">{agent.agentType}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="描述" span={2}>
            {agent.description}
          </Descriptions.Item>
        </Descriptions>

        {agent.inputSchema && (
          <Alert
            message="输入Schema"
            description={
              <pre style={{ margin: 0, maxHeight: 200, overflow: 'auto' }}>
                {formatJsonField(agent.inputSchema)}
              </pre>
            }
            type="info"
            style={{ marginBottom: 16 }}
          />
        )}

        <Card title="输入参数" size="small" style={{ marginBottom: 16 }}>
          <MonacoEditor
            height="200px"
            language="json"
            theme="vs-light"
            value={inputJson}
            onChange={(value) => setInputJson(value || '{}')}
            loading={<Spin tip="加载编辑器..." />}
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              lineNumbers: 'on',
              scrollBeyondLastLine: false,
              automaticLayout: true,
            }}
          />
        </Card>

        <Space style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlayCircleOutlined />}
            onClick={handleExecute}
            loading={executing}
          >
            执行
          </Button>
          <Button onClick={() => setInputJson('{\n  \n}')}>
            清空
          </Button>
        </Space>

        {result && (
          <Card
            title={
              <Space>
                <span>执行结果</span>
                <Tag color={result.status === 'SUCCESS' ? 'success' : 'error'}>
                  {result.status}
                </Tag>
              </Space>
            }
            size="small"
          >
            {result.status === 'SUCCESS' ? (
              <>
                <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
                  <Descriptions.Item label="执行ID">{result.executionId}</Descriptions.Item>
                  <Descriptions.Item label="执行时间">{result.executionTime}ms</Descriptions.Item>
                </Descriptions>
                <div>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>输出数据：</div>
                  <pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, overflow: 'auto' }}>
                    {JSON.stringify(result.output, null, 2)}
                  </pre>
                </div>
              </>
            ) : (
              <Alert
                message="执行失败"
                description={result.errorMessage}
                type="error"
                showIcon
              />
            )}
          </Card>
        )}
      </Card>
    </div>
  )
}

export default AgentExecute

