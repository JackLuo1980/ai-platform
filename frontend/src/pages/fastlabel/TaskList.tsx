import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, message, Tag, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getLabelTasks, createLabelTask } from '@/services/fastlabel';

const { Option } = Select;

export default function TaskListPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);
  const [createForm] = Form.useForm();
  const navigate = useNavigate();

  function fetchTasks() {
    setLoading(true);
    getLabelTasks({ page, pageSize })
      .then(function (res) {
        setTasks(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load tasks'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchTasks(); }, [page, pageSize]);

  function handleCreate(values: any) {
    createLabelTask(values)
      .then(function () {
        message.success('Task created');
        setCreateVisible(false);
        createForm.resetFields();
        fetchTasks();
      })
      .catch(function () { message.error('Failed to create task'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Dataset', dataIndex: 'dataset', key: 'dataset' },
    { title: 'Assignee', dataIndex: 'assignee', key: 'assignee' },
    { title: 'Reviewer', dataIndex: 'reviewer', key: 'reviewer' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { pending: 'default', in_progress: 'orange', completed: 'green', reviewing: 'blue' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    {
      title: 'Progress',
      dataIndex: 'progress',
      key: 'progress',
      render: function (progress: number) {
        return progress !== undefined ? progress.toFixed(1) + '%' : '-';
      },
    },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Label Tasks</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setCreateVisible(true); }}>Create Task</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={tasks}
        loading={loading}
        onRow={function (record: any) {
          return { onClick: function () { navigate('/fastlabel/annotation/' + record.id); }, style: { cursor: 'pointer' } };
        }}
        pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }}
      />
      <Modal title="Create Label Task" open={createVisible} onCancel={function () { setCreateVisible(false); }} onOk={function () { createForm.submit(); }}>
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="Task Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="datasetId" label="Dataset" rules={[{ required: true }]}><Select placeholder="Select dataset"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="assignee" label="Assignee" rules={[{ required: true }]}><Select placeholder="Select assignee"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="reviewer" label="Reviewer"><Select placeholder="Select reviewer"><Option value="">Select</Option></Select></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
