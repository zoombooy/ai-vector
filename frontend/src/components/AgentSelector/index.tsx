import { useState, useEffect } from 'react'
import { Modal, Select, Form, Button, message, Spin, Alert } from 'antd'
import { PlayCircleOutlined } from '@ant-design/icons'
import MonacoEditor from '@monaco-editor/react'
import request from '../../utils/request'

interface Agent {
  id: number
  agentName: string
  agentCode: string
  description: string
  inputSchema: string
  outputSchema: string
}

interface AgentSelectorProps {
  visible: boolean
  onClose: () => void
  onExecute?: (result: any) => void
}

const AgentSelector = ({ visible, onClose, onExecute }: AgentSelectorProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [executing, setExecuting] = useState(false)
  const [agents, setAgents] = useState<Agent[]>([])
  const [selectedAgent, setSelectedAgent] = useState<Agent | null>(null)
  const [inputJson, setInputJson] = useState('{\n  \n}')
  const [result, setResult] = useState<any>(null)

  useEffect(() => {
    if (visible) {
      fetchAgents()
    }
  }, [visible])

  const fetchAgents = async () => {
    setLoading(true)
    try {
      const response: any = await request.get('/agent/available')
      setAgents(response || [])
    } catch (error) {
      message.error('加载Agent列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAgentChange = (agentCode: string) => {
    const agent = agents.find(a => a.agentCode === agentCode)
    setSelectedAgent(agent || null)
    setResult(null)
    
    if (agent?.inputSchema) {
      try {
        const schema = JSON.parse(agent.inputSchema)
        const example: any = {}
        if (schema.properties) {
          Object.keys(schema.properties).forEach(key => {
            const prop = schema.properties[key]
            if (prop.type === 'string') {
              example[key] = ''
            } else if (prop.type === 'number') {
              example[key] = 0
            } else if (prop.type === 'array') {
              example[key] = []
            } else if (prop.type === 'object') {
              example[key] = {}
            }
          })
        }
        setInputJson(JSON.stringify(example, null, 2))
      } catch (e) {
        console.error('解析inputSchema失败', e)
      }
    }
  }

  const handleExecute = async () => {
    if (!selectedAgent) {
      message.warning('请选择Agent')
      return
    }

    try {
      const input = JSON.parse(inputJson)
      setExecuting(true)
      setResult(null)

      const response: any = await request.post('/agent/execute', {
        agentCode: selectedAgent.agentCode,
        input: input,
      })

      setResult(response)
      
      if (response.status === 'SUCCESS') {
        message.success('执行成功')
        if (onExecute) {
          onExecute(response)
        }
      } else {
        message.error('执行失败')
      }
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        message.error('输入JSON格式错误')
      } else {
        message.error('执行失败')
      }
    } finally {
      setExecuting(false)
    }
  }

  const handleClose = () => {
    setSelectedAgent(null)
    setInputJson('{\n  \n}')
    setResult(null)
    form.resetFields()
    onClose()
  }

  return (
    <Modal
      title="调用Agent"
      open={visible}
      onCancel={handleClose}
      width={800}
      footer={null}
    >
      <Spin spinning={loading}>
        <Form form={form} layout="vertical">
          <Form.Item label="选择Agent" required>
            <Select
              placeholder="请选择Agent"
              onChange={handleAgentChange}
              options={agents.map(agent => ({
                label: `${agent.agentName} (${agent.agentCode})`,
                value: agent.agentCode,
              }))}
            />
          </Form.Item>

          {selectedAgent && (
            <>
              <Alert
                message={selectedAgent.description}
                type="info"
                style={{ marginBottom: 16 }}
              />

              {selectedAgent.inputSchema && (
                <Alert
                  message="输入Schema"
                  description={
                    <pre style={{ margin: 0, maxHeight: 150, overflow: 'auto' }}>
                      {JSON.stringify(JSON.parse(selectedAgent.inputSchema), null, 2)}
                    </pre>
                  }
                  type="info"
                  style={{ marginBottom: 16 }}
                />
              )}

              <Form.Item label="输入参数">
                <div style={{ border: '1px solid #d9d9d9', borderRadius: 4 }}>
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
                </div>
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  icon={<PlayCircleOutlined />}
                  onClick={handleExecute}
                  loading={executing}
                  block
                >
                  执行
                </Button>
              </Form.Item>

              {result && (
                <Alert
                  message={result.status === 'SUCCESS' ? '执行成功' : '执行失败'}
                  description={
                    <pre style={{ margin: 0, maxHeight: 300, overflow: 'auto' }}>
                      {JSON.stringify(result.status === 'SUCCESS' ? result.output : result.errorMessage, null, 2)}
                    </pre>
                  }
                  type={result.status === 'SUCCESS' ? 'success' : 'error'}
                />
              )}
            </>
          )}
        </Form>
      </Spin>
    </Modal>
  )
}

export default AgentSelector

