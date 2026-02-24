import { useState } from 'react'
import { Card, Button, Input, Space, message } from 'antd'
import request from '../../utils/request'

const AgentDebug = () => {
  const [agentId, setAgentId] = useState('1')
  const [response, setResponse] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  const testGetAgent = async () => {
    setLoading(true)
    try {
      console.log('Fetching agent with ID:', agentId)
      const data = await request.get(`/agent/${agentId}`)
      console.log('Response data:', data)
      console.log('Response type:', typeof data)
      console.log('Response keys:', data ? Object.keys(data) : 'null')
      setResponse(data)
      message.success('请求成功，查看控制台和下方数据')
    } catch (error: any) {
      console.error('Error:', error)
      message.error('请求失败: ' + error.message)
      setResponse({ error: error.message })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <Card title="Agent API 调试工具">
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space>
            <span>Agent ID:</span>
            <Input
              value={agentId}
              onChange={(e) => setAgentId(e.target.value)}
              style={{ width: 200 }}
            />
            <Button type="primary" onClick={testGetAgent} loading={loading}>
              测试获取Agent
            </Button>
          </Space>

          <Card title="响应数据" size="small">
            <pre style={{ 
              background: '#f5f5f5', 
              padding: 16, 
              borderRadius: 4,
              maxHeight: 600,
              overflow: 'auto'
            }}>
              {response ? JSON.stringify(response, null, 2) : '点击按钮测试...'}
            </pre>
          </Card>

          <Card title="数据检查" size="small">
            {response && (
              <div>
                <p><strong>数据类型:</strong> {typeof response}</p>
                <p><strong>是否为null:</strong> {response === null ? '是' : '否'}</p>
                <p><strong>是否为undefined:</strong> {response === undefined ? '是' : '否'}</p>
                <p><strong>是否为对象:</strong> {typeof response === 'object' ? '是' : '否'}</p>
                <p><strong>包含的字段:</strong></p>
                <ul>
                  {response && typeof response === 'object' && Object.keys(response).map(key => (
                    <li key={key}>
                      <strong>{key}:</strong> {typeof response[key]} 
                      {response[key] === null && ' (null)'}
                      {response[key] === undefined && ' (undefined)'}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </Card>

          <Card title="使用说明" size="small">
            <ol>
              <li>输入Agent ID（默认为1）</li>
              <li>点击"测试获取Agent"按钮</li>
              <li>查看浏览器控制台（F12）的日志输出</li>
              <li>查看下方的响应数据和数据检查</li>
              <li>确认返回的数据结构是否正确</li>
            </ol>
            <p><strong>预期数据结构:</strong></p>
            <pre style={{ background: '#f5f5f5', padding: 8 }}>
{`{
  "id": 1,
  "agentName": "天气查询Agent",
  "agentCode": "weather_agent",
  "agentType": "PYTHON",
  "description": "...",
  "endpointUrl": "http://localhost:5001",
  "inputSchema": "...",
  "outputSchema": "...",
  ...
}`}
            </pre>
          </Card>
        </Space>
      </Card>
    </div>
  )
}

export default AgentDebug

