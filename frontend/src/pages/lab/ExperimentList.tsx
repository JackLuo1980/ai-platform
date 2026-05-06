import { useState } from 'react';
import { Button, Modal, Form, Input, Space, Tag, message } from 'antd';
import { PlusOutlined, DeleteOutlined, BarChartOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import StatusTag from '@/components/StatusTag';
import { listExperiments, createExperiment, deleteExperiment, getExperimentMetrics } from '@/services/lab';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

function ExperimentList() {
  const [metricOpen, setMetricOpen] = useState(false);
  const [metricData, setMetricData] = useState<Record<string, unknown>[]>([]);
  const [metricKeys, setMetricKeys] = useState<string[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  async function handleMetrics(record: Record<string, unknown>) {
    const res: any = await getExperimentMetrics(record.id as string);
    const data = res.data?.points || res.points || [];
    setMetricData(data);
    if (data.length > 0) {
      setMetricKeys(Object.keys(data[0]).filter(function (k) { return k !== 'step' && k !== 'epoch'; }));
    }
    setMetricOpen(true);
  }

  async function handleCreate() {
    const values = await new Promise<Record<string, unknown> | null>(function (resolve) {
      Modal.confirm({
        title: 'New Experiment',
        content: (
          <Form layout="vertical" id="experiment-form">
            <Form.Item name="name" label="Name" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="Description">
              <Input.TextArea rows={2} />
            </Form.Item>
          </Form>
        ),
        onOk: function () {
          resolve({ name: 'New Experiment' });
        },
        onCancel: function () {
          resolve(null);
        },
      });
    });
    if (values) {
      await createExperiment(values);
      message.success('Created');
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this experiment?',
      onOk: async function () {
        await deleteExperiment(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Workflow', dataIndex: 'workflowName' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Best Metric', dataIndex: 'bestMetric' },
    { title: 'Duration', dataIndex: 'duration' },
    { title: 'Started', dataIndex: 'startedAt' },
    { title: 'Finished', dataIndex: 'finishedAt' },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<BarChartOutlined />} onClick={function () { handleMetrics(record); }}>Metrics</Button>
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
        fetchData={listExperiments}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Experiment</Button>}
      />
      <Modal title="Experiment Metrics" open={metricOpen} onCancel={function () { setMetricOpen(false); }} footer={null} width={900}>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart data={metricData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="step" />
            <YAxis />
            <Tooltip />
            <Legend />
            {metricKeys.map(function (key, i) {
              return <Line key={key} type="monotone" dataKey={key} stroke={['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1'][i % 5]} />;
            })}
          </LineChart>
        </ResponsiveContainer>
      </Modal>
    </div>
  );
}

export default ExperimentList;
