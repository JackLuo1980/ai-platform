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
      title: '确认删除',
      content: '确定删除该数据源？',
      onOk: async function () {
        await deleteDataSource(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleTest(id: string) {
    setTesting(id);
    try {
      await testDataSourceConnection(id);
      message.success('连接成功');
    } catch {
      message.error('连接失败');
    } finally {
      setTesting(null);
    }
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '类型', dataIndex: 'type' },
    { title: '主机', dataIndex: 'host' },
    { title: '数据库', dataIndex: 'database' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={testing === record.id ? <LoadingOutlined /> : <ApiOutlined />} onClick={function () { handleTest(record.id as string); }} disabled={!!testing}>
              测试
            </Button>
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
        fetchData={listDataSources}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建数据源</Button>}
      />
      <DrawerForm
        title={editing ? '编辑数据源' : '新建数据源'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="类型" rules={[{ required: true }]}>
          <Select options={[{ label: 'MySQL', value: 'mysql' }, { label: 'PostgreSQL', value: 'postgresql' }, { label: 'Hive', value: 'hive' }, { label: 'HDFS', value: 'hdfs' }, { label: 'S3', value: 's3' }, { label: 'MinIO', value: 'minio' }]} />
        </Form.Item>
        <Form.Item name="host" label="主机" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="port" label="端口">
          <Input />
        </Form.Item>
        <Form.Item name="database" label="数据库">
          <Input />
        </Form.Item>
        <Form.Item name="username" label="用户名">
          <Input />
        </Form.Item>
        <Form.Item name="password" label="密码">
          <Input.Password />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default DataSourceList;
