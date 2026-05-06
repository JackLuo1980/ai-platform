import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ThunderboltOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listFeatureGroups, createFeatureGroup, updateFeatureGroup, deleteFeatureGroup, triggerFeatureComputation } from '@/services/lab';

function FeatureGroupList() {
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
      await updateFeatureGroup(editing.id as string, values);
    } else {
      await createFeatureGroup(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this feature group?',
      onOk: async function () {
        await deleteFeatureGroup(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleCompute(id: string) {
    Modal.confirm({
      title: 'Trigger Computation',
      content: 'Compute features for this group?',
      onOk: async function () {
        await triggerFeatureComputation(id);
        message.success('Computation triggered');
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Entity', dataIndex: 'entity' },
    { title: 'Features', dataIndex: 'featureCount' },
    { title: 'Schedule', dataIndex: 'schedule' },
    { title: 'Last Computed', dataIndex: 'lastComputedAt' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <Tag>{status}</Tag>; } },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<ThunderboltOutlined />} onClick={function () { handleCompute(record.id as string); }}>Compute</Button>
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
        fetchData={listFeatureGroups}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Feature Group</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Feature Group' : 'New Feature Group'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="entity" label="Entity" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="schedule" label="Schedule (Cron)">
          <Input placeholder="0 0 * * *" />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default FeatureGroupList;
