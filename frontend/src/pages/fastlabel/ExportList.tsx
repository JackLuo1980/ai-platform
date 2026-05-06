import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, message, Space } from 'antd';
import { ExportOutlined, CloudUploadOutlined } from '@ant-design/icons';
import { getExports, createExport, pushToLab } from '@/services/fastlabel';

const { Option } = Select;

export default function ExportListPage() {
  const [exports, setExports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [exportVisible, setExportVisible] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState<string>('');
  const [exportForm] = Form.useForm();

  function fetchExports() {
    setLoading(true);
    getExports({ page, pageSize })
      .then(function (res) {
        setExports(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load exports'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchExports(); }, [page, pageSize]);

  function handleExport(values: any) {
    createExport(values)
      .then(function () {
        message.success('Export started');
        setExportVisible(false);
        exportForm.resetFields();
        fetchExports();
      })
      .catch(function () { message.error('Export failed'); });
  }

  function handlePushToLab(id: string) {
    pushToLab(id)
      .then(function () { message.success('Pushed to Lab'); fetchExports(); })
      .catch(function () { message.error('Push failed'); });
  }

  function handleDownload(record: any) {
    if (!record.downloadUrl) { message.warning('File not ready'); return; }
    window.open(record.downloadUrl, '_blank');
  }

  const columns = [
    { title: 'Task', dataIndex: 'task', key: 'task' },
    { title: 'Format', dataIndex: 'format', key: 'format' },
    { title: 'Item Count', dataIndex: 'itemCount', key: 'itemCount' },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            <Button size="small" onClick={function () { handleDownload(record); }}>Download</Button>
            <Button size="small" icon={<CloudUploadOutlined />} onClick={function () { handlePushToLab(record.id); }}>Push to Lab</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Exports</h2>
        <Button type="primary" icon={<ExportOutlined />} onClick={function () { setExportVisible(true); }}>Export</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={exports} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Export Labels" open={exportVisible} onCancel={function () { setExportVisible(false); }} onOk={function () { exportForm.submit(); }}>
        <Form form={exportForm} layout="vertical" onFinish={handleExport}>
          <Form.Item name="taskId" label="Task" rules={[{ required: true }]}>
            <Select placeholder="Select task"><Option value="">Select</Option></Select>
          </Form.Item>
          <Form.Item name="format" label="Format" rules={[{ required: true }]}>
            <Select placeholder="Select format">
              <Option value="coco">COCO</Option>
              <Option value="json">JSON</Option>
              <Option value="csv">CSV</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
