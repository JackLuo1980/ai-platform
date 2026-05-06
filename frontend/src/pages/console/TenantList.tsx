import { useState } from 'react';
import { Button, Modal, Form, Input, InputNumber, Switch, message, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SettingOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import StatusTag from '@/components/StatusTag';
import { listTenants, createTenant, updateTenant, deleteTenant, toggleTenantStatus, updateTenantQuota } from '@/services/console';

function TenantList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [quotaOpen, setQuotaOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [quotaTarget, setQuotaTarget] = useState<Record<string, unknown> | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  function handleQuota(record: Record<string, unknown>) {
    setQuotaTarget(record);
    setQuotaOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateTenant(editing.id as string, values);
    } else {
      await createTenant(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this tenant?',
      onOk: async function () {
        await deleteTenant(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleToggleStatus(record: Record<string, unknown>) {
    await toggleTenantStatus(record.id as string, !record.enabled);
    message.success('Status updated');
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleQuotaSubmit(values: Record<string, unknown>) {
    if (quotaTarget) {
      await updateTenantQuota(quotaTarget.id as string, values);
      message.success('Quota updated');
      setQuotaOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Code', dataIndex: 'code' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'CPU Quota', dataIndex: 'cpuQuota' },
    { title: 'GPU Quota', dataIndex: 'gpuQuota' },
    { title: 'Storage Quota', dataIndex: 'storageQuota' },
    { title: 'Enabled', dataIndex: 'enabled', render: function (val: boolean, record: Record<string, unknown>) {
      return <Switch checked={val} onChange={function () { handleToggleStatus(record); }} />;
    }},
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" icon={<SettingOutlined />} onClick={function () { handleQuota(record); }}>Quota</Button>
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
        fetchData={listTenants}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Tenant</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Tenant' : 'New Tenant'}
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
      <Modal title="Quota Settings" open={quotaOpen} onCancel={function () { setQuotaOpen(false); }} footer={null} destroyOnClose>
        <Form layout="vertical" onFinish={handleQuotaSubmit} initialValues={quotaTarget || undefined}>
          <Form.Item name="cpuQuota" label="CPU Cores">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuQuota" label="GPU Count">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryQuota" label="Memory (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="storageQuota" label="Storage (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">Save</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default TenantList;
