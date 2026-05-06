import { useState } from 'react';
import { Button, Modal, Form, Input, InputNumber, Space, Row, Col, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, BarChartOutlined } from '@ant-design/icons';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listResourcePools, createResourcePool, updateResourcePool, deleteResourcePool, getResourcePoolUsage } from '@/services/operation';

function ResourcePoolList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [chartOpen, setChartOpen] = useState(false);
  const [chartData, setChartData] = useState<Record<string, unknown>[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handleUsage(record: Record<string, unknown>) {
    const res: any = await getResourcePoolUsage(record.id as string);
    setChartData(res.data?.items || res.items || []);
    setChartOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateResourcePool(editing.id as string, values);
    } else {
      await createResourcePool(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this resource pool?',
      onOk: async function () {
        await deleteResourcePool(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Cluster', dataIndex: 'clusterName' },
    { title: 'Type', dataIndex: 'type' },
    { title: 'CPU Used/Total', dataIndex: 'cpuUsage', render: function (v: number, r: any) { return `${v || 0}/${r.cpuTotal || 0}`; } },
    { title: 'GPU Used/Total', dataIndex: 'gpuUsage', render: function (v: number, r: any) { return `${v || 0}/${r.gpuTotal || 0}`; } },
    { title: 'Memory Used/Total', dataIndex: 'memoryUsage', render: function (v: number, r: any) { return `${v || 0}/${r.memoryTotal || 0} GB`; } },
    { title: 'Status', dataIndex: 'status' },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<BarChartOutlined />} onClick={function () { handleUsage(record); }}>Usage</Button>
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
        fetchData={listResourcePools}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Pool</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Pool' : 'New Pool'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="clusterId" label="Cluster" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="Type">
          <Input />
        </Form.Item>
        <Form.Item name="cpuTotal" label="CPU Cores">
          <InputNumber min={1} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="gpuTotal" label="GPU Cards">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="memoryTotal" label="Memory (GB)">
          <InputNumber min={1} style={{ width: '100%' }} />
        </Form.Item>
      </DrawerForm>
      <Modal title="Resource Usage" open={chartOpen} onCancel={function () { setChartOpen(false); }} footer={null} width={700}>
        <ResponsiveContainer width="100%" height={350}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="used" fill="#1890ff" name="Used" />
            <Bar dataKey="total" fill="#e8e8e8" name="Total" />
          </BarChart>
        </ResponsiveContainer>
      </Modal>
    </div>
  );
}

export default ResourcePoolList;
