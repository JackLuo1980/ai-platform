import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { listBatchServices, createBatchService, startBatchService, stopBatchService, getBatchServiceResult } from '@/services/inference';

function BatchServiceList() {
  const [batches, setBatches] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [createForm] = Form.useForm();

  function fetchBatches() {
    setLoading(true);
    listBatchServices({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setBatches(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchBatches(); }, [page, pageSize]);

  function handleCreate(values: Record<string, unknown>) {
    createBatchService(values)
      .then(function () {
        message.success('批量任务已创建');
        setCreateVisible(false);
        createForm.resetFields();
        fetchBatches();
      })
      .catch(function () { message.error('创建失败'); });
  }

  function handleStart(id: string) {
    startBatchService(id)
      .then(function () { message.success('已启动'); fetchBatches(); })
      .catch(function () { message.error('启动失败'); });
  }

  function handleStop(id: string) {
    stopBatchService(id)
      .then(function () { message.success('已停止'); fetchBatches(); })
      .catch(function () { message.error('停止失败'); });
  }

  function handleDownload(id: string) {
    getBatchServiceResult(id)
      .then(function (res: any) {
        const blob = res instanceof Blob ? res : new Blob([res]);
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'batch_results.csv');
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch(function () { message.error('下载失败'); });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    running: { color: 'green', label: '运行中' },
    stopped: { color: 'red', label: '已停止' },
    completed: { color: 'blue', label: '已完成' },
    pending: { color: 'orange', label: '等待中' },
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '模型', dataIndex: 'modelName' },
    { title: '调度', dataIndex: 'schedule' },
    {
      title: '状态',
      dataIndex: 'status',
      render: function (status: string) {
        const cfg = STATUS_MAP[status] || { color: 'default', label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            {record.status === 'pending' ? <Button size="small" type="primary" onClick={function () { handleStart(record.id as string); }}>启动</Button> : null}
            {record.status === 'running' ? <Button size="small" danger onClick={function () { handleStop(record.id as string); }}>停止</Button> : null}
            {record.status === 'completed' ? <Button size="small" onClick={function () { handleDownload(record.id as string); }}>下载</Button> : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>批量服务</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>创建批量任务</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={batches} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="创建批量任务" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="任务名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="modelId" label="模型" rules={[{ required: true }]}><Select placeholder="选择模型" /></Form.Item>
          <Form.Item name="dataSource" label="数据源路径" rules={[{ required: true }]}><Input placeholder="/data/input.csv" /></Form.Item>
          <Form.Item name="outputPath" label="输出路径" rules={[{ required: true }]}><Input placeholder="/data/output/" /></Form.Item>
          <Form.Item name="schedule" label="调度 (cron)"><Input placeholder="0 0 * * *" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default BatchServiceList;
