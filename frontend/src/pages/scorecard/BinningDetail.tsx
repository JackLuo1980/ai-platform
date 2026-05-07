import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Button, Modal, Form, Input, Select, message, Card, Row, Col } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getBinningDetail, runBinning, adjustBinning } from '@/services/scorecard';

function BinningDetail() {
  const { variableId } = useParams<{ variableId: string }>();
  const [bins, setBins] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [chartData, setChartData] = useState<Record<string, unknown>[]>([]);
  const [methodVisible, setMethodVisible] = useState(false);
  const [adjustVisible, setAdjustVisible] = useState(false);
  const [methodForm] = Form.useForm();
  const [adjustForm] = Form.useForm();

  function fetchBinning() {
    if (!variableId) return;
    setLoading(true);
    getBinningDetail(variableId)
      .then(function (res: any) {
        const d = res?.data || res || {};
        const items: Record<string, unknown>[] = d.bins || d.items || [];
        setBins(items);
        setChartData(items.map(function (b: any) { return { bin: b.binRange, woe: b.woe || 0 }; }));
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchBinning(); }, [variableId]);

  function handleRunBinning(values: Record<string, unknown>) {
    if (!variableId) return;
    runBinning(variableId, values)
      .then(function () {
        message.success('自动分箱完成');
        setMethodVisible(false);
        methodForm.resetFields();
        fetchBinning();
      })
      .catch(function () { message.error('分箱失败'); });
  }

  function handleAdjust(values: Record<string, unknown>) {
    if (!variableId) return;
    adjustBinning(variableId, values)
      .then(function () {
        message.success('分箱已调整');
        setAdjustVisible(false);
        adjustForm.resetFields();
        fetchBinning();
      })
      .catch(function () { message.error('调整失败'); });
  }

  const columns = [
    { title: '分箱范围', dataIndex: 'binRange' },
    { title: '数量', dataIndex: 'count' },
    { title: '事件数', dataIndex: 'eventCount' },
    { title: '非事件数', dataIndex: 'nonEventCount' },
    { title: 'WOE', dataIndex: 'woe', render: function (v: number) { return v?.toFixed(4) || '-'; } },
    { title: 'IV 贡献', dataIndex: 'ivContribution', render: function (v: number) { return v?.toFixed(6) || '-'; } },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>分箱详情 - {variableId}</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="primary" onClick={function () { setMethodVisible(true); }}>自动分箱</Button>
          <Button onClick={function () { setAdjustVisible(true); }}>手动调整</Button>
        </div>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="WOE 分布">
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
      <Modal title="自动分箱" open={methodVisible} onCancel={function () { setMethodVisible(false); }} onOk={function () { methodForm.submit(); }}>
        <Form form={methodForm} layout="vertical" onFinish={handleRunBinning}>
          <Form.Item name="method" label="方法" rules={[{ required: true }]}>
            <Select placeholder="选择方法">
              <Select.Option value="equal_width">等距</Select.Option>
              <Select.Option value="equal_freq">等频</Select.Option>
              <Select.Option value="chimerge">ChiMerge</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="maxBins" label="最大分箱数" initialValue={10}><Input type="number" /></Form.Item>
        </Form>
      </Modal>
      <Modal title="手动调整" open={adjustVisible} onCancel={function () { setAdjustVisible(false); }} onOk={function () { adjustForm.submit(); }}>
        <Form form={adjustForm} layout="vertical" onFinish={handleAdjust}>
          <Form.Item name="boundaries" label="分箱边界 (逗号分隔)" rules={[{ required: true }]}>
            <Input placeholder="0, 100, 200, 500" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default BinningDetail;
