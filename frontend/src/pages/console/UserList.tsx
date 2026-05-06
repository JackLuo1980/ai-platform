import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import StatusTag from '@/components/StatusTag';
import { listUsers, createUser, updateUser, deleteUser, assignRoles, listRoles } from '@/services/console';

function UserList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [roleOpen, setRoleOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [roleTarget, setRoleTarget] = useState<Record<string, unknown> | null>(null);
  const [allRoles, setAllRoles] = useState<Record<string, unknown>[]>([]);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handleRoleAssign(record: Record<string, unknown>) {
    setRoleTarget(record);
    const roleRes: any = await listRoles({ size: 999 });
    setAllRoles(roleRes.data?.items || roleRes.items || []);
    setSelectedRoles((record.roles || []) as string[]);
    setRoleOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateUser(editing.id as string, values);
    } else {
      await createUser(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this user?',
      onOk: async function () {
        await deleteUser(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleRoleSubmit() {
    if (roleTarget) {
      await assignRoles(roleTarget.id as string, selectedRoles);
      message.success('Roles assigned');
      setRoleOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  const columns = [
    { title: 'Username', dataIndex: 'username', sorter: true },
    { title: 'Name', dataIndex: 'name' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Phone', dataIndex: 'phone' },
    { title: 'Tenant', dataIndex: 'tenantName' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    {
      title: 'Roles',
      dataIndex: 'roles',
      render: function (roles: string[]) {
        return (roles || []).map(function (r) { return <Tag key={r}>{r}</Tag>; });
      },
    },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" icon={<SafetyCertificateOutlined />} onClick={function () { handleRoleAssign(record); }}>Roles</Button>
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
        fetchData={listUsers}
        searchFields={[{ key: 'username', label: 'Username' }, { key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New User</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit User' : 'New User'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="username" label="Username" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        {editing ? null : (
          <Form.Item name="password" label="Password" rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
        )}
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="email" label="Email">
          <Input />
        </Form.Item>
        <Form.Item name="phone" label="Phone">
          <Input />
        </Form.Item>
        <Form.Item name="tenantId" label="Tenant">
          <Select placeholder="Select tenant" />
        </Form.Item>
      </DrawerForm>
      <Modal title="Assign Roles" open={roleOpen} onCancel={function () { setRoleOpen(false); }} onOk={handleRoleSubmit}>
        <Select
          mode="multiple"
          style={{ width: '100%' }}
          value={selectedRoles}
          onChange={setSelectedRoles}
          options={allRoles.map(function (r: any) { return { label: r.name, value: r.id }; })}
          placeholder="Select roles"
        />
      </Modal>
    </div>
  );
}

export default UserList;
