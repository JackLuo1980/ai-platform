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
      title: 'Confirm Delete',
      content: 'Delete this image?',
      onOk: async function () {
        await deleteImage(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Type', dataIndex: 'type' },
    { title: 'Framework', dataIndex: 'framework' },
    { title: 'Python', dataIndex: 'pythonVersion' },
    { title: 'CUDA', dataIndex: 'cudaVersion' },
    { title: 'Size', dataIndex: 'size' },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
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
        fetchData={listImages}
        searchFields={[{ key: 'name', label: 'Name' }, { key: 'framework', label: 'Framework' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Image</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Image' : 'New Image'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="version" label="Version">
          <Input />
        </Form.Item>
        <Form.Item name="type" label="Type">
          <Select options={[{ label: 'CPU', value: 'cpu' }, { label: 'GPU', value: 'gpu' }]} />
        </Form.Item>
        <Form.Item name="framework" label="Framework">
          <Select options={[{ label: 'PyTorch', value: 'pytorch' }, { label: 'TensorFlow', value: 'tensorflow' }, { label: 'MXNet', value: 'mxnet' }]} />
        </Form.Item>
        <Form.Item name="registryUrl" label="Registry URL">
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default ImageList;
