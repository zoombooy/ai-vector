import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Input, Space, Tag, message, Modal } from 'antd'
import { SearchOutlined, EyeOutlined, DeleteOutlined, ClearOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import request from '@/utils/request'
import dayjs from 'dayjs'

interface Document {
  id: number
  docTitle: string
  docType: string
  categoryId: number
  fileSize: number
  viewCount: number
  status: number
  createTime: string
}

const DocumentList = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<Document[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [keyword, setKeyword] = useState('')

  const columns: ColumnsType<Document> = [
    {
      title: '文档标题',
      dataIndex: 'docTitle',
      key: 'docTitle',
    },
    {
      title: '文档类型',
      dataIndex: 'docType',
      key: 'docType',
      render: (type: string) => <Tag color="blue">{type.toUpperCase()}</Tag>,
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size: number) => `${(size / 1024).toFixed(2)} KB`,
    },
    {
      title: '浏览次数',
      dataIndex: 'viewCount',
      key: 'viewCount',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => {
        const statusMap: Record<number, { text: string; color: string }> = {
          0: { text: '草稿', color: 'default' },
          1: { text: '已发布', color: 'success' },
          2: { text: '已归档', color: 'warning' },
        }
        const { text, color } = statusMap[status] || statusMap[0]
        return <Tag color={color}>{text}</Tag>
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleView(record.id)}
          >
            查看
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ]

  const fetchData = async () => {
    setLoading(true)
    try {
      const response = await request.get('/document/page', {
        params: { pageNum, pageSize, keyword },
      })
      setDataSource(response.records || [])
      setTotal(response.total || 0)
    } catch (error) {
      console.error('获取文档列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleView = (id: number) => {
    navigate(`/document/detail/${id}`)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个文档吗？',
      onOk: async () => {
        try {
          await request.delete(`/document/${id}`)
          message.success('删除成功')
          fetchData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      },
    })
  }

  const handleSearch = () => {
    setPageNum(1)
    fetchData()
  }

  const handleResetCollection = () => {
    Modal.confirm({
      title: '确认清空向量数据库',
      content: (
        <div>
          <p>⚠️ 此操作将清空所有文档的向量数据！</p>
          <p>清空后需要重新上传文档才能使用 RAG 问答功能。</p>
          <p style={{ color: 'red', fontWeight: 'bold' }}>此操作不可恢复，请谨慎操作！</p>
        </div>
      ),
      okText: '确认清空',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await request.post('/vector/reset-collection')
          message.success('向量数据库已清空，Collection 已重新创建')
        } catch (error) {
          console.error('清空向量数据库失败:', error)
          message.error('清空向量数据库失败')
        }
      },
    })
  }

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize])

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>文档列表</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input
          placeholder="搜索文档"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onPressEnter={handleSearch}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
          搜索
        </Button>
        <Button
          danger
          icon={<ClearOutlined />}
          onClick={handleResetCollection}
        >
          清空向量数据库
        </Button>
      </Space>
      <Table
        columns={columns}
        dataSource={dataSource}
        loading={loading}
        rowKey="id"
        pagination={{
          current: pageNum,
          pageSize: pageSize,
          total: total,
          onChange: (page, size) => {
            setPageNum(page)
            setPageSize(size)
          },
        }}
      />
    </div>
  )
}

export default DocumentList

