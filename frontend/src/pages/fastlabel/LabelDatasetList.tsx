import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listLabelDatasets, createLabelDataset } from '@/services/fastlabel';

function LabelDatasetList() {
  const [datasets, setDatasets] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [importVisible, setImportVisible] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [importForm] = Form.useForm();

  function fetchDatasets() {
    setLoading(true);
    listLabelDatasets({ page, size: pageSize, type: typeFilter || undefined })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setDatasets(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchDatasets(); }, [page, pageSize, typeFilter]);

  function handleImport(values: Record<string, unknown>) {
    createLabelDataset(values)
      .then(function () {
        message.success('数据集已创建');
        setImportVisible(false);
        importForm.resetFields();
        fetchDatasets();
      })
      .catch(function () { message.error('创建失败'); });
  }

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type', render: function (type: string) { return <Tag>{type}</Tag>; } },
    { title: '数据量', dataIndex: 'itemCount' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <Tag color={status === 'ready' ? 'green' : 'orange'}>{status}</Tag>; } },
    { title: '创建时间', dataIndex: 'createdAt' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>标注数据集</h2>
        <Space>
          <Select value={typeFilter || undefined} onChange={function (v) { setTypeFilter(v || ''); }} style={{ width: 140 }} placeholder="类型筛选" allowClear>
            <Select.Option value="image">图片</Select.Option>
            <Select.Option value="text">文本</Select.Option>
            <Select.Option value="audio">音频</Select.Option>
            <Select.Option value="video">视频</Select.Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setImportVisible(true); }}>导入</Button>
        </Space>
      </div>
      <Table rowKey="id" columns={columns} dataSource={datasets} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="导入数据集" open={importVisible} onCancel={function () { setImportVisible(false); }} onOk={function () { importForm.submit(); }}>
        <Form form={importForm} layout="vertical" onFinish={handleImport}>
          <Form.Item name="name" label="数据集名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true }]}>
            <Select placeholder="选择类型">
              <Select.Option value="image">图片</Select.Option>
              <Select.Option value="text">文本</Select.Option>
              <Select.Option value="audio">音频</Select.Option>
              <Select.Option value="video">视频</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="source" label="来源" rules={[{ required: true }]}>
            <Select placeholder="选择来源">
              <Select.Option value="upload">上传文件</Select.Option>
              <Select.Option value="lab">实验室数据集</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default LabelDatasetList;
