import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Card, Row, Col } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useNavigate } from 'react-router-dom';
import { listVariables, analyzeVariables } from '@/services/scorecard';

function VariableList() {
  const [variables, setVariables] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [chartData, setChartData] = useState<Record<string, unknown>[]>([]);
  const [analyzeVisible, setAnalyzeVisible] = useState(false);
  const [analyzeForm] = Form.useForm();
  const navigate = useNavigate();

  function fetchVariables() {
    setLoading(true);
    listVariables({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        const items: Record<string, unknown>[] = d.items || d.content || [];
        setVariables(items);
        setTotal(d.total || d.totalElements || 0);
        setChartData(items.slice(0, 20).map(function (v: any) { return { name: v.name, ivValue: v.ivValue || 0 }; }));
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchVariables(); }, [page, pageSize]);

  function handleAnalyze(values: Record<string, unknown>) {
    analyzeVariables(values)
      .then(function () {
        message.success('分析已启动');
        setAnalyzeVisible(false);
        analyzeForm.resetFields();
        fetchVariables();
      })
      .catch(function () { message.error('分析失败'); });
  }

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'dtype' },
    { title: 'IV 值', dataIndex: 'ivValue', sorter: true, render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: '缺失率', dataIndex: 'missingRate', render: function (v: number) { return v !== undefined ? (v * 100).toFixed(2) + '%' : '-'; } },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return <Button size="small" type="link" onClick={function () { navigate('/scorecard/binning/' + record.id); }}>分箱</Button>;
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>变量管理</h2>
        <Button type="primary" onClick={function () { setAnalyzeVisible(true); }}>分析</Button>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="IV 值 Top 20">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" angle={-45} textAnchor="end" height={80} interval={0} tick={{ fontSize: 11 }} />
                <YAxis />
                <Tooltip />
                <Bar dataKey="ivValue" fill="#1890ff" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={24}>
          <Table rowKey="id" columns={columns} dataSource={variables} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
        </Col>
      </Row>
      <Modal title="分析变量" open={analyzeVisible} onCancel={function () { setAnalyzeVisible(false); }} onOk={function () { analyzeForm.submit(); }}>
        <Form form={analyzeForm} layout="vertical" onFinish={handleAnalyze}>
          <Form.Item name="datasetId" label="数据集" rules={[{ required: true }]}><Select placeholder="选择数据集" /></Form.Item>
          <Form.Item name="targetVariable" label="目标变量" rules={[{ required: true }]}><Input placeholder="如 default_flag" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default VariableList;
