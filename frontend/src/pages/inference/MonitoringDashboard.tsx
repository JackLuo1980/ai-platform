import { useState, useEffect } from 'react';
import { Card, Row, Col, Select, message } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getMonitoringMetrics } from '@/services/inference';

function MonitoringDashboard() {
  const [qpsData, setQpsData] = useState<Record<string, unknown>[]>([]);
  const [latencyData, setLatencyData] = useState<Record<string, unknown>[]>([]);
  const [errorData, setErrorData] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [timeRange, setTimeRange] = useState('1h');

  function fetchMetrics() {
    setLoading(true);
    getMonitoringMetrics({ timeRange })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setQpsData(d.qps || []);
        setLatencyData(d.latency || []);
        setErrorData(d.errors || []);
      })
      .catch(function () { message.error('加载监控数据失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchMetrics(); }, [timeRange]);

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>监控仪表盘</h2>
        <div style={{ display: 'flex', gap: 12 }}>
          <Select value={timeRange} onChange={setTimeRange} style={{ width: 120 }}>
            <Select.Option value="1h">1 小时</Select.Option>
            <Select.Option value="6h">6 小时</Select.Option>
            <Select.Option value="24h">24 小时</Select.Option>
            <Select.Option value="7d">7 天</Select.Option>
          </Select>
        </div>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="QPS (每秒查询数)" loading={loading}>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={qpsData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#1890ff" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={24}>
          <Card title="平均延迟 (ms)" loading={loading}>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={latencyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#52c41a" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={24}>
          <Card title="错误率 (%)" loading={loading}>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={errorData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#ff4d4f" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default MonitoringDashboard;
