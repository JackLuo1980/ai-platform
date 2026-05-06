import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Select, message } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getServiceMetrics } from '@/services/inference';

const { Option } = Select;

export default function MonitoringDashboardPage() {
  const [qpsData, setQpsData] = useState([]);
  const [latencyData, setLatencyData] = useState([]);
  const [errorData, setErrorData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [serviceId, setServiceId] = useState<string>('');
  const [timeRange, setTimeRange] = useState('1h');

  function fetchMetrics() {
    setLoading(true);
    getServiceMetrics({ serviceId: serviceId, timeRange: timeRange })
      .then(function (res) {
        const data = res.data.data;
        setQpsData(data.qps || []);
        setLatencyData(data.latency || []);
        setErrorData(data.errors || []);
      })
      .catch(function () { message.error('Failed to load metrics'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchMetrics(); }, [serviceId, timeRange]);

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Monitoring Dashboard</h2>
        <div style={{ display: 'flex', gap: 12 }}>
          <Select value={serviceId} onChange={setServiceId} style={{ width: 200 }} placeholder="Select service" allowClear>
            <Option value="">All Services</Option>
          </Select>
          <Select value={timeRange} onChange={setTimeRange} style={{ width: 120 }}>
            <Option value="1h">1 Hour</Option>
            <Option value="6h">6 Hours</Option>
            <Option value="24h">24 Hours</Option>
            <Option value="7d">7 Days</Option>
          </Select>
        </div>
      </div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="QPS (Queries Per Second)" loading={loading}>
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
          <Card title="Average Latency (ms)" loading={loading}>
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
          <Card title="Error Rate (%)" loading={loading}>
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
