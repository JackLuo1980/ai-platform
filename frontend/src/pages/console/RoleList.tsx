import { useState } from 'react';
import { Button, Modal, Form, Input, Checkbox, Space, Row, Col, Card, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listRoles, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '@/services/console';

const PERMISSION_MODULES = [
  'console', 'operation', 'lab', 'inference', 'fastlabel', 'scorecard',
];

const MODULE_LABELS: Record<string, string> = {
  console: '控制台', operation: '运营', lab: '实验室', inference: '推理', fastlabel: '标注', scorecard: '评分卡',
};

const PERMISSION_ACTIONS = ['create', 'read', 'update', 'delete', 'manage'];

const ACTION_LABELS: Record<string, string> = {
  create: '创建', read: '查看', update: '修改', delete: '删除', manage: '管理',
};

function RoleList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [permOpen, setPermOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [permTarget, setPermTarget] = useState<Record<string, unknown> | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handlePermissions(record: Record<string, unknown>) {
    setPermTarget(record);
    const res: any = await getRolePermissions(record.id as string);
    setPermissions(res.data?.permissions || res.permissions || []);
    setPermOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateRole(editing.id as string, values);
    } else {
      await createRole(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该角色吗？',
      onOk: async function () {
        await deleteRole(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handlePermSubmit() {
    if (permTarget) {
      await updateRolePermissions(permTarget.id as string, permissions);
      message.success('权限已更新');
      setPermOpen(false);
    }
  }

  function togglePermission(perm: string) {
    setPermissions(function (prev) {
      return prev.includes(perm) ? prev.filter(function (p) { return p !== perm; }) : [...prev, perm];
    });
  }

  function toggleModule(moduleName: string, checked: boolean) {
    setPermissions(function (prev) {
      const other = prev.filter(function (p) { return !p.startsWith(moduleName + ':'); });
      if (checked) {
        return [...other, ...PERMISSION_ACTIONS.map(function (a) { return moduleName + ':' + a; })];
      }
      return other;
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '编码', dataIndex: 'code' },
    { title: '描述', dataIndex: 'description' },
    { title: '用户数', dataIndex: 'userCount' },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" icon={<SafetyCertificateOutlined />} onClick={function () { handlePermissions(record); }}>权限</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>删除</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listRoles}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建角色</Button>}
      />
      <DrawerForm
        title={editing ? '编辑角色' : '新建角色'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="code" label="编码" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
      <Modal title="权限矩阵" open={permOpen} onCancel={function () { setPermOpen(false); }} onOk={handlePermSubmit} width={800}>
        <Row gutter={[16, 16]}>
          {PERMISSION_MODULES.map(function (mod) {
            const allChecked = PERMISSION_ACTIONS.every(function (a) { return permissions.includes(mod + ':' + a); });
            return (
              <Col span={12} key={mod}>
                <Card title={MODULE_LABELS[mod] || mod} size="small" extra={<Checkbox checked={allChecked} onChange={function (e) { toggleModule(mod, e.target.checked); }}>全部</Checkbox>}>
                  <Space wrap>
                    {PERMISSION_ACTIONS.map(function (action) {
                      const perm = mod + ':' + action;
                      return (
                        <Checkbox key={perm} checked={permissions.includes(perm)} onChange={function () { togglePermission(perm); }}>
                          {ACTION_LABELS[action] || action}
                        </Checkbox>
                      );
                    })}
                  </Space>
                </Card>
              </Col>
            );
          })}
        </Row>
      </Modal>
    </div>
  );
}

export default RoleList;
