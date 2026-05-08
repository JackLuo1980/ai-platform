import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Avatar, Dropdown, Button } from 'antd';
import {
  DashboardOutlined,
  TeamOutlined,
  UserOutlined,
  SafetyCertificateOutlined,
  ProjectOutlined,
  FileSearchOutlined,
  MailOutlined,
  CopyrightOutlined,
  ClusterOutlined,
  DatabaseOutlined,
  AppstoreOutlined,
  CloudServerOutlined,
  ExperimentOutlined,
  CodeOutlined,
  ApartmentOutlined,
  ThunderboltOutlined,
  InboxOutlined,
  StarOutlined,
  FolderOutlined,
  RobotOutlined,
  LineChartOutlined,
  AreaChartOutlined,
  FundProjectionScreenOutlined,
  EyeOutlined,
  SwapOutlined,
  TagOutlined,
  ExportOutlined,
  LogoutOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BarChartOutlined,
  CalculatorOutlined,
  ScissorOutlined,
  BlockOutlined,
  RocketOutlined,
  CloudUploadOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';

const { Sider, Header, Content } = Layout;

const menuItems: MenuProps['items'] = [
  {
    key: 'console',
    icon: <DashboardOutlined />,
    label: '控制台',
    children: [
      { key: '/console/tenants', icon: <TeamOutlined />, label: '租户管理' },
      { key: '/console/users', icon: <UserOutlined />, label: '用户管理' },
      { key: '/console/roles', icon: <SafetyCertificateOutlined />, label: '角色管理' },
      { key: '/console/projects', icon: <ProjectOutlined />, label: '项目管理' },
      { key: '/console/audit-logs', icon: <FileSearchOutlined />, label: '审计日志' },
      { key: '/console/messages', icon: <MailOutlined />, label: '消息中心' },
      { key: '/console/license', icon: <CopyrightOutlined />, label: '许可证' },
    ],
  },
  {
    key: 'operation',
    icon: <ClusterOutlined />,
    label: '运维管理',
    children: [
      { key: '/operation/clusters', icon: <ClusterOutlined />, label: '集群管理' },
      { key: '/operation/pools', icon: <DatabaseOutlined />, label: '资源池' },
      { key: '/operation/quotas', icon: <AppstoreOutlined />, label: '资源配额' },
      { key: '/operation/images', icon: <CloudServerOutlined />, label: '镜像管理' },
      { key: '/operation/environments', icon: <ExperimentOutlined />, label: '环境管理' },
      { key: '/operation/monitoring', icon: <LineChartOutlined />, label: '监控面板' },
    ],
  },
  {
    key: 'lab',
    icon: <ExperimentOutlined />,
    label: '实验室',
    children: [
      { key: '/lab/datasources', icon: <DatabaseOutlined />, label: '数据源' },
      { key: '/lab/datasets', icon: <FolderOutlined />, label: '数据集' },
      { key: '/lab/operators', icon: <CodeOutlined />, label: '算子管理' },
      { key: '/lab/workflows', icon: <ApartmentOutlined />, label: '工作流' },
      { key: '/lab/experiments', icon: <ThunderboltOutlined />, label: '实验管理' },
      { key: '/lab/archives', icon: <InboxOutlined />, label: '模型归档' },
      { key: '/lab/features', icon: <StarOutlined />, label: '特征工程' },
      { key: '/lab/files', icon: <FolderOutlined />, label: '文件管理' },
      { key: '/lab/automl', icon: <ThunderboltOutlined />, label: '自动机器学习' },
    ],
  },
  {
    key: 'inference',
    icon: <RobotOutlined />,
    label: '推理服务',
    children: [
      { key: '/inference/models', icon: <BlockOutlined />, label: '模型管理' },
      { key: '/inference/online-services', icon: <CloudServerOutlined />, label: '在线服务' },
      { key: '/inference/batch-services', icon: <DatabaseOutlined />, label: '批量服务' },
      { key: '/inference/monitoring', icon: <LineChartOutlined />, label: '服务监控' },
      { key: '/inference/evaluations', icon: <FundProjectionScreenOutlined />, label: '模型评估' },
      { key: '/inference/drift', icon: <SwapOutlined />, label: '漂移检测' },
      { key: '/inference/deployment', icon: <RocketOutlined />, label: '部署管理' },
      { key: '/inference/backflow', icon: <CloudUploadOutlined />, label: '数据回流' },
    ],
  },
  {
    key: 'fastlabel',
    icon: <TagOutlined />,
    label: '数据标注',
    children: [
      { key: '/fastlabel/datasets', icon: <DatabaseOutlined />, label: '标注数据集' },
      { key: '/fastlabel/tasks', icon: <BlockOutlined />, label: '标注任务' },
      { key: '/fastlabel/exports', icon: <ExportOutlined />, label: '数据导出' },
    ],
  },
  {
    key: 'scorecard',
    icon: <CalculatorOutlined />,
    label: '评分卡',
    children: [
      { key: '/scorecard/variables', icon: <BarChartOutlined />, label: '变量管理' },
      { key: '/scorecard/binning', icon: <ScissorOutlined />, label: '分箱管理' },
      { key: '/scorecard/models', icon: <BlockOutlined />, label: '评分模型' },
      { key: '/scorecard/scoring', icon: <CalculatorOutlined />, label: '在线评分' },
    ],
  },
];

function BasicLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  function handleMenuClick(info: { key: string }) {
    navigate(info.key);
  }

  function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  }

  const userMenuItems: MenuProps['items'] = [
    { key: 'profile', icon: <UserOutlined />, label: '个人资料' },
    { key: 'settings', icon: <SettingOutlined />, label: '系统设置' },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
  ];

  function handleUserMenuClick(info: { key: string }) {
    if (info.key === 'logout') {
      handleLogout();
    } else if (info.key === 'profile') {
      navigate('/console/profile');
    }
  }

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const openKeys = (() => {
    const path = location.pathname;
    if (path.startsWith('/console')) return ['console'];
    if (path.startsWith('/operation')) return ['operation'];
    if (path.startsWith('/lab')) return ['lab'];
    if (path.startsWith('/inference')) return ['inference'];
    if (path.startsWith('/fastlabel')) return ['fastlabel'];
    if (path.startsWith('/scorecard')) return ['scorecard'];
    return [];
  })();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={240}
        style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0, top: 0, bottom: 0 }}
      >
        <div style={{ height: 48, margin: 12, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ color: '#fff', fontSize: collapsed ? 16 : 18, fontWeight: 600, whiteSpace: 'nowrap' }}>
            {collapsed ? 'AI' : 'AI 平台'}
          </span>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={openKeys}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 240, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={function () { setCollapsed(!collapsed); }}
          />
          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} />
              <span>{user.username || 'Admin'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8, minHeight: 360 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}

export default BasicLayout;
