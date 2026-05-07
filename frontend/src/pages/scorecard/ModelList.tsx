import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Tag, Space, Descriptions } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listScorecardModels, trainScorecardModel, getScModelReport, deleteScorecardModel } from '@/services/scorecard';

function ScorecardModelList() {
  const [models, setModels] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [trainVisible, setTrainVisible] = useState(false);
  const [reportVisible, setReportVisible] = useState(false);
  const [report, setReport] = useState<Record<string, unknown> | null>(null);
  const [trainForm] = Form.useForm();

  function fetchModels() {
    setLoading(true);
    listScorecardModels({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setModels(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchModels(); }, [page, pageSize]);

  function handleTrain(values: Record<string, unknown>) {
    trainScorecardModel(values.modelId as string)
      .then(function () {
        message.success('训练已启动');
        setTrainVisible(false);
        trainForm.resetFields();
        fetchModels();
      })
      .catch(function () { message.error('训练失败'); });
  }

  function handleViewReport(id: string) {
    getScModelReport(id)
      .then(function (res: any) {
        setReport(res?.data || res || null);
        setReportVisible(true);
      })
      .catch(function () { message.error('加载报告失败'); });
  }

  function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该模型吗？',
      onOk: function () {
        deleteScorecardModel(id)
          .then(function () { message.success('已删除'); fetchModels(); })
          .catch(function () { message.error('删除失败'); });
      },
    });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    training: { color: 'orange', label: '训练中' },
    completed: { color: 'green', label: '已完成' },
    failed: { color: 'red', label: '失败' },
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '数据集', dataIndex: 'datasetName' },
    { title: '变量数', dataIndex: 'variableCount' },
    { title: 'KS', dataIndex: 'ks', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'AUC', dataIndex: 'auc', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'Gini', dataIndex: 'gini', render: function (v: number) { return v?.toFixed(4) || '-'; } },
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
            <Button size="small" danger onClick={function () { handleDelete(record.id as string); }}>删除</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>评分卡模型</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setTrainVisible(true); }}>训练</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={models} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="训练评分卡模型" open={trainVisible} onCancel={function () { setTrainVisible(false); }} onOk={function () { trainForm.submit(); }} width={600}>
        <Form form={trainForm} layout="vertical" onFinish={handleTrain}>
          <Form.Item name="modelId" label="模型" rules={[{ required: true }]}><Select placeholder="选择模型" /></Form.Item>
        </Form>
      </Modal>
      <Modal title="模型报告" open={reportVisible} onCancel={function () { setReportVisible(false); }} footer={null} width={640}>
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

export default ScorecardModelList;
