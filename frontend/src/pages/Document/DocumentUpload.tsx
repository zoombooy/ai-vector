import { useState } from 'react'
import { Form, Input, Select, Upload, Button, Card, message } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import type { UploadFile } from 'antd'
import request from '@/utils/request'

const DocumentUpload = () => {
  const [form] = Form.useForm()
  const [uploading, setUploading] = useState(false)
  const [fileList, setFileList] = useState<UploadFile[]>([])

  const handleSubmit = async (values: any) => {
    if (fileList.length === 0) {
      message.error('请选择要上传的文件')
      return
    }

    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('categoryId', values.categoryId)
      formData.append('title', values.title)
      formData.append('file', fileList[0] as any)
      if (values.tags) {
        formData.append('tags', values.tags)
      }

      await request.post('/document/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      message.success('上传成功')
      form.resetFields()
      setFileList([])
    } catch (error) {
      console.error('上传失败:', error)
    } finally {
      setUploading(false)
    }
  }

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>上传文档</h1>
      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          style={{ maxWidth: 600 }}
        >
          <Form.Item
            label="文档标题"
            name="title"
            rules={[{ required: true, message: '请输入文档标题' }]}
          >
            <Input placeholder="请输入文档标题" />
          </Form.Item>

          <Form.Item
            label="分类"
            name="categoryId"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            <Select placeholder="请选择分类">
              <Select.Option value={1}>数据库脚本</Select.Option>
              <Select.Option value={2}>业务知识</Select.Option>
              <Select.Option value={3}>技术文档</Select.Option>
              <Select.Option value={4}>其他资料</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="标签" name="tags">
            <Input placeholder="多个标签用逗号分隔" />
          </Form.Item>

          <Form.Item label="文件">
            <Upload
              fileList={fileList}
              beforeUpload={(file) => {
                const isSupported = ['pdf', 'doc', 'docx', 'txt', 'md'].some(
                  (ext) => file.name.toLowerCase().endsWith(ext)
                )
                if (!isSupported) {
                  message.error('只支持 PDF、Word、TXT、Markdown 格式的文件')
                  return false
                }
                const isLt50M = file.size / 1024 / 1024 < 50
                if (!isLt50M) {
                  message.error('文件大小不能超过 50MB')
                  return false
                }
                setFileList([file])
                return false
              }}
              onRemove={() => {
                setFileList([])
              }}
            >
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </Upload>
            <div style={{ marginTop: 8, color: '#999' }}>
              支持格式：PDF、Word、TXT、Markdown，文件大小不超过 50MB
            </div>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={uploading}>
              上传
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default DocumentUpload

