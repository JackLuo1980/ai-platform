import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, message, Tag, Space, Descriptions } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { getEvaluations, runEvaluation, getEvaluationReport } from '@/services/inference';

const { Option } = Select;

export default function EvaluationListPage() {
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [reportVisible, setReportVisible] = useState(false);
  const [report, setReport] = useState<any>(null);
  const [createForm] = Form.useForm();

  function fetchEvaluations() {
    setLoading(true);
    getEvaluations({ page, pageSize })
      .then(function (res) {
        setEvaluations(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load evaluations'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchEvaluations(); }, [page, pageSize]);

  function handleCreate(values: any) {
    runEvaluation(values)
      .then(function () {
        message.success('Evaluation started');
        setCreateVisible(false);
        createForm.resetFields();
        fetchEvaluations();
      })
      .catch(function () { message.error('Failed to start evaluation'); });
  }

  function handleViewReport(id: string) {
    getEvaluationReport(id)
      .then(function (res) {
        setReport(res.data.data);
        setReportVisible(true);
      })
      .catch(function () { message.error('Failed to load report'); });
  }

  const columns = [
    { title: 'Model', dataIndex: 'model', key: 'model' },
    { title: 'Type', dataIndex: 'type', key: 'type' },
    { title: 'Template', dataIndex: 'template', key: 'template' },
    {
      title: 'Metrics',
      dataIndex: 'metricsSummary',
      key: 'metricsSummary',
      render: function (summary: any) {
        if (!summary) return '-';
        return JSON.stringify(summary);
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { running: 'orange', completed: 'green', failed: 'red', pending: 'default' };
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
        <h2>Evaluations</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>Evaluate</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={evaluations} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Run Evaluation" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="modelId" label="Model" rules={[{ required: true }]}><Select placeholder="Select model"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="templateId" label="Evaluation Template" rules={[{ required: true }]}><Select placeholder="Select template"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="dataSource" label="Data Source" rules={[{ required: true }]}><Select placeholder="Select data source"><Option value="">Select</Option></Select></Form.Item>
        </Form>
      </Modal>
      <Modal title="Evaluation Report" open={reportVisible} onCancel={function () { setReportVisible(false); }} footer={null} width={640}>
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
