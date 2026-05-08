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
      message.success('配额已更新');
      setEditOpen(false);
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  async function handleCreate(values: Record<string, unknown>) {
    await createQuota(values);
    message.success('配额已创建');
    setCreateOpen(false);
    createForm.resetFields();
    setRefreshKey(function (k) { return k + 1; });
  }

  var priorityColors: Record<string, string> = { HIGH: 'red', MEDIUM: 'blue', LOW: 'green' };

  const columns = [
    { title: '租户', dataIndex: 'tenantName' },
    { title: '资源池', dataIndex: 'poolName' },
    { title: '优先级', dataIndex: 'priority', render: function (p: string) { return <Tag color={priorityColors[p] || 'default'}>{p || 'MEDIUM'}</Tag>; } },
    { title: 'CPU 上限', dataIndex: 'cpuLimit' },
    { title: 'CPU 保底', dataIndex: 'cpuGuaranteed', render: function (v: number) { return v || 0; } },
    { title: 'GPU 上限', dataIndex: 'gpuLimit' },
    { title: 'GPU 保底', dataIndex: 'gpuGuaranteed', render: function (v: number) { return v || 0; } },
    { title: '内存上限 (GB)', dataIndex: 'memoryLimit' },
    { title: '内存保底', dataIndex: 'memoryGuaranteed', render: function (v: number) { return v || 0; } },
    { title: '抢占策略', dataIndex: 'preemptionPolicy', render: function (p: string) {
      var labels: Record<string, string> = { NONE: '无', PREEMPT_LOWER: '抢占低优先级', PREEMPT_ALL: '抢占全部' };
      var colors: Record<string, string> = { NONE: 'default', PREEMPT_LOWER: 'orange', PREEMPT_ALL: 'red' };
      return <Tag color={colors[p] || 'default'}>{labels[p] || p || 'NONE'}</Tag>;
    }},
    { title: '状态', dataIndex: 'status', render: function (s: string) { return <Tag color={s === 'ACTIVE' ? 'green' : 'red'}>{s || 'ACTIVE'}</Tag>; } },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>;
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateOpen(true); }}>新增配额</Button>
      </div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listQuotas}
        searchFields={[{ key: 'tenantName', label: '租户' }]}
      />
      <Modal title="编辑配额" open={editOpen} onCancel={function () { setEditOpen(false); }} onOk={function () { form.submit(); }} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="priority" label="优先级">
            <Select options={[{ value: 'HIGH', label: '高' }, { value: 'MEDIUM', label: '中' }, { value: 'LOW', label: '低' }]} />
          </Form.Item>
          <Form.Item name="cpuLimit" label="CPU 核数上限">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="cpuGuaranteed" label="CPU 核数保底">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuLimit" label="GPU 卡数上限">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gpuGuaranteed" label="GPU 卡数保底">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryLimit" label="内存上限 (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="memoryGuaranteed" label="内存保底 (GB)">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="preemptionPolicy" label="抢占策略">
            <Select options={[{ value: 'NONE', label: '无' }, { value: 'PREEMPT_LOWER', label: '抢占低优先级' }, { value: 'PREEMPT_ALL', label: '抢占全部' }]} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="新增配额" open={createOpen} onCancel={function () { setCreateOpen(false); }} onOk={function () { createForm.submit(); }} destroyOnClose>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="tenantId" label="租户 ID" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="resourcePoolId" label="资源池 ID" rules={[{ required: true }]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="priority" label="优先级" initialValue="MEDIUM">
            <Select options={[{ value: 'HIGH', label: '高' }, { value: 'MEDIUM', label: '中' }, { value: 'LOW', label: '低' }]} />
          </Form.Item>
          <Form.Item name="cpuLimit" label="CPU 核数上限"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="cpuGuaranteed" label="CPU 保底" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="gpuLimit" label="GPU 卡数上限"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="gpuGuaranteed" label="GPU 保底" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="memoryLimit" label="内存上限 (GB)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="memoryGuaranteed" label="内存保底 (GB)" initialValue={0}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="preemptionPolicy" label="抢占策略" initialValue="NONE">
            <Select options={[{ value: 'NONE', label: '无' }, { value: 'PREEMPT_LOWER', label: '抢占低优先级' }, { value: 'PREEMPT_ALL', label: '抢占全部' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default QuotaList;
