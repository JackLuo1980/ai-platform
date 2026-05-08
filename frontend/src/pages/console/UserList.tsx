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
      title: '确认删除',
      content: '确定要删除该用户吗？',
      onOk: async function () {
        await deleteUser(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleRoleSubmit() {
    if (roleTarget) {
      await assignRoles(roleTarget.id as string, selectedRoles);
      message.success('角色已分配');
      setRoleOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  const columns = [
    { title: '用户名', dataIndex: 'username', sorter: true },
    { title: '姓名', dataIndex: 'name' },
    { title: '邮箱', dataIndex: 'email' },
    { title: '手机号', dataIndex: 'phone' },
    { title: '租户', dataIndex: 'tenantName' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    {
      title: '角色',
      dataIndex: 'roles',
      render: function (roles: string[]) {
        return (roles || []).map(function (r) { return <Tag key={r}>{r}</Tag>; });
      },
    },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" icon={<SafetyCertificateOutlined />} onClick={function () { handleRoleAssign(record); }}>角色</Button>
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
        fetchData={listUsers}
        searchFields={[{ key: 'username', label: '用户名' }, { key: 'name', label: '姓名' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建用户</Button>}
      />
      <DrawerForm
        title={editing ? '编辑用户' : '新建用户'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        {editing ? null : (
          <Form.Item name="password" label="密码" rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
        )}
        <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="email" label="邮箱">
          <Input />
        </Form.Item>
        <Form.Item name="phone" label="手机号">
          <Input />
        </Form.Item>
        <Form.Item name="tenantId" label="租户">
          <Select placeholder="请选择租户" />
        </Form.Item>
      </DrawerForm>
      <Modal title="分配角色" open={roleOpen} onCancel={function () { setRoleOpen(false); }} onOk={handleRoleSubmit}>
        <Select
          mode="multiple"
          style={{ width: '100%' }}
          value={selectedRoles}
          onChange={setSelectedRoles}
          options={allRoles.map(function (r: any) { return { label: r.name, value: r.id }; })}
          placeholder="请选择角色"
        />
      </Modal>
    </div>
  );
}

export default UserList;
