import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { listTasks, createTask } from '@/services/fastlabel';

function TaskList() {
  const [tasks, setTasks] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  function fetchTasks() {
    setLoading(true);
    listTasks({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setTasks(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchTasks(); }, [page, pageSize]);

  function handleCreate(values: Record<string, unknown>) {
    createTask(values)
      .then(function () {
        message.success('任务已创建');
        setCreateVisible(false);
        createForm.resetFields();
        fetchTasks();
      })
      .catch(function () { message.error('创建失败'); });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    pending: { color: 'default', label: '等待中' },
    in_progress: { color: 'orange', label: '进行中' },
    completed: { color: 'green', label: '已完成' },
    reviewing: { color: 'blue', label: '审核中' },
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '数据集', dataIndex: 'datasetName' },
    { title: '标注员', dataIndex: 'assigneeName' },
    { title: '审核员', dataIndex: 'reviewerName' },
    {
      title: '状态',
      dataIndex: 'status',
      render: function (status: string) {
        const cfg = STATUS_MAP[status] || { color: 'default', label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    {
      title: '进度',
      dataIndex: 'progress',
      render: function (progress: number) {
        return progress !== undefined ? progress.toFixed(1) + '%' : '-';
      },
    },
    { title: '创建时间', dataIndex: 'createdAt' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>标注任务</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>创建任务</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={tasks}
        loading={loading}
        onRow={function (record: Record<string, unknown>) {
          return { onClick: function () { navigate('/fastlabel/annotation/' + record.id); }, style: { cursor: 'pointer' } } as any;
        }}
        pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }}
      />
      <Modal title="创建标注任务" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="任务名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="datasetId" label="数据集" rules={[{ required: true }]}><Select placeholder="选择数据集" /></Form.Item>
          <Form.Item name="assigneeId" label="标注员" rules={[{ required: true }]}><Select placeholder="选择标注员" /></Form.Item>
          <Form.Item name="reviewerId" label="审核员"><Select placeholder="选择审核员" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default TaskList;
