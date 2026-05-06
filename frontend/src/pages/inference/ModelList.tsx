import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space, Upload } from 'antd';
import { PlusOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { getModels, importModel, approveModel } from '@/services/inference';

const { Option } = Select;

export default function ModelListPage() {
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [importVisible, setImportVisible] = useState(false);
  const [importForm] = Form.useForm();

  function fetchModels() {
    setLoading(true);
    getModels({ page, pageSize })
      .then(function (res) {
        setModels(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load models'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchModels(); }, [page, pageSize]);

  function handleImport(values: any) {
    importModel(values)
      .then(function () {
        message.success('Model imported');
        setImportVisible(false);
        importForm.resetFields();
        fetchModels();
      })
      .catch(function () { message.error('Import failed'); });
  }

  function handleApprove(id: string, action: 'approved' | 'rejected') {
    approveModel(id, { status: action })
      .then(function () {
        message.success('Status updated');
        fetchModels();
      })
      .catch(function () { message.error('Action failed'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Version', dataIndex: 'version', key: 'version' },
    { title: 'Format', dataIndex: 'format', key: 'format' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { pending: 'orange', approved: 'green', rejected: 'red' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    { title: 'Source', dataIndex: 'source', key: 'source' },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            {record.status === 'pending' ? (
              <>
                <Button size="small" type="primary" icon={<CheckOutlined />} onClick={function () { handleApprove(record.id, 'approved'); }}>Approve</Button>
                <Button size="small" danger icon={<CloseOutlined />} onClick={function () { handleApprove(record.id, 'rejected'); }}>Reject</Button>
              </>
            ) : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Models</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setImportVisible(true); }}>Import Model</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={models}
        loading={loading}
        pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }}
      />
      <Modal
        title="Import Model"
        open={importVisible}
        onCancel={function () { setImportVisible(false); }}
        onOk={function () { importForm.submit(); }}
      >
        <Form form={importForm} layout="vertical" onFinish={handleImport}>
          <Form.Item name="name" label="Model Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="source" label="Source" rules={[{ required: true }]}>
            <Select placeholder="Select source">
              <Option value="lab">From Lab</Option>
              <Option value="upload">Upload File</Option>
            </Select>
          </Form.Item>
          <Form.Item name="format" label="Format" rules={[{ required: true }]}>
            <Select placeholder="Select format">
              <Option value="onnx">ONNX</Option>
              <Option value="pytorch">PyTorch</Option>
              <Option value="tensorflow">TensorFlow</Option>
            </Select>
          </Form.Item>
          <Form.Item name="version" label="Version">
            <Input placeholder="e.g. 1.0.0" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
