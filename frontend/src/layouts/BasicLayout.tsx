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
    label: 'Console',
    children: [
      { key: '/console/tenants', icon: <TeamOutlined />, label: 'Tenants' },
      { key: '/console/users', icon: <UserOutlined />, label: 'Users' },
      { key: '/console/roles', icon: <SafetyCertificateOutlined />, label: 'Roles' },
      { key: '/console/projects', icon: <ProjectOutlined />, label: 'Projects' },
      { key: '/console/audit-logs', icon: <FileSearchOutlined />, label: 'Audit Logs' },
      { key: '/console/messages', icon: <MailOutlined />, label: 'Messages' },
      { key: '/console/license', icon: <CopyrightOutlined />, label: 'License' },
    ],
  },
  {
    key: 'operation',
    icon: <ClusterOutlined />,
    label: 'Operation',
    children: [
      { key: '/operation/clusters', icon: <ClusterOutlined />, label: 'Clusters' },
      { key: '/operation/pools', icon: <DatabaseOutlined />, label: 'Resources' },
      { key: '/operation/quotas', icon: <AppstoreOutlined />, label: 'Quotas' },
      { key: '/operation/images', icon: <CloudServerOutlined />, label: 'Images' },
      { key: '/operation/environments', icon: <ExperimentOutlined />, label: 'Environments' },
      { key: '/operation/monitoring', icon: <LineChartOutlined />, label: 'Monitoring' },
    ],
  },
  {
    key: 'lab',
    icon: <ExperimentOutlined />,
    label: 'Lab',
    children: [
      { key: '/lab/datasources', icon: <DatabaseOutlined />, label: 'Data Sources' },
      { key: '/lab/datasets', icon: <FolderOutlined />, label: 'Datasets' },
      { key: '/lab/operators', icon: <CodeOutlined />, label: 'Operators' },
      { key: '/lab/workflows', icon: <ApartmentOutlined />, label: 'Workflows' },
      { key: '/lab/experiments', icon: <ThunderboltOutlined />, label: 'Experiments' },
      { key: '/lab/archives', icon: <InboxOutlined />, label: 'Archives' },
      { key: '/lab/features', icon: <StarOutlined />, label: 'Features' },
      { key: '/lab/files', icon: <FolderOutlined />, label: 'Files' },
      { key: '/lab/automl', icon: <ThunderboltOutlined />, label: 'AutoML' },
    ],
  },
  {
    key: 'inference',
    icon: <RobotOutlined />,
    label: 'Inference',
    children: [
      { key: '/inference/models', icon: <BlockOutlined />, label: 'Models' },
      { key: '/inference/online-services', icon: <CloudServerOutlined />, label: 'Online Services' },
      { key: '/inference/batch-services', icon: <DatabaseOutlined />, label: 'Batch Services' },
      { key: '/inference/monitoring', icon: <LineChartOutlined />, label: 'Monitoring' },
      { key: '/inference/evaluations', icon: <FundProjectionScreenOutlined />, label: 'Evaluations' },
      { key: '/inference/drift', icon: <SwapOutlined />, label: 'Drift' },
      { key: '/inference/deployment', icon: <RocketOutlined />, label: 'Deployment' },
      { key: '/inference/backflow', icon: <CloudUploadOutlined />, label: 'Data Backflow' },
    ],
  },
  {
    key: 'fastlabel',
    icon: <TagOutlined />,
    label: 'FastLabel',
    children: [
      { key: '/fastlabel/datasets', icon: <DatabaseOutlined />, label: 'Datasets' },
      { key: '/fastlabel/tasks', icon: <BlockOutlined />, label: 'Tasks' },
      { key: '/fastlabel/exports', icon: <ExportOutlined />, label: 'Exports' },
    ],
  },
  {
    key: 'scorecard',
    icon: <CalculatorOutlined />,
    label: 'Scorecard',
    children: [
      { key: '/scorecard/variables', icon: <BarChartOutlined />, label: 'Variables' },
      { key: '/scorecard/binning', icon: <ScissorOutlined />, label: 'Binning' },
      { key: '/scorecard/models', icon: <BlockOutlined />, label: 'Models' },
      { key: '/scorecard/scoring', icon: <CalculatorOutlined />, label: 'Scoring' },
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
    { key: 'profile', icon: <UserOutlined />, label: 'Profile' },
    { key: 'settings', icon: <SettingOutlined />, label: 'Settings' },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', danger: true },
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
            {collapsed ? 'AI' : 'AI Platform'}
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
