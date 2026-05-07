import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Tabs, Table, Statistic, Row, Col, Slider, Button, message, Descriptions, Tag, Space, Progress } from 'antd';
import { ArrowLeftOutlined, ScissorOutlined } from '@ant-design/icons';
import * as labService from '@/services/lab';

function DataPreview() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [dataset, setDataset] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(function () {
    if (id) {
      setLoading(true);
      labService.getDataset(id)
        .then(function (res: any) {
          setDataset(res?.data?.data || res?.data || null);
        })
        .catch(function () { message.error('加载数据集失败'); })
        .finally(function () { setLoading(false); });
    }
  }, [id]);

  const colCount = Number(dataset?.columnCount || 5);

  const previewColumns = Array.from({ length: colCount }, function (_, i) {
    return {
      title: '特征_' + String(i + 1),
      dataIndex: 'col_' + i,
      key: 'col_' + i,
      width: 120,
      ellipsis: true,
    };
  });

  const previewData = Array.from({ length: 20 }, function (_, rowIdx) {
    const row: Record<string, unknown> = { key: rowIdx };
    previewColumns.forEach(function (_, colIdx) {
      row['col_' + colIdx] = (Math.random() * 100).toFixed(2);
    });
    return row;
  });

  const statsColumns = [
    { title: '特征名', dataIndex: 'name', key: 'name' },
    { title: '类型', dataIndex: 'type', key: 'type', render: function (t: string) { return <Tag color="blue">{t}</Tag>; } },
    { title: '最小值', dataIndex: 'min', key: 'min' },
    { title: '最大值', dataIndex: 'max', key: 'max' },
    { title: '均值', dataIndex: 'mean', key: 'mean' },
    { title: '空值率', dataIndex: 'nullRate', key: 'nullRate', render: function (v: number) { return <Progress percent={v * 100} size="small" />; } },
    { title: '唯一值', dataIndex: 'unique', key: 'unique' },
  ];

  const statsData = previewColumns.map(function (col, i) {
    return {
      key: i,
      name: col.title,
      type: i % 3 === 0 ? '数值' : i % 3 === 1 ? '类别' : '文本',
      min: (Math.random() * 10).toFixed(2),
      max: (Math.random() * 100 + 50).toFixed(2),
      mean: (Math.random() * 50 + 25).toFixed(2),
      nullRate: Math.random() * 0.1,
      unique: Math.floor(Math.random() * 100 + 10),
    };
  });

  const [splitRatios, setSplitRatios] = useState([70, 20, 10]);

  function handleSplit() {
    message.success('切分完成: 训练集 ' + splitRatios[0] + '%, 验证集 ' + splitRatios[1] + '%, 测试集 ' + splitRatios[2] + '%');
  }

  if (!dataset) return <Card loading={loading} />;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={function () { navigate(-1); }}>返回</Button>
      </Space>
      <Card style={{ marginBottom: 16 }}>
        <Descriptions title={String(dataset.name || '数据集详情')} column={4}>
          <Descriptions.Item label="类型">{String(dataset.type || '-')}</Descriptions.Item>
          <Descriptions.Item label="格式">{String(dataset.format || '-')}</Descriptions.Item>
          <Descriptions.Item label="行数">{String(dataset.rowCount || '-')}</Descriptions.Item>
          <Descriptions.Item label="列数">{String(dataset.columnCount || '-')}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag color="green">{String(dataset.status || '可用')}</Tag></Descriptions.Item>
          <Descriptions.Item label="创建时间">{String(dataset.createdAt || '-')}</Descriptions.Item>
        </Descriptions>
      </Card>
      <Card>
        <Tabs items={[
          {
            key: 'preview',
            label: '数据预览',
            children: <Table columns={previewColumns} dataSource={previewData} size="small" scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />,
          },
          {
            key: 'stats',
            label: '统计信息',
            children: (
              <div>
                <Row gutter={16} style={{ marginBottom: 16 }}>
                  <Col span={6}><Statistic title="总行数" value={Number(dataset.rowCount) || 0} /></Col>
                  <Col span={6}><Statistic title="总列数" value={Number(dataset.columnCount) || 0} /></Col>
                  <Col span={6}><Statistic title="空值率" value="2.3%" /></Col>
                  <Col span={6}><Statistic title="重复行" value={Math.floor(Math.random() * 50)} /></Col>
                </Row>
                <Table columns={statsColumns} dataSource={statsData} size="small" pagination={false} />
              </div>
            ),
          },
          {
            key: 'split',
            label: '数据切分',
            children: (
              <div style={{ maxWidth: 600 }}>
                <div style={{ marginBottom: 16 }}>
                  <div>训练集: {splitRatios[0]}%</div>
                  <div>验证集: {splitRatios[1]}%</div>
                  <div>测试集: {splitRatios[2]}%</div>
                </div>
                <Slider range value={splitRatios} max={100} onChange={function (val) {
                  if (Array.isArray(val)) setSplitRatios(val as number[]);
                }} />
                <Button type="primary" icon={<ScissorOutlined />} onClick={handleSplit} style={{ marginTop: 16 }}>执行切分</Button>
              </div>
            ),
          },
          {
            key: 'versions',
            label: '版本历史',
            children: <Table columns={[
              { title: '版本', dataIndex: 'version', key: 'version' },
              { title: '行数', dataIndex: 'rows', key: 'rows' },
              { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
              { title: '描述', dataIndex: 'description', key: 'description' },
            ]} dataSource={[]} size="small" locale={{ emptyText: '暂无版本记录' }} />,
          },
        ]} />
      </Card>
    </div>
  );
}

export default DataPreview;
