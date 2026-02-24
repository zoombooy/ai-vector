import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Modal,
  message,
  Popconfirm,
  Input,
  Select,
  Card,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  EyeOutlined,
  PoweroffOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const { Search } = Input;
const { Option } = Select;

interface ExternalFunction {
  id: number;
  functionName: string;
  displayName: string;
  description: string;
  apiUrl: string;
  httpMethod: string;
  authType: string;
  status: number;
  category: string;
  timeout: number;
  createTime: string;
  updateTime: string;
}

const FunctionList: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<ExternalFunction[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchCategory, setSearchCategory] = useState<string | undefined>();
  const [searchStatus, setSearchStatus] = useState<number | undefined>();
  const [testModalVisible, setTestModalVisible] = useState(false);
  const [testFunctionId, setTestFunctionId] = useState<number | null>(null);
  const [testParams, setTestParams] = useState('{}');
  const [testResult, setTestResult] = useState('');

  // 加载函数列表
  const loadFunctions = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:8086/function/page', {
        params: {
          current: currentPage,
          size: pageSize,
          category: searchCategory,
          status: searchStatus,
        },
      });

      if (response.data.code === 200) {
        setDataSource(response.data.data.records);
        setTotal(response.data.data.total);
      } else {
        message.error(response.data.message || '加载失败');
      }
    } catch (error) {
      console.error('加载函数列表失败:', error);
      message.error('加载函数列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFunctions();
  }, [currentPage, pageSize, searchCategory, searchStatus]);

  // 删除函数
  const handleDelete = async (id: number) => {
    try {
      const response = await axios.delete(`http://localhost:8086/function/${id}`);
      if (response.data.code === 200) {
        message.success('删除成功');
        loadFunctions();
      } else {
        message.error(response.data.message || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      message.error('删除失败');
    }
  };

  // 启用/禁用函数
  const handleToggleStatus = async (id: number, currentStatus: number) => {
    const newStatus = currentStatus === 1 ? 0 : 1;
    try {
      const response = await axios.put(
        `http://localhost:8086/function/${id}/status?status=${newStatus}`
      );
      if (response.data.code === 200) {
        message.success(newStatus === 1 ? '已启用' : '已禁用');
        loadFunctions();
      } else {
        message.error(response.data.message || '操作失败');
      }
    } catch (error) {
      console.error('操作失败:', error);
      message.error('操作失败');
    }
  };

  // 测试函数
  const handleTest = (id: number) => {
    setTestFunctionId(id);
    setTestParams('{}');
    setTestResult('');
    setTestModalVisible(true);
  };

  const executeTest = async () => {
    if (!testFunctionId) return;

    try {
      const params = JSON.parse(testParams);
      const response = await axios.post(
        `http://localhost:8086/function/${testFunctionId}/test`,
        params
      );

      if (response.data.code === 200) {
        setTestResult(JSON.stringify(response.data.data, null, 2));
        message.success('测试成功');
      } else {
        setTestResult(`错误: ${response.data.message}`);
        message.error(response.data.message || '测试失败');
      }
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || error.message || '测试失败';
      setTestResult(`错误: ${errorMsg}`);
      message.error(errorMsg);
    }
  };

  const columns: ColumnsType<ExternalFunction> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '函数名称',
      dataIndex: 'functionName',
      key: 'functionName',
      width: 150,
      render: (text: string) => <code>{text}</code>,
    },
    {
      title: '显示名称',
      dataIndex: 'displayName',
      key: 'displayName',
      width: 150,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: {
        showTitle: false,
      },
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          {text}
        </Tooltip>
      ),
    },
    {
      title: 'API地址',
      dataIndex: 'apiUrl',
      key: 'apiUrl',
      width: 200,
      ellipsis: {
        showTitle: false,
      },
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          <code style={{ fontSize: '12px' }}>{text}</code>
        </Tooltip>
      ),
    },
    {
      title: 'HTTP方法',
      dataIndex: 'httpMethod',
      key: 'httpMethod',
      width: 100,
      render: (method: string) => {
        const colors: Record<string, string> = {
          GET: 'blue',
          POST: 'green',
          PUT: 'orange',
          DELETE: 'red',
        };
        return <Tag color={colors[method] || 'default'}>{method}</Tag>;
      },
    },
    {
      title: '认证类型',
      dataIndex: 'authType',
      key: 'authType',
      width: 100,
      render: (type: string) => {
        const colors: Record<string, string> = {
          NONE: 'default',
          BEARER: 'blue',
          API_KEY: 'green',
          BASIC: 'orange',
        };
        return <Tag color={colors[type] || 'default'}>{type}</Tag>;
      },
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 100,
      render: (category: string) => category ? <Tag>{category}</Tag> : '-',
    },
    {
      title: '超时(秒)',
      dataIndex: 'timeout',
      key: 'timeout',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) =>
        status === 1 ? (
          <Tag color="success" icon={<CheckCircleOutlined />}>
            启用
          </Tag>
        ) : (
          <Tag color="default" icon={<PoweroffOutlined />}>
            禁用
          </Tag>
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_: any, record: ExternalFunction) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/function/detail/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => navigate(`/function/edit/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="测试">
            <Button
              type="link"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handleTest(record.id)}
            />
          </Tooltip>
          <Tooltip title={record.status === 1 ? '禁用' : '启用'}>
            <Popconfirm
              title={`确定要${record.status === 1 ? '禁用' : '启用'}此函数吗？`}
              onConfirm={() => handleToggleStatus(record.id, record.status)}
            >
              <Button
                type="link"
                size="small"
                icon={<PoweroffOutlined />}
                danger={record.status === 1}
              />
            </Popconfirm>
          </Tooltip>
          <Tooltip title="删除">
            <Popconfirm
              title="确定要删除此函数吗？"
              onConfirm={() => handleDelete(record.id)}
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title="外部函数管理"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/function/add')}
          >
            新增函数
          </Button>
        }
      >
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="选择分类"
            style={{ width: 150 }}
            allowClear
            onChange={setSearchCategory}
          >
            <Option value="天气">天气</Option>
            <Option value="时间">时间</Option>
            <Option value="计算">计算</Option>
            <Option value="股票">股票</Option>
            <Option value="新闻">新闻</Option>
            <Option value="数据库">数据库</Option>
          </Select>
          <Select
            placeholder="选择状态"
            style={{ width: 120 }}
            allowClear
            onChange={setSearchStatus}
          >
            <Option value={1}>启用</Option>
            <Option value={0}>禁用</Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1500 }}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
          }}
        />
      </Card>

      {/* 测试函数 Modal */}
      <Modal
        title="测试函数"
        open={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        onOk={executeTest}
        width={800}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div>
            <div style={{ marginBottom: 8 }}>
              <strong>输入参数 (JSON格式):</strong>
            </div>
            <Input.TextArea
              rows={6}
              value={testParams}
              onChange={(e) => setTestParams(e.target.value)}
              placeholder='{"param1": "value1", "param2": "value2"}'
              style={{ fontFamily: 'monospace' }}
            />
          </div>
          {testResult && (
            <div>
              <div style={{ marginBottom: 8 }}>
                <strong>测试结果:</strong>
              </div>
              <Input.TextArea
                rows={10}
                value={testResult}
                readOnly
                style={{ fontFamily: 'monospace', backgroundColor: '#f5f5f5' }}
              />
            </div>
          )}
        </Space>
      </Modal>
    </div>
  );
};

export default FunctionList;

