import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, message, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ThunderboltOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listFeatureGroups, createFeatureGroup, updateFeatureGroup, deleteFeatureGroup, triggerFeatureComputation } from '@/services/lab';

function FeatureGroupList() {
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
      await updateFeatureGroup(editing.id as string, values);
    } else {
      await createFeatureGroup(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定删除该特征组？',
      onOk: async function () {
        await deleteFeatureGroup(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleCompute(id: string) {
    Modal.confirm({
      title: '触发计算',
      content: '确定计算该特征组的特征？',
      onOk: async function () {
        await triggerFeatureComputation(id);
        message.success('已触发计算');
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '实体', dataIndex: 'entity' },
    { title: '特征数', dataIndex: 'featureCount' },
    { title: '调度', dataIndex: 'schedule' },
    { title: '上次计算', dataIndex: 'lastComputedAt' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <Tag>{status}</Tag>; } },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<ThunderboltOutlined />} onClick={function () { handleCompute(record.id as string); }}>计算</Button>
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
        fetchData={listFeatureGroups}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建特征组</Button>}
      />
      <DrawerForm
        title={editing ? '编辑特征组' : '新建特征组'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="entity" label="实体" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="schedule" label="调度 (Cron)">
          <Input placeholder="0 0 * * *" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
    </div>
  );
}

export default FeatureGroupList;
