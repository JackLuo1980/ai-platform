import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, Input, message, Card, Row, Col } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useNavigate } from 'react-router-dom';
import { getVariables, analyzeVariables } from '@/services/scorecard';

const { Option } = Select;

export default function VariableListPage() {
  const [variables, setVariables] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [chartData, setChartData] = useState([]);
  const [analyzeVisible, setAnalyzeVisible] = useState(false);
  const [analyzeForm] = Form.useForm();
  const navigate = useNavigate();

  function fetchVariables() {
    setLoading(true);
    getVariables({ page, pageSize })
      .then(function (res) {
        const items = res.data.data.items || [];
        setVariables(items);
        setTotal(res.data.data.total || 0);
        const top20 = items.slice(0, 20).map(function (v: any) { return { name: v.name, ivValue: v.ivValue || 0 }; });
        setChartData(top20);
      })
      .catch(function () { message.error('Failed to load variables'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchVariables(); }, [page, pageSize]);

  function handleAnalyze(values: any) {
    analyzeVariables(values)
      .then(function () {
        message.success('Analysis started');
        setAnalyzeVisible(false);
        analyzeForm.resetFields();
        fetchVariables();
      })
      .catch(function () { message.error('Analysis failed'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'dtype', key: 'dtype' },
    { title: 'IV Value', dataIndex: 'ivValue', key: 'ivValue', sorter: true, render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'Missing Rate', dataIndex: 'missingRate', key: 'missingRate', render: function (v: number) { return v !== undefined ? (v * 100).toFixed(2) + '%' : '-'; } },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return <Button size="small" type="link" onClick={function () { navigate('/scorecard/binning/' + record.id); }}>Binning</Button>;
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Variables</h2>
        <Button type="primary" onClick={function () { setAnalyzeVisible(true); }}>Analyze</Button>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="Top 20 Variables by IV Value">
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
          <Table rowKey="id" columns={columns} dataSource={variables} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
        </Col>
      </Row>
      <Modal title="Analyze Variables" open={analyzeVisible} onCancel={function () { setAnalyzeVisible(false); }} onOk={function () { analyzeForm.submit(); }}>
        <Form form={analyzeForm} layout="vertical" onFinish={handleAnalyze}>
          <Form.Item name="datasetId" label="Dataset" rules={[{ required: true }]}><Select placeholder="Select dataset"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="targetVariable" label="Target Variable" rules={[{ required: true }]}><Input placeholder="e.g. default_flag" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
