import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listImages, createImage, updateImage, deleteImage } from '@/services/operation';

function ImageList() {
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
      await updateImage(editing.id as string, values);
    } else {
      await createImage(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确认删除此镜像？',
      onOk: async function () {
        await deleteImage(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '版本', dataIndex: 'version' },
    { title: '类型', dataIndex: 'type' },
    { title: '框架', dataIndex: 'framework' },
    { title: 'Python', dataIndex: 'pythonVersion' },
    { title: 'CUDA', dataIndex: 'cudaVersion' },
    { title: '大小', dataIndex: 'size' },
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
        fetchData={listImages}
        searchFields={[{ key: 'name', label: '名称' }, { key: 'framework', label: '框架' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新增镜像</Button>}
      />
      <DrawerForm
        title={editing ? '编辑镜像' : '新增镜像'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="version" label="版本">
          <Input />
        </Form.Item>
        <Form.Item name="type" label="类型">
          <Select options={[{ label: 'CPU', value: 'cpu' }, { label: 'GPU', value: 'gpu' }]} />
        </Form.Item>
        <Form.Item name="framework" label="框架">
          <Select options={[{ label: 'PyTorch', value: 'pytorch' }, { label: 'TensorFlow', value: 'tensorflow' }, { label: 'MXNet', value: 'mxnet' }]} />
        </Form.Item>
        <Form.Item name="registryUrl" label="仓库地址">
          <Input />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default ImageList;
