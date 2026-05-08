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
      title: '确认删除',
      content: '确定删除该工作流？',
      onOk: async function () {
        await deleteWorkflow(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleRun(id: string) {
    Modal.confirm({
      title: '运行工作流',
      content: '确定开始执行该工作流？',
      onOk: async function () {
        await runWorkflow(id);
        message.success('工作流已启动');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '节点数', dataIndex: 'nodeCount' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '上次运行', dataIndex: 'lastRunAt' },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<ApartmentOutlined />} onClick={function () { navigate(`/lab/workflows/${record.id}`); }}>DAG</Button>
            <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={function () { handleRun(record.id as string); }}>运行</Button>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>删除</Button>
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
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建工作流</Button>}
      />
      <DrawerForm
        title={editing ? '编辑工作流' : '新建工作流'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default WorkflowList;
