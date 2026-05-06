import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, Input, InputNumber, message, Tag, Space, Descriptions } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { getScModels, trainScModel, getScModelReport } from '@/services/scorecard';

const { Option } = Select;

export default function ModelListPage() {
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [trainVisible, setTrainVisible] = useState(false);
  const [reportVisible, setReportVisible] = useState(false);
  const [report, setReport] = useState<any>(null);
  const [trainForm] = Form.useForm();

  function fetchModels() {
    setLoading(true);
    getScModels({ page, pageSize })
      .then(function (res) {
        setModels(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load models'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchModels(); }, [page, pageSize]);

  function handleTrain(values: any) {
    trainScModel(values)
      .then(function () {
        message.success('Training started');
        setTrainVisible(false);
        trainForm.resetFields();
        fetchModels();
      })
      .catch(function () { message.error('Training failed'); });
  }

  function handleViewReport(id: string) {
    getScModelReport(id)
      .then(function (res) {
        setReport(res.data.data);
        setReportVisible(true);
      })
      .catch(function () { message.error('Failed to load report'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Dataset', dataIndex: 'dataset', key: 'dataset' },
    {
      title: 'Variables',
      dataIndex: 'selectedVariables',
      key: 'selectedVariables',
      render: function (v: any) { return Array.isArray(v) ? v.length : v || 0; },
    },
    { title: 'KS', dataIndex: 'ks', key: 'ks', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'AUC', dataIndex: 'auc', key: 'auc', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'Gini', dataIndex: 'gini', key: 'gini', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { training: 'orange', completed: 'green', failed: 'red' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            {record.status === 'completed' ? (
              <Button size="small" onClick={function () { handleViewReport(record.id); }}>View Report</Button>
            ) : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Scorecard Models</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setTrainVisible(true); }}>Train</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={models} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Train Scorecard Model" open={trainVisible} onCancel={function () { setTrainVisible(false); }} onOk={function () { trainForm.submit(); }} width={600}>
        <Form form={trainForm} layout="vertical" onFinish={handleTrain}>
          <Form.Item name="name" label="Model Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="datasetId" label="Dataset" rules={[{ required: true }]}><Select placeholder="Select dataset"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="targetVariable" label="Target Variable" rules={[{ required: true }]}><Input placeholder="e.g. default_flag" /></Form.Item>
          <Form.Item name="variables" label="Selected Variables" rules={[{ required: true }]}><Select mode="multiple" placeholder="Select variables"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="binningMethod" label="Binning Method" initialValue="chimerge">
            <Select>
              <Option value="equal_width">Equal Width</Option>
              <Option value="equal_freq">Equal Frequency</Option>
              <Option value="chimerge">ChiMerge</Option>
            </Select>
          </Form.Item>
          <Form.Item name="maxBins" label="Max Bins" initialValue={10}><InputNumber min={3} max={50} /></Form.Item>
        </Form>
      </Modal>
      <Modal title="Model Report" open={reportVisible} onCancel={function () { setReportVisible(false); }} footer={null} width={640}>
        {report ? (
          <Descriptions bordered column={2}>
            {Object.entries(report).map(function (entry) {
              const key = entry[0];
              const val = entry[1];
              return <Descriptions.Item key={key} label={key}>{typeof val === 'object' ? JSON.stringify(val) : String(val)}</Descriptions.Item>;
            })}
          </Descriptions>
        ) : null}
      </Modal>
    </div>
  );
}
