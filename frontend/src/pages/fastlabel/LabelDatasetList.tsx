import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Upload, message, Tag, Space } from 'antd';
import { PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { getLabelDatasets, createLabelDataset } from '@/services/fastlabel';

const { Option } = Select;

export default function LabelDatasetListPage() {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [importVisible, setImportVisible] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [importForm] = Form.useForm();

  function fetchDatasets() {
    setLoading(true);
    getLabelDatasets({ page, pageSize, type: typeFilter })
      .then(function (res) {
        setDatasets(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load datasets'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchDatasets(); }, [page, pageSize, typeFilter]);

  function handleImport(values: any) {
    createLabelDataset(values)
      .then(function () {
        message.success('Dataset created');
        setImportVisible(false);
        importForm.resetFields();
        fetchDatasets();
      })
      .catch(function () { message.error('Failed to create dataset'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'type', key: 'type', render: function (type: string) { return <Tag>{type}</Tag>; } },
    { title: 'Item Count', dataIndex: 'itemCount', key: 'itemCount' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { ready: 'green', importing: 'orange', empty: 'default' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Label Datasets</h2>
        <Space>
          <Select value={typeFilter} onChange={setTypeFilter} style={{ width: 140 }} placeholder="Filter by type" allowClear>
            <Option value="image">Image</Option>
            <Option value="text">Text</Option>
            <Option value="audio">Audio</Option>
            <Option value="video">Video</Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setImportVisible(true); }}>Import</Button>
        </Space>
      </div>
      <Table rowKey="id" columns={columns} dataSource={datasets} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Import Dataset" open={importVisible} onCancel={function () { setImportVisible(false); }} onOk={function () { importForm.submit(); }}>
        <Form form={importForm} layout="vertical" onFinish={handleImport}>
          <Form.Item name="name" label="Dataset Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="type" label="Type" rules={[{ required: true }]}>
            <Select placeholder="Select type">
              <Option value="image">Image</Option>
              <Option value="text">Text</Option>
              <Option value="audio">Audio</Option>
              <Option value="video">Video</Option>
            </Select>
          </Form.Item>
          <Form.Item name="source" label="Source" rules={[{ required: true }]}>
            <Select placeholder="Select source">
              <Option value="upload">Upload Files</Option>
              <Option value="lab">From Lab Dataset</Option>
            </Select>
          </Form.Item>
          <Form.Item name="files" label="Files" valuePropName="fileList" getValueFromEvent={function (e) { return Array.isArray(e) ? e : e?.fileList; }}>
            <Upload.Dragger beforeUpload={function () { return false; }}>
              <p><UploadOutlined style={{ fontSize: 24 }} /></p>
              <p>Click or drag files to upload</p>
            </Upload.Dragger>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
