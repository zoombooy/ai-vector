import React, { useState, useEffect, useCallback } from 'react'
import { Card, Button, Form, Input, Select, message, Space, Modal, Drawer } from 'antd'
import { SaveOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import request from '../../utils/request'

const { TextArea } = Input
const { Option } = Select

interface Node {
  nodeId: string
  nodeName: string
  nodeType: string
  positionX: number
  positionY: number
  config: any
}

interface Edge {
  edgeId: string
  sourceNodeId: string
  targetNodeId: string
  condition?: string
}

const WorkflowEditor: React.FC = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [nodes, setNodes] = useState<Node[]>([])
  const [edges, setEdges] = useState<Edge[]>([])
  const [selectedNode, setSelectedNode] = useState<Node | null>(null)
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [agents, setAgents] = useState<any[]>([])
  const [functions, setFunctions] = useState<any[]>([])

  useEffect(() => {
    if (id) {
      fetchWorkflow()
    } else {
      // 新建时添加START和END节点
      setNodes([
        {
          nodeId: 'start',
          nodeName: '开始',
          nodeType: 'START',
          positionX: 100,
          positionY: 100,
          config: {}
        },
        {
          nodeId: 'end',
          nodeName: '结束',
          nodeType: 'END',
          positionX: 500,
          positionY: 100,
          config: {}
        }
      ])
    }
    fetchAgents()
    fetchFunctions()
  }, [id])

  const fetchWorkflow = async () => {
    try {
      const response: any = await request.get(`/workflow/${id}`)
      form.setFieldsValue({
        workflowName: response.workflowName,
        workflowCode: response.workflowCode,
        description: response.description,
        category: response.category,
        tags: response.tags,
        status: response.status,
        version: response.version
      })
      setNodes(response.nodes || [])
      setEdges(response.edges || [])
    } catch (error) {
      message.error('加载失败')
    }
  }

  const fetchAgents = async () => {
    try {
      const response: any = await request.get('/agent/page', {
        params: { pageNum: 1, pageSize: 100 }
      })
      setAgents(response.list || [])
    } catch (error) {
      console.error('加载Agent失败', error)
    }
  }

  const fetchFunctions = async () => {
    try {
      const response: any = await request.get('/function/page', {
        params: { pageNum: 1, pageSize: 100 }
      })
      setFunctions(response.list || [])
    } catch (error) {
      console.error('加载Function失败', error)
    }
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      const data = {
        ...values,
        nodes,
        edges
      }

      if (id) {
        await request.put(`/workflow/${id}`, data)
        message.success('更新成功')
      } else {
        await request.post('/workflow', data)
        message.success('创建成功')
      }

      navigate('/workflow/list')
    } catch (error) {
      message.error('保存失败')
    } finally {
      setLoading(false)
    }
  }

  const addNode = (nodeType: string) => {
    const newNode: Node = {
      nodeId: `node_${Date.now()}`,
      nodeName: `新节点_${nodes.length}`,
      nodeType,
      positionX: 300,
      positionY: 200 + nodes.length * 80,
      config: {}
    }
    setNodes([...nodes, newNode])
    setSelectedNode(newNode)
    setDrawerVisible(true)
  }

  const updateNode = (nodeId: string, updates: Partial<Node>) => {
    setNodes(nodes.map(n => n.nodeId === nodeId ? { ...n, ...updates } : n))
  }

  const deleteNode = (nodeId: string) => {
    setNodes(nodes.filter(n => n.nodeId !== nodeId))
    setEdges(edges.filter(e => e.sourceNodeId !== nodeId && e.targetNodeId !== nodeId))
  }

  return (
    <div style={{ padding: 24 }}>
      <Card
        title={id ? '编辑工作流' : '创建工作流'}
        extra={
          <Space>
            <Button onClick={() => navigate('/workflow/list')}>取消</Button>
            <Button type="primary" icon={<SaveOutlined />} loading={loading} onClick={handleSave}>
              保存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="workflowName" label="工作流名称" rules={[{ required: true }]}>
            <Input placeholder="请输入工作流名称" />
          </Form.Item>

          <Form.Item name="workflowCode" label="工作流编码" rules={[{ required: true }]}>
            <Input placeholder="请输入工作流编码（唯一）" />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>

          <Form.Item name="category" label="分类">
            <Input placeholder="请输入分类" />
          </Form.Item>

          <Form.Item name="tags" label="标签">
            <Input placeholder="多个标签用逗号分隔" />
          </Form.Item>

          <Form.Item name="status" label="状态" initialValue={1}>
            <Select>
              <Option value={1}>启用</Option>
              <Option value={0}>禁用</Option>
            </Select>
          </Form.Item>

          <Form.Item name="version" label="版本" initialValue="1.0.0">
            <Input placeholder="请输入版本号" />
          </Form.Item>
        </Form>

        <div style={{ marginTop: 24 }}>
          <h3>工作流节点</h3>
          <Space style={{ marginBottom: 16 }}>
            <Button onClick={() => addNode('AGENT')}>添加Agent节点</Button>
            <Button onClick={() => addNode('FUNCTION')}>添加Function节点</Button>
            <Button onClick={() => addNode('CONDITION')}>添加条件节点</Button>
          </Space>

          <div style={{ border: '1px solid #d9d9d9', padding: 16, minHeight: 400, background: '#fafafa' }}>
            {nodes.map(node => (
              <div
                key={node.nodeId}
                style={{
                  position: 'absolute',
                  left: node.positionX,
                  top: node.positionY,
                  padding: '8px 16px',
                  background: node.nodeType === 'START' ? '#52c41a' : node.nodeType === 'END' ? '#ff4d4f' : '#1890ff',
                  color: 'white',
                  borderRadius: 4,
                  cursor: 'pointer'
                }}
                onClick={() => {
                  setSelectedNode(node)
                  setDrawerVisible(true)
                }}
              >
                {node.nodeName}
              </div>
            ))}
          </div>
        </div>
      </Card>

      <Drawer
        title="节点配置"
        placement="right"
        width={400}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
      >
        {selectedNode && (
          <Form layout="vertical">
            <Form.Item label="节点名称">
              <Input
                value={selectedNode.nodeName}
                onChange={e => updateNode(selectedNode.nodeId, { nodeName: e.target.value })}
              />
            </Form.Item>

            <Form.Item label="节点类型">
              <Input value={selectedNode.nodeType} disabled />
            </Form.Item>

            {selectedNode.nodeType === 'AGENT' && (
              <Form.Item label="选择Agent">
                <Select
                  value={selectedNode.config?.agentCode}
                  onChange={value => updateNode(selectedNode.nodeId, {
                    config: { ...selectedNode.config, agentCode: value }
                  })}
                >
                  {agents.map(agent => (
                    <Option key={agent.agentCode} value={agent.agentCode}>
                      {agent.agentName}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}

            {selectedNode.nodeType === 'FUNCTION' && (
              <Form.Item label="选择Function">
                <Select
                  value={selectedNode.config?.functionName}
                  onChange={value => updateNode(selectedNode.nodeId, {
                    config: { ...selectedNode.config, functionName: value }
                  })}
                >
                  {functions.map(func => (
                    <Option key={func.functionName} value={func.functionName}>
                      {func.functionName}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}

            {selectedNode.nodeType !== 'START' && selectedNode.nodeType !== 'END' && (
              <Button danger onClick={() => {
                deleteNode(selectedNode.nodeId)
                setDrawerVisible(false)
              }}>
                删除节点
              </Button>
            )}
          </Form>
        )}
      </Drawer>
    </div>
  )
}

export default WorkflowEditor

