import React, { useState, useEffect } from 'react';
import { Table, Card, Row, Col, message } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import { getDriftReports, getDriftTrend } from '@/services/inference';

export default function DriftDashboardPage() {
  const [trendData, setTrendData] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [threshold] = useState(0.5);

  function fetchTrend() {
    getDriftTrend({})
      .then(function (res) { setTrendData(res.data.data || []); })
      .catch(function () { message.error('Failed to load drift trend'); });
  }

  function fetchReports() {
    setLoading(true);
    getDriftReports({ page, pageSize })
      .then(function (res) {
        setReports(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load drift reports'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchTrend(); fetchReports(); }, [page, pageSize]);

  const reportColumns = [
    { title: 'Drift Type', dataIndex: 'driftType', key: 'driftType' },
    { title: 'Score', dataIndex: 'score', key: 'score', render: function (v: number) { return v?.toFixed(4); } },
    { title: 'Feature', dataIndex: 'feature', key: 'feature' },
    { title: 'Detected At', dataIndex: 'detectedAt', key: 'detectedAt' },
  ];

  return (
    <div>
      <h2>Data Drift Dashboard</h2>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="Drift Score Trend">
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis domain={[0, 1]} />
                <Tooltip />
                <ReferenceLine y={threshold} stroke="#ff4d4f" strokeDasharray="5 5" label="Threshold" />
                <Line type="monotone" dataKey="score" stroke="#1890ff" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={24}>
          <Card title="Recent Drift Reports">
            <Table
              rowKey="id"
              columns={reportColumns}
              dataSource={reports}
              loading={loading}
              pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
