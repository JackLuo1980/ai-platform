import { useState } from 'react';
import { Button, Modal, Form, Input, InputNumber, Space, message } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import { listQuotas, updateQuota, getQuotaUsage } from '@/services/operation';

function QuotaList() {
  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [form] = Form.useForm();
  const [refreshKey, setRefreshKey] = useState(0);

  async function handleEdit(record: Record<string, unknown>) {
    const usage: any = await getQuotaUsage(record.id as string);
    setEditing({ ...record, usage: usage.data || usage });
    form.setFieldsValue(record);
    setEditOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateQuota(editing.id as string, values);
      message.success('Quota updated');
      setEditOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  const columns = [
    { title: 'Tenant', dataIndex: 'tenantName' },
    { title: 'Resource Pool', dataIndex: 'poolName' },
    { title: 'CPU Limit', dataIndex: 'cpuLimit' },
    { title: 'CPU Used', dataIndex: 'cpuUsed' },
    { title: 'GPU Limit', dataIndex: 'gpuLimit' },
    { title: 'GPU Used', dataIndex: 'gpuUsed' },
    { title: 'Memory Limit (GB)', dataIndex: 'memoryLimit' },
    { title: 'Memory Used (GB)', dataIndex: 'memoryUsed' },
    { title: 'Storage Limit (GB)', dataIndex: 'storageLimit' },
    { title: 'Storage Used (GB)', dataIndex: 'storageUsed' },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>;
      },
    },
  ];

  return (
    <div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listQuotas}
        searchFields={[{ key: 'tenantName', label: 'Tenant' }]}
      />
      <Modal title="Edit Quota" open={editOpen} onCancel={function () { setEditOpen(false); }} onOk={function () { form.submit(); }} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="cpuLimit" label="CPU Cores">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuLimit" label="GPU Cards">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryLimit" label="Memory (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="storageLimit" label="Storage (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default QuotaList;
