import { useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { Layout as AntLayout, Menu, Avatar, Dropdown } from 'antd'
import {
  DashboardOutlined,
  FileTextOutlined,
  QuestionCircleOutlined,
  ApiOutlined,
  UserOutlined,
  LogoutOutlined,
  TeamOutlined,
  ApartmentOutlined,
  FunctionOutlined,
  RobotOutlined,
  PartitionOutlined,
  CloudServerOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'
import './index.css'

const { Header, Sider, Content } = AntLayout

const Layout = () => {
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(false)
  
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  const menuItems: MenuProps['items'] = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '工作台',
    },
    {
      key: '/document',
      icon: <FileTextOutlined />,
      label: '知识库',
      children: [
        { key: '/document/list', label: '文档列表' },
        { key: '/document/upload', label: '上传文档' },
      ],
    },
    {
      key: '/qa',
      icon: <QuestionCircleOutlined />,
      label: 'AI问答',
    },
    {
      key: '/model',
      icon: <ApiOutlined />,
      label: '模型管理',
    },
    {
      key: '/function',
      icon: <FunctionOutlined />,
      label: 'Function Call',
      children: [
        { key: '/function/list', label: '函数列表' },
      ],
    },
    {
      key: '/agent',
      icon: <RobotOutlined />,
      label: 'Agent管理',
      children: [
        { key: '/agent', label: 'Agent概览' },
        { key: '/agent/list', label: 'Agent列表' },
        { key: '/agent/history', label: '执行历史' },
      ],
    },
    {
      key: '/workflow',
      icon: <PartitionOutlined />,
      label: '工作流编排',
      children: [
        { key: '/workflow/list', label: '工作流列表' },
      ],
    },
    {
      key: '/mcp',
      icon: <CloudServerOutlined />,
      label: 'MCP工具',
      children: [
        { key: '/mcp/servers', label: 'MCP Server管理' },
      ],
    },
    {
      key: '/user',
      icon: <UserOutlined />,
      label: '用户管理',
      children: [
        {
          key: '/user/list',
          icon: <UserOutlined />,
          label: '用户列表',
        },
        {
          key: '/user/role',
          icon: <TeamOutlined />,
          label: '角色管理',
        },
        {
          key: '/user/organization',
          icon: <ApartmentOutlined />,
          label: '组织管理',
        },
      ],
    },
  ]

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        navigate('/login')
      },
    },
  ]

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key)
  }

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div className="logo">AI知识库</div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['/dashboard']}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header className="header">
          <div className="header-right">
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div className="user-info">
                <Avatar icon={<UserOutlined />} />
                <span className="username">{userInfo.realName || userInfo.username}</span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content className="content">
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout

