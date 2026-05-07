import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, message } from 'antd';
import { listDriftReports } from '@/services/inference';

function DriftDashboard() {
  const [reports, setReports] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);

  function fetchReports() {
    setLoading(true);
    listDriftReports({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setReports(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchReports(); }, [page, pageSize]);

  const reportColumns = [
    { title: '漂移类型', dataIndex: 'driftType' },
    { title: '分数', dataIndex: 'score', render: function (v: number) { return v?.toFixed(4); } },
    { title: '特征', dataIndex: 'feature' },
    { title: '检测时间', dataIndex: 'detectedAt' },
  ];

  return (
    <div>
      <h2>数据漂移监控</h2>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="漂移报告">
            <Table
              rowKey="id"
              columns={reportColumns}
              dataSource={reports}
              loading={loading}
              pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default DriftDashboard;
