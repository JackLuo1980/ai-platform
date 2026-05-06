import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listEnvironments, createEnvironment, updateEnvironment, deleteEnvironment } from '@/services/operation';

function EnvironmentList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateEnvironment(editing.id as string, values);
    } else {
      await createEnvironment(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this environment?',
      onOk: async function () {
        await deleteEnvironment(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Image', dataIndex: 'imageName' },
    { title: 'Type', dataIndex: 'type' },
    { title: 'Description', dataIndex: 'description', ellipsis: true },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
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
        fetchData={listEnvironments}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Environment</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Environment' : 'New Environment'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="imageId" label="Image" rules={[{ required: true }]}>
          <Select placeholder="Select image" />
        </Form.Item>
        <Form.Item name="type" label="Type">
          <Select options={[{ label: 'Notebook', value: 'notebook' }, { label: 'Training', value: 'training' }, { label: 'Inference', value: 'inference' }]} />
        </Form.Item>
        <Form.Item name="pipPackages" label="Pip Packages">
          <Select mode="tags" placeholder="Add packages" />
        </Form.Item>
        <Form.Item name="envVars" label="Environment Variables">
          <Input.TextArea rows={3} placeholder="KEY=VALUE per line" />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default EnvironmentList;
