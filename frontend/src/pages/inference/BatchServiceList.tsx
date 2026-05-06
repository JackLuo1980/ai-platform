import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { getBatchServices, createBatch, startBatch, stopBatch, downloadResults } from '@/services/inference';

const { Option } = Select;

export default function BatchServiceListPage() {
  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [createForm] = Form.useForm();

  function fetchBatches() {
    setLoading(true);
    getBatchServices({ page, pageSize })
      .then(function (res) {
        setBatches(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load batch services'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchBatches(); }, [page, pageSize]);

  function handleCreate(values: any) {
    createBatch(values)
      .then(function () {
        message.success('Batch job created');
        setCreateVisible(false);
        createForm.resetFields();
        fetchBatches();
      })
      .catch(function () { message.error('Create failed'); });
  }

  function handleStart(id: string) {
    startBatch(id)
      .then(function () { message.success('Batch started'); fetchBatches(); })
      .catch(function () { message.error('Start failed'); });
  }

  function handleStop(id: string) {
    stopBatch(id)
      .then(function () { message.success('Batch stopped'); fetchBatches(); })
      .catch(function () { message.error('Stop failed'); });
  }

  function handleDownload(id: string) {
    downloadResults(id)
      .then(function (res) {
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'batch_results.csv');
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch(function () { message.error('Download failed'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Model', dataIndex: 'model', key: 'model' },
    { title: 'Schedule', dataIndex: 'schedule', key: 'schedule' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { running: 'green', stopped: 'red', completed: 'blue', pending: 'orange' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            {record.status === 'pending' ? <Button size="small" type="primary" onClick={function () { handleStart(record.id); }}>Start</Button> : null}
            {record.status === 'running' ? <Button size="small" danger onClick={function () { handleStop(record.id); }}>Stop</Button> : null}
            {record.status === 'completed' ? <Button size="small" onClick={function () { handleDownload(record.id); }}>Download</Button> : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Batch Services</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>Create Batch</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={batches} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Create Batch Job" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="Job Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="modelId" label="Model" rules={[{ required: true }]}><Select placeholder="Select model"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="dataSource" label="Data Source Path" rules={[{ required: true }]}><Input placeholder="/data/input.csv" /></Form.Item>
          <Form.Item name="outputPath" label="Output Path" rules={[{ required: true }]}><Input placeholder="/data/output/" /></Form.Item>
          <Form.Item name="schedule" label="Schedule (cron)"><Input placeholder="0 0 * * *" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
