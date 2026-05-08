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
      title: '确认删除',
      content: '确定要删除该租户吗？',
      onOk: async function () {
        await deleteTenant(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleToggleStatus(record: Record<string, unknown>) {
    await toggleTenantStatus(record.id as string, !record.enabled);
    message.success('状态已更新');
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleQuotaSubmit(values: Record<string, unknown>) {
    if (quotaTarget) {
      await updateTenantQuota(quotaTarget.id as string, values);
      message.success('配额已更新');
      setQuotaOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '编码', dataIndex: 'code' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'CPU配额', dataIndex: 'cpuQuota' },
    { title: 'GPU配额', dataIndex: 'gpuQuota' },
    { title: '存储配额', dataIndex: 'storageQuota' },
    { title: '是否启用', dataIndex: 'enabled', render: function (val: boolean, record: Record<string, unknown>) {
      return <Switch checked={val} onChange={function () { handleToggleStatus(record); }} />;
    }},
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" icon={<SettingOutlined />} onClick={function () { handleQuota(record); }}>配额</Button>
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
        fetchData={listTenants}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建租户</Button>}
      />
      <DrawerForm
        title={editing ? '编辑租户' : '新建租户'}
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
      <Modal title="配额设置" open={quotaOpen} onCancel={function () { setQuotaOpen(false); }} footer={null} destroyOnClose>
        <Form layout="vertical" onFinish={handleQuotaSubmit} initialValues={quotaTarget || undefined}>
          <Form.Item name="cpuQuota" label="CPU核心数">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuQuota" label="GPU数量">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryQuota" label="内存 (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="storageQuota" label="存储 (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">保存</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default TenantList;
