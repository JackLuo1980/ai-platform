import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, Drawer, List, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, TeamOutlined, UserAddOutlined, DeleteColumnOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import StatusTag from '@/components/StatusTag';
import { listProjects, createProject, updateProject, deleteProject, listProjectMembers, addProjectMember, removeProjectMember } from '@/services/console';

function ProjectList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [memberOpen, setMemberOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [memberTarget, setMemberTarget] = useState<Record<string, unknown> | null>(null);
  const [members, setMembers] = useState<Record<string, unknown>[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handleMembers(record: Record<string, unknown>) {
    setMemberTarget(record);
    const res: any = await listProjectMembers(record.id as string);
    setMembers(res.data?.items || res.items || []);
    setMemberOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateProject(editing.id as string, values);
    } else {
      await createProject(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该项目吗？',
      onOk: async function () {
        await deleteProject(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleAddMember(values: Record<string, unknown>) {
    if (memberTarget) {
      await addProjectMember(memberTarget.id as string, values);
      message.success('成员已添加');
      const res: any = await listProjectMembers(memberTarget.id as string);
      setMembers(res.data?.items || res.items || []);
    }
  }

  async function handleRemoveMember(userId: string) {
    if (memberTarget) {
      await removeProjectMember(memberTarget.id as string, userId);
      message.success('成员已移除');
      const res: any = await listProjectMembers(memberTarget.id as string);
      setMembers(res.data?.items || res.items || []);
    }
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '编码', dataIndex: 'code' },
    { title: '描述', dataIndex: 'description' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '成员数', dataIndex: 'memberCount' },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" icon={<TeamOutlined />} onClick={function () { handleMembers(record); }}>成员</Button>
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
        fetchData={listProjects}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建项目</Button>}
      />
      <DrawerForm
        title={editing ? '编辑项目' : '新建项目'}
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
        <Form.Item name="tenantId" label="租户">
          <Select placeholder="请选择租户" />
        </Form.Item>
      </DrawerForm>
      <Drawer title="项目成员" open={memberOpen} onClose={function () { setMemberOpen(false); }} width={480}>
        <div style={{ marginBottom: 16 }}>
          <Form layout="inline" onFinish={handleAddMember}>
            <Form.Item name="userId" rules={[{ required: true }]}>
              <Select placeholder="请选择用户" style={{ width: 200 }} />
            </Form.Item>
            <Form.Item name="role">
              <Select placeholder="角色" style={{ width: 120 }} options={[{ label: '管理员', value: 'admin' }, { label: '成员', value: 'member' }]} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<UserAddOutlined />}>添加</Button>
            </Form.Item>
          </Form>
        </div>
        <List
          dataSource={members}
          renderItem={function (item: any) {
            return (
              <List.Item
                actions={[<Button type="link" danger size="small" icon={<DeleteColumnOutlined />} onClick={function () { handleRemoveMember(item.userId); }} />]}
              >
                <List.Item.Meta title={item.userName} description={<Tag>{item.role}</Tag>} />
              </List.Item>
            );
          }}
        />
      </Drawer>
    </div>
  );
}

export default ProjectList;
