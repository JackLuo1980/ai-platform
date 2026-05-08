import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listEnvironments, createEnvironment, updateEnvironment, deleteEnvironment } from '@/services/operation';

function EnvironmentList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

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
      await updateEnvironment(editing.id as string, values);
    } else {
      await createEnvironment(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确认删除此环境？',
      onOk: async function () {
        await deleteEnvironment(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '镜像', dataIndex: 'imageName' },
    { title: '类型', dataIndex: 'type' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
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
        fetchData={listEnvironments}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新增环境</Button>}
      />
      <DrawerForm
        title={editing ? '编辑环境' : '新增环境'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="imageId" label="镜像" rules={[{ required: true }]}>
          <Select placeholder="请选择镜像" />
        </Form.Item>
        <Form.Item name="type" label="类型">
          <Select options={[{ label: '笔记本', value: 'notebook' }, { label: '训练', value: 'training' }, { label: '推理', value: 'inference' }]} />
        </Form.Item>
        <Form.Item name="pipPackages" label="Pip 包">
          <Select mode="tags" placeholder="添加包" />
        </Form.Item>
        <Form.Item name="envVars" label="环境变量">
          <Input.TextArea rows={3} placeholder="每行一个 KEY=VALUE" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default EnvironmentList;
