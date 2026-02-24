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
  Tag,
  InputNumber,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import request from '../../utils/request'

const { TextArea } = Input

interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  sort: number
  status: number
  createTime: string
}

const RoleManagement = () => {
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<Role[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRole, setEditingRole] = useState<Role | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize, keyword])

  const fetchData = async () => {
    setLoading(true)
    try {
      const params: any = { pageNum, pageSize }
      if (keyword) params.keyword = keyword

      const response: any = await request.get('/role/page', { params })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      message.error('获取角色列表失败')
    } finally {
      setLoading(false)
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
    setEditingRole(null)
    form.resetFields()
    form.setFieldsValue({ status: 1, sort: 0 })
    setModalVisible(true)
  }

  const handleEdit = (record: Role) => {
    setEditingRole(record)
    form.setFieldsValue({
      ...record,
      status: record.status === 1,
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/role/${id}`)
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
        id: editingRole?.id,
        status: values.status ? 1 : 0,
      }

      if (editingRole) {
        await request.put('/role', data)
      } else {
        await request.post('/role', data)
      }

      message.success(editingRole ? '更新成功' : '创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请填写完整信息')
      } else {
        message.error(editingRole ? '更新失败' : '创建失败')
      }
    }
  }

  const columns = [
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName',
      width: 150,
    },
    {
      title: '角色编码',
      dataIndex: 'roleCode',
      key: 'roleCode',
      width: 180,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
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
      width: 180,
      fixed: 'right' as const,
      render: (_: any, record: Role) => (
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
            title="确定要删除这个角色吗？"
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
      <h1 style={{ marginBottom: 24 }}>角色管理</h1>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索角色名称或编码"
            allowClear
            style={{ width: 250 }}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增角色
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1000 }}
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
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="角色名称"
            name="roleName"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>

          <Form.Item
            label="角色编码"
            name="roleCode"
            rules={[{ required: true, message: '请输入角色编码' }]}
          >
            <Input placeholder="例如：ROLE_ADMIN" disabled={!!editingRole} />
          </Form.Item>

          <Form.Item label="描述" name="description">
            <TextArea rows={3} placeholder="请输入角色描述" />
          </Form.Item>

          <Form.Item label="排序" name="sort">
            <InputNumber min={0} placeholder="数字越小越靠前" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="状态" name="status" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RoleManagement

