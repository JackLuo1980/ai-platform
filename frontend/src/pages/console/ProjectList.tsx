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
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this project?',
      onOk: async function () {
        await deleteProject(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleAddMember(values: Record<string, unknown>) {
    if (memberTarget) {
      await addProjectMember(memberTarget.id as string, values);
      message.success('Member added');
      const res: any = await listProjectMembers(memberTarget.id as string);
      setMembers(res.data?.items || res.items || []);
    }
  }

  async function handleRemoveMember(userId: string) {
    if (memberTarget) {
      await removeProjectMember(memberTarget.id as string, userId);
      message.success('Member removed');
      const res: any = await listProjectMembers(memberTarget.id as string);
      setMembers(res.data?.items || res.items || []);
    }
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Code', dataIndex: 'code' },
    { title: 'Description', dataIndex: 'description' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Members', dataIndex: 'memberCount' },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" icon={<TeamOutlined />} onClick={function () { handleMembers(record); }}>Members</Button>
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
        fetchData={listProjects}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Project</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Project' : 'New Project'}
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
        <Form.Item name="tenantId" label="Tenant">
          <Select placeholder="Select tenant" />
        </Form.Item>
      </DrawerForm>
      <Drawer title="Project Members" open={memberOpen} onClose={function () { setMemberOpen(false); }} width={480}>
        <div style={{ marginBottom: 16 }}>
          <Form layout="inline" onFinish={handleAddMember}>
            <Form.Item name="userId" rules={[{ required: true }]}>
              <Select placeholder="Select user" style={{ width: 200 }} />
            </Form.Item>
            <Form.Item name="role">
              <Select placeholder="Role" style={{ width: 120 }} options={[{ label: 'Admin', value: 'admin' }, { label: 'Member', value: 'member' }]} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<UserAddOutlined />}>Add</Button>
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
