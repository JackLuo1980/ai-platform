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
        title: '新建实验',
        content: (
          <Form layout="vertical" id="experiment-form">
            <Form.Item name="name" label="名称" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="描述">
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
      message.success('已创建');
      setRefreshKey(function (k) { return k + 1; });
    }
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定删除该实验？',
      onOk: async function () {
        await deleteExperiment(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '工作流', dataIndex: 'workflowName' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '最佳指标', dataIndex: 'bestMetric' },
    { title: '耗时', dataIndex: 'duration' },
    { title: '开始时间', dataIndex: 'startedAt' },
    { title: '结束时间', dataIndex: 'finishedAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<BarChartOutlined />} onClick={function () { handleMetrics(record); }}>指标</Button>
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
        fetchData={listExperiments}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建实验</Button>}
      />
      <Modal title="实验指标" open={metricOpen} onCancel={function () { setMetricOpen(false); }} footer={null} width={900}>
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
