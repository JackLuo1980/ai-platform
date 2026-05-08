import { useState } from 'react';
import { Button, Modal, Form, Input, Select, Space, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, CodeOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import DrawerForm from '@/components/DrawerForm';
import { listOperators, createOperator, updateOperator, deleteOperator } from '@/services/lab';

function OperatorList() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [codeOpen, setCodeOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [codeTarget, setCodeTarget] = useState<Record<string, unknown> | null>(null);
  const [codeValue, setCodeValue] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreate() {
    setEditing(null);
    setDrawerOpen(true);
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    setDrawerOpen(true);
  }

  function handleViewCode(record: Record<string, unknown>) {
    setCodeTarget(record);
    setCodeValue((record.code || record.content || '') as string);
    setCodeOpen(true);
  }

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateOperator(editing.id as string, values);
    } else {
      await createOperator(values);
    }
    setRefreshKey(function (k) { return k + 1; });
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定删除该算子？',
      onOk: async function () {
        await deleteOperator(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '类型', dataIndex: 'type', render: function (type: string) { return <Tag>{type}</Tag>; } },
    { title: '分类', dataIndex: 'category' },
    { title: '版本', dataIndex: 'version' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<CodeOutlined />} onClick={function () { handleViewCode(record); }}>代码</Button>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>编辑</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>删除</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Select
            style={{ width: 160 }}
            placeholder="按类型筛选"
            allowClear
            value={typeFilter || undefined}
            onChange={setTypeFilter}
            options={[{ label: '预设', value: 'preset' }, { label: '自定义', value: 'custom' }]}
          />
        </Space>
      </div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={function (params) { return listOperators({ ...params, type: typeFilter || undefined }); }}
        searchFields={[{ key: 'name', label: '名称' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建算子</Button>}
      />
      <DrawerForm
        title={editing ? '编辑算子' : '新建算子'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="类型" rules={[{ required: true }]}>
          <Select options={[{ label: '预设', value: 'preset' }, { label: '自定义', value: 'custom' }]} />
        </Form.Item>
        <Form.Item name="category" label="分类">
          <Select options={[{ label: '数据处理', value: 'data' }, { label: '特征工程', value: 'feature' }, { label: '模型训练', value: 'training' }, { label: '模型评估', value: 'evaluation' }]} />
        </Form.Item>
        <Form.Item name="code" label="代码">
          <Input.TextArea rows={10} style={{ fontFamily: 'monospace' }} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
      <Modal title={codeTarget?.name ? `代码: ${codeTarget.name}` : '代码'} open={codeOpen} onCancel={function () { setCodeOpen(false); }} footer={null} width={800}>
        <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 4, overflow: 'auto', maxHeight: 500, fontSize: 13, fontFamily: 'monospace' }}>
          {codeValue || '暂无代码'}
        </pre>
      </Modal>
    </div>
  );
}

export default OperatorList;
