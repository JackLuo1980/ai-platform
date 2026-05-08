import { useState } from 'react';
import { Button, Modal, Form, Input, InputNumber, Space, Select, Tag, message } from 'antd';
import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import { listQuotas, updateQuota, getQuotaUsage, createQuota } from '@/services/operation';

function QuotaList() {
  const [editOpen, setEditOpen] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [form] = Form.useForm();
  const [createForm] = Form.useForm();
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

  async function handleCreate(values: Record<string, unknown>) {
    await createQuota(values);
    message.success('Quota created');
    setCreateOpen(false);
    createForm.resetFields();
    setRefreshKey(function (k) { return k + 1; });
  }

  var priorityColors: Record<string, string> = { HIGH: 'red', MEDIUM: 'blue', LOW: 'green' };

  const columns = [
    { title: 'Tenant', dataIndex: 'tenantName' },
    { title: 'Resource Pool', dataIndex: 'poolName' },
    { title: 'Priority', dataIndex: 'priority', render: function (p: string) { return <Tag color={priorityColors[p] || 'default'}>{p || 'MEDIUM'}</Tag>; } },
    { title: 'CPU Limit', dataIndex: 'cpuLimit' },
    { title: 'CPU Guaranteed', dataIndex: 'cpuGuaranteed', render: function (v: number) { return v || 0; } },
    { title: 'GPU Limit', dataIndex: 'gpuLimit' },
    { title: 'GPU Guaranteed', dataIndex: 'gpuGuaranteed', render: function (v: number) { return v || 0; } },
    { title: 'Memory Limit (GB)', dataIndex: 'memoryLimit' },
    { title: 'Memory Guaranteed', dataIndex: 'memoryGuaranteed', render: function (v: number) { return v || 0; } },
    { title: 'Preemption', dataIndex: 'preemptionPolicy', render: function (p: string) {
      var labels: Record<string, string> = { NONE: 'None', PREEMPT_LOWER: 'Preempt Lower', PREEMPT_ALL: 'Preempt All' };
      var colors: Record<string, string> = { NONE: 'default', PREEMPT_LOWER: 'orange', PREEMPT_ALL: 'red' };
      return <Tag color={colors[p] || 'default'}>{labels[p] || p || 'NONE'}</Tag>;
    }},
    { title: 'Status', dataIndex: 'status', render: function (s: string) { return <Tag color={s === 'ACTIVE' ? 'green' : 'red'}>{s || 'ACTIVE'}</Tag>; } },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>;
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateOpen(true); }}>Create Quota</Button>
      </div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listQuotas}
        searchFields={[{ key: 'tenantName', label: 'Tenant' }]}
      />
      <Modal title="Edit Quota" open={editOpen} onCancel={function () { setEditOpen(false); }} onOk={function () { form.submit(); }} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="priority" label="Priority">
            <Select options={[{ value: 'HIGH', label: 'High' }, { value: 'MEDIUM', label: 'Medium' }, { value: 'LOW', label: 'Low' }]} />
          </Form.Item>
          <Form.Item name="cpuLimit" label="CPU Cores Limit">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="cpuGuaranteed" label="CPU Cores Guaranteed">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuLimit" label="GPU Cards Limit">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuGuaranteed" label="GPU Cards Guaranteed">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryLimit" label="Memory Limit (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryGuaranteed" label="Memory Guaranteed (GB)">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="preemptionPolicy" label="Preemption Policy">
            <Select options={[{ value: 'NONE', label: 'None' }, { value: 'PREEMPT_LOWER', label: 'Preempt Lower Priority' }, { value: 'PREEMPT_ALL', label: 'Preempt All' }]} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="Create Quota" open={createOpen} onCancel={function () { setCreateOpen(false); }} onOk={function () { createForm.submit(); }} destroyOnClose>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="tenantId" label="Tenant ID" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="resourcePoolId" label="Resource Pool ID" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="priority" label="Priority" initialValue="MEDIUM">
            <Select options={[{ value: 'HIGH', label: 'High' }, { value: 'MEDIUM', label: 'Medium' }, { value: 'LOW', label: 'Low' }]} />
          </Form.Item>
          <Form.Item name="cpuLimit" label="CPU Cores Limit"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="cpuGuaranteed" label="CPU Guaranteed" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="gpuLimit" label="GPU Cards Limit"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="gpuGuaranteed" label="GPU Guaranteed" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="memoryLimit" label="Memory Limit (GB)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="memoryGuaranteed" label="Memory Guaranteed (GB)" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="preemptionPolicy" label="Preemption Policy" initialValue="NONE">
            <Select options={[{ value: 'NONE', label: 'None' }, { value: 'PREEMPT_LOWER', label: 'Preempt Lower Priority' }, { value: 'PREEMPT_ALL', label: 'Preempt All' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default QuotaList;
