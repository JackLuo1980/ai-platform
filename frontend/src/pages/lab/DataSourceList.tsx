import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ApiOutlined, LoadingOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import StatusTag from '@/components/StatusTag';
import { listDataSources, createDataSource, updateDataSource, deleteDataSource, testDataSourceConnection } from '@/services/lab';

function DataSourceList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [testing, setTesting] = useState<string | null>(null);
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
      await updateDataSource(editing.id as string, values);
    } else {
      await createDataSource(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Delete this data source?',
      onOk: async function () {
        await deleteDataSource(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleTest(id: string) {
    setTesting(id);
    try {
      await testDataSourceConnection(id);
      message.success('Connection successful');
    } catch {
      message.error('Connection failed');
    } finally {
      setTesting(null);
    }
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Type', dataIndex: 'type' },
    { title: 'Host', dataIndex: 'host' },
    { title: 'Database', dataIndex: 'database' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={testing === record.id ? <LoadingOutlined /> : <ApiOutlined />} onClick={function () { handleTest(record.id as string); }} disabled={!!testing}>
              Test
            </Button>
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
        fetchData={listDataSources}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Data Source</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Data Source' : 'New Data Source'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true }]}>
          <Select options={[{ label: 'MySQL', value: 'mysql' }, { label: 'PostgreSQL', value: 'postgresql' }, { label: 'Hive', value: 'hive' }, { label: 'HDFS', value: 'hdfs' }, { label: 'S3', value: 's3' }, { label: 'MinIO', value: 'minio' }]} />
        </Form.Item>
        <Form.Item name="host" label="Host" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="port" label="Port">
          <Input />
        </Form.Item>
        <Form.Item name="database" label="Database">
          <Input />
        </Form.Item>
        <Form.Item name="username" label="Username">
          <Input />
        </Form.Item>
        <Form.Item name="password" label="Password">
          <Input.Password />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default DataSourceList;
