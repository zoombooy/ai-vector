import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Button, Space, message, Spin } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeftOutlined, EditOutlined, CheckCircleOutlined, PoweroffOutlined } from '@ant-design/icons';
import request from '../../utils/request';

interface ExternalFunction {
  id: number;
  functionName: string;
  displayName: string;
  description: string;
  apiUrl: string;
  httpMethod: string;
  headers: string;
  authType: string;
  authConfig: string;
  parametersSchema: string;
  responseMapping: string;
  timeout: number;
  retryTimes: number;
  status: number;
  category: string;
  createTime: string;
  updateTime: string;
  createBy: string;
  updateBy: string;
}

const FunctionDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [loading, setLoading] = useState(false);
  const [functionData, setFunctionData] = useState<ExternalFunction | null>(null);

  useEffect(() => {
    loadFunctionDetail();
  }, [id]);

  const loadFunctionDetail = async () => {
    setLoading(true);
    try {
      const data = await request.get(`/function/${id}`);
      setFunctionData(data);
    } catch (error) {
      console.error('加载函数详情失败:', error);
      message.error('加载函数详情失败');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!functionData) {
    return null;
  }

  const httpMethodColors: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
  };

  const authTypeColors: Record<string, string> = {
    NONE: 'default',
    BEARER: 'blue',
    API_KEY: 'green',
    BASIC: 'orange',
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/function/list')}
            />
            函数详情
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<EditOutlined />}
            onClick={() => navigate(`/function/edit/${id}`)}
          >
            编辑
          </Button>
        }
      >
        <Descriptions bordered column={2}>
          <Descriptions.Item label="ID">{functionData.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            {functionData.status === 1 ? (
              <Tag color="success" icon={<CheckCircleOutlined />}>
                启用
              </Tag>
            ) : (
              <Tag color="default" icon={<PoweroffOutlined />}>
                禁用
              </Tag>
            )}
          </Descriptions.Item>

          <Descriptions.Item label="函数名称" span={2}>
            <code>{functionData.functionName}</code>
          </Descriptions.Item>

          <Descriptions.Item label="显示名称" span={2}>
            {functionData.displayName}
          </Descriptions.Item>

          <Descriptions.Item label="描述" span={2}>
            {functionData.description}
          </Descriptions.Item>

          <Descriptions.Item label="分类">
            {functionData.category ? <Tag>{functionData.category}</Tag> : '-'}
          </Descriptions.Item>

          <Descriptions.Item label="HTTP方法">
            <Tag color={httpMethodColors[functionData.httpMethod] || 'default'}>
              {functionData.httpMethod}
            </Tag>
          </Descriptions.Item>

          <Descriptions.Item label="API地址" span={2}>
            <code style={{ wordBreak: 'break-all' }}>{functionData.apiUrl}</code>
          </Descriptions.Item>

          <Descriptions.Item label="请求头" span={2}>
            <pre style={{ margin: 0, backgroundColor: '#f5f5f5', padding: 8 }}>
              {functionData.headers || '-'}
            </pre>
          </Descriptions.Item>

          <Descriptions.Item label="认证类型">
            <Tag color={authTypeColors[functionData.authType] || 'default'}>
              {functionData.authType}
            </Tag>
          </Descriptions.Item>

          <Descriptions.Item label="超时时间">
            {functionData.timeout} 秒
          </Descriptions.Item>

          <Descriptions.Item label="重试次数">
            {functionData.retryTimes} 次
          </Descriptions.Item>

          <Descriptions.Item label="认证配置" span={2}>
            {functionData.authConfig ? (
              <pre style={{ margin: 0, backgroundColor: '#f5f5f5', padding: 8 }}>
                {functionData.authConfig}
              </pre>
            ) : (
              '-'
            )}
          </Descriptions.Item>

          <Descriptions.Item label="参数 Schema" span={2}>
            <pre
              style={{
                margin: 0,
                backgroundColor: '#f5f5f5',
                padding: 8,
                maxHeight: 300,
                overflow: 'auto',
              }}
            >
              {functionData.parametersSchema
                ? JSON.stringify(JSON.parse(functionData.parametersSchema), null, 2)
                : '-'}
            </pre>
          </Descriptions.Item>

          <Descriptions.Item label="响应映射" span={2}>
            {functionData.responseMapping ? (
              <pre
                style={{
                  margin: 0,
                  backgroundColor: '#f5f5f5',
                  padding: 8,
                  maxHeight: 300,
                  overflow: 'auto',
                }}
              >
                {JSON.stringify(JSON.parse(functionData.responseMapping), null, 2)}
              </pre>
            ) : (
              '-'
            )}
          </Descriptions.Item>

          <Descriptions.Item label="创建时间">
            {functionData.createTime}
          </Descriptions.Item>

          <Descriptions.Item label="创建人">
            {functionData.createBy || '-'}
          </Descriptions.Item>

          <Descriptions.Item label="更新时间">
            {functionData.updateTime}
          </Descriptions.Item>

          <Descriptions.Item label="更新人">
            {functionData.updateBy || '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
};

export default FunctionDetail;

