import { useState } from 'react';
import { Button, Modal, Form, Input, Space, message, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, ApartmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import StatusTag from '@/components/StatusTag';
import { listWorkflows, createWorkflow, updateWorkflow, deleteWorkflow, runWorkflow } from '@/services/lab';

function WorkflowList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const navigate = useNavigate();

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateWorkflow(editing.id as string, values);
    } else {
      await createWorkflow(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this workflow?',
      onOk: async function () {
        await deleteWorkflow(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleRun(id: string) {
    Modal.confirm({
      title: 'Run Workflow',
      content: 'Start executing this workflow?',
      onOk: async function () {
        await runWorkflow(id);
        message.success('Workflow started');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Description', dataIndex: 'description', ellipsis: true },
    { title: 'Nodes', dataIndex: 'nodeCount' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Last Run', dataIndex: 'lastRunAt' },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<ApartmentOutlined />} onClick={function () { navigate(`/lab/workflows/${record.id}`); }}>DAG</Button>
            <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={function () { handleRun(record.id as string); }}>Run</Button>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>Delete</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listWorkflows}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Workflow</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Workflow' : 'New Workflow'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default WorkflowList;
