import { useState, useEffect } from 'react'
import {
  Card,
  Tree,
  Button,
  Space,
  Modal,
  Form,
  message,
  Popconfirm,
  Switch,
  Input,
  Select,
  InputNumber,
  Row,
  Col,
  Descriptions,
  Tag,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FolderOutlined,
  FolderOpenOutlined,
} from '@ant-design/icons'
import type { DataNode } from 'antd/es/tree'
import request from '../../utils/request'

const { TextArea } = Input
const { Option } = Select

interface Organization {
  id: number
  orgName: string
  orgCode: string
  parentId: number
  orgLevel: number
  leaderId?: number
  leaderName?: string
  sort: number
  status: number
  remark?: string
  createTime: string
  children?: Organization[]
}

interface User {
  id: number
  realName: string
}

const OrganizationManagement = () => {
  const [treeData, setTreeData] = useState<DataNode[]>([])
  const [orgList, setOrgList] = useState<Organization[]>([])
  const [selectedOrg, setSelectedOrg] = useState<Organization | null>(null)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null)
  const [form] = Form.useForm()
  const [users, setUsers] = useState<User[]>([])

  useEffect(() => {
    fetchData()
    fetchUsers()
  }, [])

  const fetchData = async () => {
    try {
      const response: any = await request.get('/organization/tree')
      setOrgList(flattenTree(response || []))
      setTreeData(convertToTreeData(response || []))
    } catch (error) {
      message.error('获取组织列表失败')
    }
  }

  const fetchUsers = async () => {
    try {
      const response: any = await request.get('/user/page', {
        params: { pageNum: 1, pageSize: 1000 },
      })
      setUsers(response.records || [])
    } catch (error) {
      console.error('获取用户列表失败:', error)
    }
  }

  const flattenTree = (tree: Organization[], result: Organization[] = []): Organization[] => {
    tree.forEach((node) => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        flattenTree(node.children, result)
      }
    })
    return result
  }

  const convertToTreeData = (data: Organization[]): DataNode[] => {
    return data.map((item) => ({
      key: item.id.toString(),
      title: item.orgName,
      icon: item.children && item.children.length > 0 ? <FolderOpenOutlined /> : <FolderOutlined />,
      children: item.children ? convertToTreeData(item.children) : undefined,
    }))
  }

  const handleSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length > 0) {
      const orgId = parseInt(selectedKeys[0] as string)
      const org = orgList.find((o) => o.id === orgId)
      setSelectedOrg(org || null)
    } else {
      setSelectedOrg(null)
    }
  }

  const handleAdd = (parentId?: number) => {
    setEditingOrg(null)
    form.resetFields()
    form.setFieldsValue({
      parentId: parentId || 0,
      status: 1,
      sort: 0,
    })
    setModalVisible(true)
  }

  const handleEdit = (org: Organization) => {
    setEditingOrg(org)
    form.setFieldsValue({
      ...org,
      status: org.status === 1,
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/organization/${id}`)
      message.success('删除成功')
      fetchData()
      if (selectedOrg?.id === id) {
        setSelectedOrg(null)
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data = {
        ...values,
        id: editingOrg?.id,
        status: values.status ? 1 : 0,
      }

      if (editingOrg) {
        await request.put('/organization', data)
      } else {
        await request.post('/organization', data)
      }

      message.success(editingOrg ? '更新成功' : '创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请填写完整信息')
      } else {
        message.error(editingOrg ? '更新失败' : '创建失败')
      }
    }
  }

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>组织管理</h1>
      <Row gutter={16}>
        <Col span={8}>
          <Card
            title="组织树"
            extra={
              <Button
                type="primary"
                size="small"
                icon={<PlusOutlined />}
                onClick={() => handleAdd()}
              >
                新增根组织
              </Button>
            }
          >
            <Tree
              showIcon
              defaultExpandAll
              treeData={treeData}
              onSelect={handleSelect}
            />
          </Card>
        </Col>
        <Col span={16}>
          <Card
            title="组织详情"
            extra={
              selectedOrg && (
                <Space>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => handleAdd(selectedOrg.id)}
                  >
                    新增子组织
                  </Button>
                  <Button
                    size="small"
                    icon={<EditOutlined />}
                    onClick={() => handleEdit(selectedOrg)}
                  >
                    编辑
                  </Button>
                  <Popconfirm
                    title="确定要删除这个组织吗？"
                    description="删除后不可恢复，且该组织下不能有子组织和用户"
                    onConfirm={() => handleDelete(selectedOrg.id)}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button size="small" danger icon={<DeleteOutlined />}>
                      删除
                    </Button>
                  </Popconfirm>
                </Space>
              )
            }
          >
            {selectedOrg ? (
              <Descriptions column={2} bordered>
                <Descriptions.Item label="组织名称" span={2}>
                  {selectedOrg.orgName}
                </Descriptions.Item>
                <Descriptions.Item label="组织编码" span={2}>
                  {selectedOrg.orgCode}
                </Descriptions.Item>
                <Descriptions.Item label="组织层级">
                  {selectedOrg.orgLevel}
                </Descriptions.Item>
                <Descriptions.Item label="排序">
                  {selectedOrg.sort}
                </Descriptions.Item>
                <Descriptions.Item label="负责人">
                  {selectedOrg.leaderName || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={selectedOrg.status === 1 ? 'green' : 'red'}>
                    {selectedOrg.status === 1 ? '启用' : '禁用'}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="创建时间" span={2}>
                  {selectedOrg.createTime}
                </Descriptions.Item>
                <Descriptions.Item label="备注" span={2}>
                  {selectedOrg.remark || '-'}
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <div style={{ textAlign: 'center', padding: '50px 0', color: '#999' }}>
                请从左侧选择一个组织查看详情
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <Modal
        title={editingOrg ? '编辑组织' : '新增组织'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item label="父组织" name="parentId">
            <Select placeholder="请选择父组织（不选则为根组织）" allowClear>
              <Option value={0}>根组织</Option>
              {orgList.map((org) => (
                <Option key={org.id} value={org.id} disabled={org.id === editingOrg?.id}>
                  {org.orgName}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="组织名称"
            name="orgName"
            rules={[{ required: true, message: '请输入组织名称' }]}
          >
            <Input placeholder="请输入组织名称" />
          </Form.Item>

          <Form.Item
            label="组织编码"
            name="orgCode"
            rules={[{ required: true, message: '请输入组织编码' }]}
          >
            <Input placeholder="请输入组织编码" disabled={!!editingOrg} />
          </Form.Item>

          <Form.Item label="负责人" name="leaderId">
            <Select placeholder="请选择负责人" allowClear showSearch optionFilterProp="children">
              {users.map((user) => (
                <Option key={user.id} value={user.id}>
                  {user.realName}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="排序" name="sort">
            <InputNumber min={0} placeholder="数字越小越靠前" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="状态" name="status" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>

          <Form.Item label="备注" name="remark">
            <TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default OrganizationManagement

