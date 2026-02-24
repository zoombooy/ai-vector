import React, { useState, useEffect } from 'react';
import {
  Form,
  Input,
  Select,
  Button,
  Card,
  message,
  Space,
  InputNumber,
  Radio,
  Divider,
  Alert,
  Collapse,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import request from '../../utils/request';
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons';

const { TextArea } = Input;
const { Option } = Select;
const { Panel } = Collapse;

const FunctionForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [authType, setAuthType] = useState('NONE');

  const isEdit = !!id;

  // 加载函数详情（编辑模式）
  useEffect(() => {
    if (isEdit) {
      loadFunctionDetail();
    }
  }, [id]);

  const loadFunctionDetail = async () => {
    try {
      const data = await request.get(`/function/${id}`);
      form.setFieldsValue(data);
      setAuthType(data.authType || 'NONE');
    } catch (error) {
      console.error('加载函数详情失败:', error);
      message.error('加载函数详情失败');
    }
  };

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      if (isEdit) {
        await request.put(`/function/${id}`, values);
      } else {
        await request.post('/function', values);
      }
      message.success(isEdit ? '更新成功' : '创建成功');
      navigate('/function/list');
    } catch (error) {
      console.error('提交失败:', error);
      message.error('提交失败');
    } finally {
      setLoading(false);
    }
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
            {isEdit ? '编辑函数' : '新增函数'}
          </Space>
        }
      >
        <Alert
          message="提示"
          description="Function Call 允许大模型在回答问题时自动调用外部接口获取实时数据。请仔细配置函数描述，让AI知道何时调用此函数。"
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
        />

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            httpMethod: 'GET',
            authType: 'NONE',
            timeout: 10,
            retryTimes: 0,
            status: 1,
          }}
        >
          <Divider orientation="left">基本信息</Divider>

          <Form.Item
            label="函数名称"
            name="functionName"
            rules={[
              { required: true, message: '请输入函数名称' },
              {
                pattern: /^[a-z_][a-z0-9_]*$/,
                message: '只能包含小写字母、数字和下划线，且必须以字母或下划线开头',
              },
            ]}
            extra="唯一标识，只能包含小写字母、数字和下划线，例如：get_weather"
          >
            <Input placeholder="get_weather" disabled={isEdit} />
          </Form.Item>

          <Form.Item
            label="显示名称"
            name="displayName"
            rules={[{ required: true, message: '请输入显示名称' }]}
          >
            <Input placeholder="获取天气信息" />
          </Form.Item>

          <Form.Item
            label="函数描述"
            name="description"
            rules={[{ required: true, message: '请输入函数描述' }]}
            extra="详细描述函数的用途和使用场景，帮助AI理解何时调用此函数"
          >
            <TextArea
              rows={4}
              placeholder="获取指定城市的实时天气信息，包括温度、湿度、天气状况等。当用户询问天气相关问题时调用此函数。"
            />
          </Form.Item>

          <Form.Item
            label="分类"
            name="category"
            extra="用于分类管理，例如：天气、股票、新闻等"
          >
            <Select placeholder="选择或输入分类" mode="tags" maxCount={1}>
              <Option value="天气">天气</Option>
              <Option value="时间">时间</Option>
              <Option value="计算">计算</Option>
              <Option value="股票">股票</Option>
              <Option value="新闻">新闻</Option>
              <Option value="数据库">数据库</Option>
            </Select>
          </Form.Item>

          <Divider orientation="left">API 配置</Divider>

          <Form.Item
            label="API 地址"
            name="apiUrl"
            rules={[
              { required: true, message: '请输入API地址' },
              { type: 'url', message: '请输入有效的URL' },
            ]}
          >
            <Input placeholder="https://api.example.com/weather" />
          </Form.Item>

          <Form.Item
            label="HTTP 方法"
            name="httpMethod"
            rules={[{ required: true, message: '请选择HTTP方法' }]}
          >
            <Radio.Group>
              <Radio.Button value="GET">GET</Radio.Button>
              <Radio.Button value="POST">POST</Radio.Button>
              <Radio.Button value="PUT">PUT</Radio.Button>
              <Radio.Button value="DELETE">DELETE</Radio.Button>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            label="请求头"
            name="headers"
            extra='JSON格式，例如：{"Content-Type": "application/json"}'
          >
            <TextArea
              rows={3}
              placeholder='{"Content-Type": "application/json"}'
              style={{ fontFamily: 'monospace' }}
            />
          </Form.Item>

          <Form.Item label="超时时间（秒）" name="timeout">
            <InputNumber min={1} max={300} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="重试次数" name="retryTimes">
            <InputNumber min={0} max={5} style={{ width: '100%' }} />
          </Form.Item>

          <Divider orientation="left">认证配置</Divider>

          <Form.Item
            label="认证类型"
            name="authType"
            rules={[{ required: true, message: '请选择认证类型' }]}
          >
            <Select onChange={setAuthType}>
              <Option value="NONE">无认证</Option>
              <Option value="BEARER">Bearer Token</Option>
              <Option value="API_KEY">API Key</Option>
              <Option value="BASIC">Basic Auth</Option>
            </Select>
          </Form.Item>

          {authType !== 'NONE' && (
            <Form.Item
              label="认证配置"
              name="authConfig"
              rules={[{ required: true, message: '请输入认证配置' }]}
              extra={
                <div>
                  {authType === 'BEARER' && 'JSON格式：{"token": "your-token"}'}
                  {authType === 'API_KEY' &&
                    'JSON格式：{"api_key": "your-key", "header_name": "X-API-Key"}'}
                  {authType === 'BASIC' &&
                    'JSON格式：{"username": "user", "password": "pass"}'}
                </div>
              }
            >
              <TextArea
                rows={3}
                placeholder={
                  authType === 'BEARER'
                    ? '{"token": "your-token"}'
                    : authType === 'API_KEY'
                    ? '{"api_key": "your-key", "header_name": "X-API-Key"}'
                    : '{"username": "user", "password": "pass"}'
                }
                style={{ fontFamily: 'monospace' }}
              />
            </Form.Item>
          )}

          <Divider orientation="left">参数配置</Divider>

          <Collapse ghost>
            <Panel header="📖 参数 Schema 说明" key="1">
              <Alert
                message="使用 JSON Schema 格式定义函数参数"
                description={
                  <div>
                    <p>示例：</p>
                    <pre style={{ backgroundColor: '#f5f5f5', padding: 8 }}>
                      {`{
  "type": "object",
  "properties": {
    "location": {
      "type": "string",
      "description": "城市名称，例如：北京、上海"
    }
  },
  "required": ["location"]
}`}
                    </pre>
                  </div>
                }
                type="info"
              />
            </Panel>
          </Collapse>

          <Form.Item
            label="参数 Schema"
            name="parametersSchema"
            rules={[{ required: true, message: '请输入参数Schema' }]}
            extra="JSON Schema 格式，定义函数需要哪些参数"
          >
            <TextArea
              rows={8}
              placeholder={`{
  "type": "object",
  "properties": {
    "param1": {
      "type": "string",
      "description": "参数描述"
    }
  },
  "required": ["param1"]
}`}
              style={{ fontFamily: 'monospace' }}
            />
          </Form.Item>

          <Collapse ghost>
            <Panel header="📖 响应映射说明" key="2">
              <Alert
                message="使用 JSONPath 从 API 响应中提取需要的字段"
                description={
                  <div>
                    <p>示例：</p>
                    <pre style={{ backgroundColor: '#f5f5f5', padding: 8 }}>
                      {`API 响应：
{
  "data": {
    "temperature": 25,
    "humidity": 60
  }
}

响应映射：
{
  "temperature": "$.data.temperature",
  "humidity": "$.data.humidity"
}`}
                    </pre>
                  </div>
                }
                type="info"
              />
            </Panel>
          </Collapse>

          <Form.Item
            label="响应映射"
            name="responseMapping"
            extra="JSONPath 格式，提取 API 响应中的有用字段"
          >
            <TextArea
              rows={6}
              placeholder={`{
  "field1": "$.data.field1",
  "field2": "$.data.field2"
}`}
              style={{ fontFamily: 'monospace' }}
            />
          </Form.Item>

          <Divider orientation="left">其他设置</Divider>

          <Form.Item label="状态" name="status">
            <Radio.Group>
              <Radio value={1}>启用</Radio>
              <Radio value={0}>禁用</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                {isEdit ? '更新' : '创建'}
              </Button>
              <Button onClick={() => navigate('/function/list')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default FunctionForm;

