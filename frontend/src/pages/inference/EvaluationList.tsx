import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, message, Tag, Space, Descriptions } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listEvaluations, createEvaluation, getEvaluationReport } from '@/services/inference';

function EvaluationList() {
  const [evaluations, setEvaluations] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [reportVisible, setReportVisible] = useState(false);
  const [report, setReport] = useState<Record<string, unknown> | null>(null);
  const [createForm] = Form.useForm();

  function fetchEvaluations() {
    setLoading(true);
    listEvaluations({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setEvaluations(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchEvaluations(); }, [page, pageSize]);

  function handleCreate(values: Record<string, unknown>) {
    createEvaluation(values)
      .then(function () {
        message.success('评估已启动');
        setCreateVisible(false);
        createForm.resetFields();
        fetchEvaluations();
      })
      .catch(function () { message.error('启动失败'); });
  }

  function handleViewReport(id: string) {
    getEvaluationReport(id)
      .then(function (res: any) {
        setReport(res?.data || res || null);
        setReportVisible(true);
      })
      .catch(function () { message.error('加载报告失败'); });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    running: { color: 'orange', label: '运行中' },
    completed: { color: 'green', label: '已完成' },
    failed: { color: 'red', label: '失败' },
    pending: { color: 'default', label: '等待中' },
  };

  const columns = [
    { title: '模型', dataIndex: 'modelName' },
    { title: '类型', dataIndex: 'type' },
    { title: '模板', dataIndex: 'templateName' },
    { title: '指标摘要', dataIndex: 'metricsSummary', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      render: function (status: string) {
        const cfg = STATUS_MAP[status] || { color: 'default', label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            {record.status === 'completed' ? (
              <Button size="small" onClick={function () { handleViewReport(record.id as string); }}>查看报告</Button>
            ) : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>模型评估</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>新建评估</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={evaluations} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="新建评估" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="modelId" label="模型" rules={[{ required: true }]}><Select placeholder="选择模型" /></Form.Item>
          <Form.Item name="templateId" label="评估模板" rules={[{ required: true }]}><Select placeholder="选择模板" /></Form.Item>
          <Form.Item name="dataSource" label="数据源" rules={[{ required: true }]}><Select placeholder="选择数据源" /></Form.Item>
        </Form>
      </Modal>
      <Modal title="评估报告" open={reportVisible} onCancel={function () { setReportVisible(false); }} footer={null} width={640}>
        {report ? (
          <Descriptions bordered column={2}>
            {Object.entries(report).map(function (entry) {
              return <Descriptions.Item key={entry[0]} label={entry[0]}>{typeof entry[1] === 'object' ? JSON.stringify(entry[1]) : String(entry[1])}</Descriptions.Item>;
            })}
          </Descriptions>
        ) : null}
      </Modal>
    </div>
  );
}

export default EvaluationList;
