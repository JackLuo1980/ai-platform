import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Tabs, Table, Card, Space, Statistic, Row, Col, Tag, Button } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { getDatasetPreview, getDatasetStats, getDatasetVersions } from '@/services/lab';

const COLORS = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'];

function DatasetDetail() {
  const { id } = useParams<{ id: string }>();
  const [previewData, setPreviewData] = useState<{ columns: string[]; rows: unknown[][] } | null>(null);
  const [statsData, setStatsData] = useState<Record<string, unknown>[]>([]);
  const [versions, setVersions] = useState<Record<string, unknown>[]>([]);
  const [activeTab, setActiveTab] = useState('preview');

  async function loadPreview() {
    const res: any = await getDatasetPreview(id!);
    setPreviewData(res.data || res);
  }

  async function loadStats() {
    const res: any = await getDatasetStats(id!);
    setStatsData(res.data?.columns || res.columns || []);
  }

  async function loadVersions() {
    const res: any = await getDatasetVersions(id!);
    setVersions(res.data?.items || res.items || []);
  }

  useState(function () {
    loadPreview();
  });

  function handleTabChange(key: string) {
    setActiveTab(key);
    if (key === 'stats' && statsData.length === 0) loadStats();
    if (key === 'versions' && versions.length === 0) loadVersions();
  }

  const previewColumns = previewData
    ? previewData.columns.map(function (col) { return { title: col, dataIndex: col, key: col, ellipsis: true }; })
    : [];

  const previewRows = previewData
    ? previewData.rows.map(function (row, i) {
        const obj: Record<string, unknown> = { key: i };
        previewData.columns.forEach(function (col, j) { obj[col] = row[j]; });
        return obj;
      })
    : [];

  const statColumns = [
    { title: 'Column', dataIndex: 'name' },
    { title: 'Type', dataIndex: 'type' },
    { title: 'Count', dataIndex: 'count' },
    { title: 'Missing', dataIndex: 'missing' },
    { title: 'Mean', dataIndex: 'mean' },
    { title: 'Std', dataIndex: 'std' },
    { title: 'Min', dataIndex: 'min' },
    { title: 'Max', dataIndex: 'max' },
  ];

  const versionColumns = [
    { title: 'Version', dataIndex: 'version' },
    { title: 'Rows', dataIndex: 'rowCount' },
    { title: 'Size', dataIndex: 'size' },
    { title: 'Created', dataIndex: 'createdAt' },
    { title: 'CreatedBy', dataIndex: 'createdBy' },
  ];

  return (
    <div>
      <Tabs activeKey={activeTab} onChange={handleTabChange} items={[
        {
          key: 'preview',
          label: 'Preview',
          children: (
            <Table
              columns={previewColumns}
              dataSource={previewRows}
              scroll={{ x: 'max-content' }}
              pagination={{ pageSize: 50 }}
              size="small"
            />
          ),
        },
        {
          key: 'stats',
          label: 'Statistics',
          children: (
            <div>
              <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col span={6}><Card><Statistic title="Total Rows" value={previewData?.rows.length || 0} /></Card></Col>
                <Col span={6}><Card><Statistic title="Total Columns" value={previewData?.columns.length || 0} /></Card></Col>
              </Row>
              <Table columns={statColumns} dataSource={statsData} rowKey="name" size="small" />
            </div>
          ),
        },
        {
          key: 'versions',
          label: 'Versions',
          children: <Table columns={versionColumns} dataSource={versions} rowKey="version" size="small" />,
        },
      ]} />
    </div>
  );
}

export default DatasetDetail;
