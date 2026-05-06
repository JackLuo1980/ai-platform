import { useState } from 'react';
import { Button, Modal, Form, Input, Checkbox, Space, Row, Col, Card, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listRoles, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '@/services/console';

const PERMISSION_MODULES = [
  'console', 'operation', 'lab', 'inference', 'fastlabel', 'scorecard',
];

const PERMISSION_ACTIONS = ['create', 'read', 'update', 'delete', 'manage'];

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
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this role?',
      onOk: async function () {
        await deleteRole(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handlePermSubmit() {
    if (permTarget) {
      await updateRolePermissions(permTarget.id as string, permissions);
      message.success('Permissions updated');
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
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Code', dataIndex: 'code' },
    { title: 'Description', dataIndex: 'description' },
    { title: 'User Count', dataIndex: 'userCount' },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" icon={<SafetyCertificateOutlined />} onClick={function () { handlePermissions(record); }}>Permissions</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>Delete</Button>
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
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Role</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Role' : 'New Role'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="code" label="Code" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
      <Modal title="Permission Matrix" open={permOpen} onCancel={function () { setPermOpen(false); }} onOk={handlePermSubmit} width={800}>
        <Row gutter={[16, 16]}>
          {PERMISSION_MODULES.map(function (mod) {
            const allChecked = PERMISSION_ACTIONS.every(function (a) { return permissions.includes(mod + ':' + a); });
            return (
              <Col span={12} key={mod}>
                <Card title={mod.charAt(0).toUpperCase() + mod.slice(1)} size="small" extra={<Checkbox checked={allChecked} onChange={function (e) { toggleModule(mod, e.target.checked); }}>All</Checkbox>}>
                  <Space wrap>
                    {PERMISSION_ACTIONS.map(function (action) {
                      const perm = mod + ':' + action;
                      return (
                        <Checkbox key={perm} checked={permissions.includes(perm)} onChange={function () { togglePermission(perm); }}>
                          {action}
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
