import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Space, Spin, message } from 'antd'
import { ArrowLeftOutlined, DownloadOutlined } from '@ant-design/icons'
import request from '@/utils/request'
import dayjs from 'dayjs'

interface DocumentDetail {
  id: number
  docTitle: string
  docType: string
  categoryId: number
  filePath: string
  fileSize: number
  fileHash: string
  content: string
  summary: string
  tags: string
  viewCount: number
  downloadCount: number
  status: number
  createTime: string
  updateTime: string
}

const DocumentDetail = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [document, setDocument] = useState<DocumentDetail | null>(null)

  useEffect(() => {
    if (id) {
      fetchDocumentDetail()
    }
  }, [id])

  const fetchDocumentDetail = async () => {
    setLoading(true)
    try {
      const response = await request.get(`/document/${id}`)
      setDocument(response)
    } catch (error) {
      console.error('获取文档详情失败:', error)
      message.error('获取文档详情失败')
    } finally {
      setLoading(false)
    }
  }

  const handleBack = () => {
    navigate('/document/list')
  }

  const handleDownload = () => {
    if (document?.filePath) {
      // TODO: 实现文件下载功能
      message.info('下载功能开发中...')
    }
  }

  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '草稿', color: 'default' },
    1: { text: '已发布', color: 'success' },
    2: { text: '已归档', color: 'warning' },
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!document) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <p>文档不存在</p>
        <Button type="primary" onClick={handleBack}>
          返回列表
        </Button>
      </div>
    )
  }

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回
        </Button>
        <Button type="primary" icon={<DownloadOutlined />} onClick={handleDownload}>
          下载文档
        </Button>
      </Space>

      <Card title={document.docTitle}>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="文档类型">
            <Tag color="blue">{document.docType.toUpperCase()}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusMap[document.status]?.color || 'default'}>
              {statusMap[document.status]?.text || '未知'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="文件大小">
            {(document.fileSize / 1024).toFixed(2)} KB
          </Descriptions.Item>
          <Descriptions.Item label="文件哈希">
            {document.fileHash}
          </Descriptions.Item>
          <Descriptions.Item label="浏览次数">
            {document.viewCount}
          </Descriptions.Item>
          <Descriptions.Item label="下载次数">
            {document.downloadCount}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {dayjs(document.createTime).format('YYYY-MM-DD HH:mm:ss')}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {dayjs(document.updateTime).format('YYYY-MM-DD HH:mm:ss')}
          </Descriptions.Item>
          {document.tags && (
            <Descriptions.Item label="标签" span={2}>
              {document.tags.split(',').map((tag, index) => (
                <Tag key={index} color="cyan">
                  {tag.trim()}
                </Tag>
              ))}
            </Descriptions.Item>
          )}
          {document.summary && (
            <Descriptions.Item label="摘要" span={2}>
              {document.summary}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      {document.content && (
        <Card title="文档内容" style={{ marginTop: 16 }}>
          <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
            {document.content}
          </div>
        </Card>
      )}
    </div>
  )
}

export default DocumentDetail

