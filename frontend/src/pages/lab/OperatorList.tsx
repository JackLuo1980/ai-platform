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
      title: 'Confirm Delete',
      content: 'Delete this operator?',
      onOk: async function () {
        await deleteOperator(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Type', dataIndex: 'type', render: function (type: string) { return <Tag>{type}</Tag>; } },
    { title: 'Category', dataIndex: 'category' },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Description', dataIndex: 'description', ellipsis: true },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<CodeOutlined />} onClick={function () { handleViewCode(record); }}>Code</Button>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={function () { handleEdit(record); }}>Edit</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>Delete</Button>
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
            placeholder="Filter by type"
            allowClear
            value={typeFilter || undefined}
            onChange={setTypeFilter}
            options={[{ label: 'Preset', value: 'preset' }, { label: 'Custom', value: 'custom' }]}
          />
        </Space>
      </div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={function (params) { return listOperators({ ...params, type: typeFilter || undefined }); }}
        searchFields={[{ key: 'name', label: 'Name' }]}
        toolbar={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Operator</Button>}
      />
      <DrawerForm
        title={editing ? 'Edit Operator' : 'New Operator'}
        open={drawerOpen}
        onClose={function () { setDrawerOpen(false); }}
        onSubmit={handleSubmit}
        initialValues={editing || undefined}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true }]}>
          <Select options={[{ label: 'Preset', value: 'preset' }, { label: 'Custom', value: 'custom' }]} />
        </Form.Item>
        <Form.Item name="category" label="Category">
          <Select options={[{ label: 'Data Processing', value: 'data' }, { label: 'Feature Engineering', value: 'feature' }, { label: 'Model Training', value: 'training' }, { label: 'Evaluation', value: 'evaluation' }]} />
        </Form.Item>
        <Form.Item name="code" label="Code">
          <Input.TextArea rows={10} style={{ fontFamily: 'monospace' }} />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </DrawerForm>
      <Modal title={codeTarget?.name ? `Code: ${codeTarget.name}` : 'Code'} open={codeOpen} onCancel={function () { setCodeOpen(false); }} footer={null} width={800}>
        <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 4, overflow: 'auto', maxHeight: 500, fontSize: 13, fontFamily: 'monospace' }}>
          {codeValue || 'No code available'}
        </pre>
      </Modal>
    </div>
  );
}

export default OperatorList;
