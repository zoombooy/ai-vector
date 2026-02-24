import React, { useState, useEffect, useRef } from 'react'
import { Card, Input, Button, List, Avatar, Space, Select, message, Spin, Empty, Switch, Tag, Tooltip } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined, DeleteOutlined, PlusOutlined, FunctionOutlined, ApiOutlined, CloudServerOutlined } from '@ant-design/icons'
import request from '../../utils/request'
import './index.css'

const { TextArea } = Input
const { Option } = Select

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

interface Model {
  id: number
  modelName: string
  modelCode: string
  status: number
}

const KnowledgeQA: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([])
  const [inputValue, setInputValue] = useState('')
  const [loading, setLoading] = useState(false)
  const [models, setModels] = useState<Model[]>([])
  const [selectedModelId, setSelectedModelId] = useState<number>()
  const [sessionId, setSessionId] = useState<string>('')
  const [sessions, setSessions] = useState<string[]>([])
  const [enableAgent, setEnableAgent] = useState(false)
  const [enableFunctionCall, setEnableFunctionCall] = useState(true)
  const [enableMcp, setEnableMcp] = useState(false)
  const [calledAgents, setCalledAgents] = useState<string[]>([])
  const [calledFunctions, setCalledFunctions] = useState<string[]>([])
  const [calledMcpTools, setCalledMcpTools] = useState<string[]>([])
  const messagesEndRef = useRef<HTMLDivElement>(null)

  // 滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  // 加载可用模型
  useEffect(() => {
    fetchModels()
    fetchSessions()
  }, [])

  const fetchModels = async () => {
    try {
      const response: any = await request.get('/model/enabled')
      setModels(response || [])
      if (response && response.length > 0) {
        setSelectedModelId(response[0].id)
      }
    } catch (error) {
      message.error('获取模型列表失败')
    }
  }

  const fetchSessions = async () => {
    try {
      const response: any = await request.get('/qa/sessions')
      setSessions(response || [])
    } catch (error) {
      console.error('获取会话列表失败:', error)
    }
  }

  const loadHistory = async (sid: string) => {
    try {
      const response: any = await request.get(`/qa/history/${sid}`)
      const history = response || []
      const msgs: Message[] = []
      history.forEach((item: any) => {
        msgs.push({
          role: 'user',
          content: item.question,
          timestamp: new Date(item.createTime).getTime()
        })
        msgs.push({
          role: 'assistant',
          content: item.answer,
          timestamp: new Date(item.createTime).getTime() + 1
        })
      })
      setMessages(msgs)
      setSessionId(sid)
    } catch (error) {
      message.error('加载历史记录失败')
    }
  }

  const handleSend = async () => {
    if (!inputValue.trim()) {
      message.warning('请输入问题')
      return
    }

    if (!selectedModelId) {
      message.warning('请选择模型')
      return
    }

    const userMessage: Message = {
      role: 'user',
      content: inputValue,
      timestamp: Date.now()
    }

    setMessages(prev => [...prev, userMessage])
    const currentQuestion = inputValue
    setInputValue('')
    setLoading(true)

    try {
      // 调用非流式问答接口
      const response: any = await request.post('/qa/ask', {
        question: currentQuestion,
        sessionId: sessionId || undefined,
        modelId: selectedModelId,
        enableAgent: enableAgent,
        enableFunctionCall: enableFunctionCall,
        enableMcp: enableMcp
      })

      console.log('QA响应:', response)

      // 更新会话ID
      if (response.sessionId) {
        setSessionId(response.sessionId)
        if (!sessions.includes(response.sessionId)) {
          setSessions(prev => [response.sessionId, ...prev])
        }
      }

      // 更新调用的Agent、Function和MCP工具
      if (response.calledAgents && response.calledAgents.length > 0) {
        setCalledAgents(response.calledAgents)
      }
      if (response.calledFunctions && response.calledFunctions.length > 0) {
        setCalledFunctions(response.calledFunctions)
      }
      if (response.calledMcpTools && response.calledMcpTools.length > 0) {
        setCalledMcpTools(response.calledMcpTools)
      }

      // 添加助手回复
      const assistantMessage: Message = {
        role: 'assistant',
        content: response.answer || '抱歉，我无法回答这个问题。',
        timestamp: Date.now()
      }
      setMessages(prev => [...prev, assistantMessage])

      setLoading(false)

    } catch (error) {
      setLoading(false)
      message.error('问答失败')
      console.error('问答失败:', error)
    }
  }

  const handleNewChat = () => {
    setMessages([])
    setSessionId('')
  }

  const handleDeleteSession = async (sid: string) => {
    try {
      await request.delete(`/qa/session/${sid}`)
      message.success('删除成功')
      setSessions(prev => prev.filter(s => s !== sid))
      if (sid === sessionId) {
        handleNewChat()
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  return (
    <div className="qa-page">
      <h1 style={{ marginBottom: 24 }}>AI 问答</h1>

      <div className="qa-container">
        {/* 左侧会话列表 */}
        <Card className="session-list" title="会话列表">
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleNewChat}
            style={{ width: '100%', marginBottom: 16 }}
          >
            新建对话
          </Button>
          <List
            dataSource={sessions}
            renderItem={(sid) => (
              <List.Item
                className={sid === sessionId ? 'session-item active' : 'session-item'}
                onClick={() => loadHistory(sid)}
                actions={[
                  <DeleteOutlined
                    key="delete"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleDeleteSession(sid)
                    }}
                  />
                ]}
              >
                <List.Item.Meta
                  title={`会话 ${sid.substring(0, 8)}...`}
                />
              </List.Item>
            )}
          />
        </Card>

        {/* 右侧聊天区域 */}
        <div className="chat-area">
          <Card
            title={
              <Space>
                <span>对话</span>
                <Select
                  style={{ width: 200 }}
                  placeholder="选择模型"
                  value={selectedModelId}
                  onChange={setSelectedModelId}
                >
                  {models.map(model => (
                    <Option key={model.id} value={model.id}>
                      {model.modelName}
                    </Option>
                  ))}
                </Select>
                <Tooltip title="启用后AI可以调用Agent来完成任务">
                  <Space>
                    <ApiOutlined />
                    <Switch
                      checked={enableAgent}
                      onChange={setEnableAgent}
                      checkedChildren="Agent"
                      unCheckedChildren="Agent"
                    />
                  </Space>
                </Tooltip>
                <Tooltip title="启用后AI可以调用Function">
                  <Space>
                    <FunctionOutlined />
                    <Switch
                      checked={enableFunctionCall}
                      onChange={setEnableFunctionCall}
                      checkedChildren="Function"
                      unCheckedChildren="Function"
                    />
                  </Space>
                </Tooltip>
                <Tooltip title="启用后AI可以调用MCP工具（如地图、搜索等）">
                  <Space>
                    <CloudServerOutlined />
                    <Switch
                      checked={enableMcp}
                      onChange={setEnableMcp}
                      checkedChildren="MCP"
                      unCheckedChildren="MCP"
                    />
                  </Space>
                </Tooltip>
              </Space>
            }
            extra={
              calledAgents.length > 0 || calledFunctions.length > 0 || calledMcpTools.length > 0 ? (
                <Space>
                  {calledAgents.length > 0 && (
                    <Space size={4}>
                      <span style={{ fontSize: 12, color: '#666' }}>调用Agent:</span>
                      {calledAgents.map((agent, idx) => (
                        <Tag key={idx} color="blue" icon={<ApiOutlined />}>{agent}</Tag>
                      ))}
                    </Space>
                  )}
                  {calledFunctions.length > 0 && (
                    <Space size={4}>
                      <span style={{ fontSize: 12, color: '#666' }}>调用Function:</span>
                      {calledFunctions.map((func, idx) => (
                        <Tag key={idx} color="green" icon={<FunctionOutlined />}>{func}</Tag>
                      ))}
                    </Space>
                  )}
                  {calledMcpTools.length > 0 && (
                    <Space size={4}>
                      <span style={{ fontSize: 12, color: '#666' }}>调用MCP:</span>
                      {calledMcpTools.map((tool, idx) => (
                        <Tag key={idx} color="purple" icon={<CloudServerOutlined />}>{tool}</Tag>
                      ))}
                    </Space>
                  )}
                </Space>
              ) : null
            }
            className="chat-card"
          >
            <div className="messages-container">
              {messages.length === 0 ? (
                <Empty description="开始新的对话吧" />
              ) : (
                <List
                  dataSource={messages}
                  renderItem={(msg) => (
                    <div className={`message-item ${msg.role}`}>
                      <Avatar
                        icon={msg.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                        className="message-avatar"
                      />
                      <div className="message-content">
                        <div className="message-text">{msg.content}</div>
                      </div>
                    </div>
                  )}
                />
              )}
              {loading && (
                <div className="message-item assistant">
                  <Avatar icon={<RobotOutlined />} className="message-avatar" />
                  <div className="message-content">
                    <Spin size="small" />
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            <div className="input-area">
              <TextArea
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onPressEnter={(e) => {
                  if (!e.shiftKey) {
                    e.preventDefault()
                    handleSend()
                  }
                }}
                placeholder="输入问题... (Shift+Enter 换行，Enter 发送)"
                autoSize={{ minRows: 2, maxRows: 6 }}
                disabled={loading}
              />
              <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={handleSend}
                loading={loading}
                style={{ marginTop: 8 }}
              >
                发送
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default KnowledgeQA

