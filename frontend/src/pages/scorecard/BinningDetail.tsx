import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Button, Modal, Form, Select, Input, message, Card, Row, Col } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getBinningResult, runBinning, adjustBinning } from '@/services/scorecard';

const { Option } = Select;

export default function BinningDetailPage() {
  const { variableId } = useParams<{ variableId: string }>();
  const [bins, setBins] = useState([]);
  const [loading, setLoading] = useState(false);
  const [chartData, setChartData] = useState([]);
  const [methodVisible, setMethodVisible] = useState(false);
  const [adjustVisible, setAdjustVisible] = useState(false);
  const [methodForm] = Form.useForm();
  const [adjustForm] = Form.useForm();

  function fetchBinning() {
    if (!variableId) return;
    setLoading(true);
    getBinningResult(variableId)
      .then(function (res) {
        const items = res.data.data.bins || [];
        setBins(items);
        setChartData(items.map(function (b: any) { return { bin: b.binRange, woe: b.woe || 0 }; }));
      })
      .catch(function () { message.error('Failed to load binning'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchBinning(); }, [variableId]);

  function handleRunBinning(values: any) {
    if (!variableId) return;
    runBinning(variableId, values)
      .then(function () {
        message.success('Auto-binning completed');
        setMethodVisible(false);
        methodForm.resetFields();
        fetchBinning();
      })
      .catch(function () { message.error('Binning failed'); });
  }

  function handleAdjust(values: any) {
    if (!variableId) return;
    const boundaries = values.boundaries.split(',').map(function (s: string) { return s.trim(); });
    adjustBinning(variableId, { boundaries: boundaries })
      .then(function () {
        message.success('Binning adjusted');
        setAdjustVisible(false);
        adjustForm.resetFields();
        fetchBinning();
      })
      .catch(function () { message.error('Adjust failed'); });
  }

  const columns = [
    { title: 'Bin Range', dataIndex: 'binRange', key: 'binRange' },
    { title: 'Count', dataIndex: 'count', key: 'count' },
    { title: 'Event Count', dataIndex: 'eventCount', key: 'eventCount' },
    { title: 'Non-Event Count', dataIndex: 'nonEventCount', key: 'nonEventCount' },
    { title: 'WOE', dataIndex: 'woe', key: 'woe', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'IV Contribution', dataIndex: 'ivContribution', key: 'ivContribution', render: function (v: number) { return v?.toFixed(6) || '-'; } },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Binning Detail - {variableId}</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="primary" onClick={function () { setMethodVisible(true); }}>Auto Binning</Button>
          <Button onClick={function () { setAdjustVisible(true); }}>Manual Adjust</Button>
        </div>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="WOE by Bin">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bin" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="woe" fill="#1890ff" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={24}>
          <Table rowKey="binRange" columns={columns} dataSource={bins} loading={loading} pagination={false} />
        </Col>
      </Row>
      <Modal title="Auto Binning" open={methodVisible} onCancel={function () { setMethodVisible(false); }} onOk={function () { methodForm.submit(); }}>
        <Form form={methodForm} layout="vertical" onFinish={handleRunBinning}>
          <Form.Item name="method" label="Method" rules={[{ required: true }]}>
            <Select placeholder="Select method">
              <Option value="equal_width">Equal Width</Option>
              <Option value="equal_freq">Equal Frequency</Option>
              <Option value="chimerge">ChiMerge</Option>
            </Select>
          </Form.Item>
          <Form.Item name="maxBins" label="Max Bins" initialValue={10}><Input type="number" /></Form.Item>
        </Form>
      </Modal>
      <Modal title="Manual Adjust" open={adjustVisible} onCancel={function () { setAdjustVisible(false); }} onOk={function () { adjustForm.submit(); }}>
        <Form form={adjustForm} layout="vertical" onFinish={handleAdjust}>
          <Form.Item name="boundaries" label="Bin Boundaries (comma separated)" rules={[{ required: true }]}>
            <Input placeholder="0, 100, 200, 500" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
