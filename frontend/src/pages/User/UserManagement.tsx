import { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Modal,
  Form,
  message,
  Popconfirm,
  Switch,
  Select,
  Tag,
  Transfer,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  KeyOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import request from '../../utils/request'

const { Option } = Select

interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  organizationId?: number
  status: number
  createTime: string
}

interface Organization {
  id: number
  orgName: string
  orgCode: string
  parentId: number
  status: number
}

interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
}

const UserManagement = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<User[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [form] = Form.useForm()

  // 组织列表
  const [organizations, setOrganizations] = useState<Organization[]>([])

  // 角色分配相关
  const [roleModalVisible, setRoleModalVisible] = useState(false)
  const [currentUserId, setCurrentUserId] = useState<number | null>(null)
  const [allRoles, setAllRoles] = useState<Role[]>([])
  const [userRoles, setUserRoles] = useState<number[]>([])

  useEffect(() => {
    fetchData()
    fetchOrganizations()
    fetchAllRoles()
  }, [pageNum, pageSize, keyword])

  const fetchData = async () => {
    setLoading(true)
    try {
      const params: any = { pageNum, pageSize }
      if (keyword) params.keyword = keyword

      const response: any = await request.get('/user/page', { params })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      message.error('获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchOrganizations = async () => {
    try {
      const response: any = await request.get('/organization/tree')
      // 将树形结构扁平化
      const flatOrgs = flattenTree(response || [])
      setOrganizations(flatOrgs)
    } catch (error) {
      console.error('获取组织列表失败:', error)
    }
  }

  const flattenTree = (tree: any[], result: Organization[] = []): Organization[] => {
    tree.forEach((node) => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        flattenTree(node.children, result)
      }
    })
    return result
  }

  const fetchAllRoles = async () => {
    try {
      const response: any = await request.get('/role/all')
      setAllRoles(response || [])
    } catch (error) {
      console.error('获取角色列表失败:', error)
    }
  }

  const handleSearch = () => {
    setKeyword(searchKeyword)
    setPageNum(1)
  }

  const handleReset = () => {
    setSearchKeyword('')
    setKeyword('')
    setPageNum(1)
  }

  const handleAdd = () => {
    setEditingUser(null)
    form.resetFields()
    form.setFieldsValue({ status: 1 })
    setModalVisible(true)
  }

  const handleEdit = async (record: User) => {
    setEditingUser(record)
    form.setFieldsValue({
      ...record,
      status: record.status === 1,
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/user/${id}`)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data = {
        ...values,
        id: editingUser?.id,
        status: values.status ? 1 : 0,
      }

      if (editingUser) {
        await request.put('/user', data)
      } else {
        await request.post('/user', data)
      }

      message.success(editingUser ? '更新成功' : '创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请填写完整信息')
      } else {
        message.error(editingUser ? '更新失败' : '创建失败')
      }
    }
  }

  const handleResetPassword = async (id: number) => {
    try {
      await request.post(`/user/${id}/reset-password`, null, {
        params: { newPassword: '123456' },
      })
      message.success('密码已重置为 123456')
    } catch (error) {
      message.error('重置密码失败')
    }
  }

  const handleAssignRoles = async (userId: number) => {
    setCurrentUserId(userId)
    try {
      // 获取用户当前的角色
      const response: any = await request.get(`/user/${userId}/roles`)
      const roleIds = (response || []).map((role: Role) => role.id)
      setUserRoles(roleIds)
      setRoleModalVisible(true)
    } catch (error) {
      message.error('获取用户角色失败')
    }
  }

  const handleRoleSubmit = async () => {
    if (currentUserId === null) return

    try {
      await request.post(`/user/${currentUserId}/roles`, userRoles)
      message.success('角色分配成功')
      setRoleModalVisible(false)
      setCurrentUserId(null)
      setUserRoles([])
    } catch (error) {
      message.error('角色分配失败')
    }
  }

  const columns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      key: 'realName',
      width: 120,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 180,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 130,
    },
    {
      title: '组织',
      dataIndex: 'organizationId',
      key: 'organizationId',
      width: 150,
      render: (orgId: number) => {
        const org = organizations.find((o) => o.id === orgId)
        return org ? org.orgName : '-'
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
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
      width: 280,
      fixed: 'right' as const,
      render: (_: any, record: User) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<TeamOutlined />}
            onClick={() => handleAssignRoles(record.id)}
          >
            分配角色
          </Button>
          <Popconfirm
            title="确定要重置密码吗？"
            description="密码将重置为 123456"
            onConfirm={() => handleResetPassword(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" icon={<KeyOutlined />}>
              重置密码
            </Button>
          </Popconfirm>
          <Popconfirm
            title="确定要删除这个用户吗？"
            onConfirm={() => handleDelete(record.id)}
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
      <h1 style={{ marginBottom: 24 }}>用户管理</h1>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索用户名、姓名、邮箱、手机号"
            allowClear
            style={{ width: 300 }}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增用户
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1300 }}
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

      {/* 用户编辑/新增对话框 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" disabled={!!editingUser} />
          </Form.Item>

          {!editingUser && (
            <Form.Item
              label="密码"
              name="password"
              rules={[{ required: !editingUser, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码（默认：123456）" />
            </Form.Item>
          )}

          <Form.Item
            label="真实姓名"
            name="realName"
            rules={[{ required: true, message: '请输入真实姓名' }]}
          >
            <Input placeholder="请输入真实姓名" />
          </Form.Item>

          <Form.Item
            label="邮箱"
            name="email"
            rules={[
              { type: 'email', message: '请输入正确的邮箱格式' },
            ]}
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>

          <Form.Item
            label="手机号"
            name="phone"
            rules={[
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
            ]}
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>

          <Form.Item label="组织" name="organizationId">
            <Select placeholder="请选择组织" allowClear>
              {organizations.map((org) => (
                <Option key={org.id} value={org.id}>
                  {org.orgName}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="状态" name="status" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 角色分配对话框 */}
      <Modal
        title="分配角色"
        open={roleModalVisible}
        onOk={handleRoleSubmit}
        onCancel={() => {
          setRoleModalVisible(false)
          setCurrentUserId(null)
          setUserRoles([])
        }}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Transfer
          dataSource={allRoles.map((role) => ({
            key: role.id.toString(),
            title: role.roleName,
            description: role.description,
          }))}
          titles={['可选角色', '已选角色']}
          targetKeys={userRoles.map((id) => id.toString())}
          onChange={(targetKeys) => {
            setUserRoles(targetKeys.map((key) => parseInt(key as string)))
          }}
          render={(item) => item.title}
          listStyle={{
            width: 250,
            height: 400,
          }}
        />
      </Modal>
    </div>
  )
}

export default UserManagement

