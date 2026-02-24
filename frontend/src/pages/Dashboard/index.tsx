import { useEffect, useState } from 'react'
import { Card, Row, Col, Statistic } from 'antd'
import { FileTextOutlined, QuestionCircleOutlined, ApiOutlined, UserOutlined } from '@ant-design/icons'

const Dashboard = () => {
  const [stats] = useState({
    documentCount: 0,
    qaCount: 0,
    modelCount: 0,
    userCount: 0,
  })

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>工作台</h1>
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic
              title="文档总数"
              value={stats.documentCount}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="问答记录"
              value={stats.qaCount}
              prefix={<QuestionCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="AI模型"
              value={stats.modelCount}
              prefix={<ApiOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="用户数量"
              value={stats.userCount}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard

