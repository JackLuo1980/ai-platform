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
    { title: '列名', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type' },
    { title: '计数', dataIndex: 'count' },
    { title: '缺失值', dataIndex: 'missing' },
    { title: '均值', dataIndex: 'mean' },
    { title: '标准差', dataIndex: 'std' },
    { title: '最小值', dataIndex: 'min' },
    { title: '最大值', dataIndex: 'max' },
  ];

  const versionColumns = [
    { title: '版本', dataIndex: 'version' },
    { title: '行数', dataIndex: 'rowCount' },
    { title: '大小', dataIndex: 'size' },
    { title: '创建时间', dataIndex: 'createdAt' },
    { title: '创建者', dataIndex: 'createdBy' },
  ];

  return (
    <div>
      <Tabs activeKey={activeTab} onChange={handleTabChange} items={[
        {
          key: 'preview',
          label: '预览',
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
          label: '统计',
          children: (
            <div>
              <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col span={6}><Card><Statistic title="总行数" value={previewData?.rows.length || 0} /></Card></Col>
                <Col span={6}><Card><Statistic title="总列数" value={previewData?.columns.length || 0} /></Card></Col>
              </Row>
              <Table columns={statColumns} dataSource={statsData} rowKey="name" size="small" />
            </div>
          ),
        },
        {
          key: 'versions',
          label: '版本',
          children: <Table columns={versionColumns} dataSource={versions} rowKey="version" size="small" />,
        },
      ]} />
    </div>
  );
}

export default DatasetDetail;
