import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Space } from 'antd';
import { PlusOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { listModels, importModel, approveModel, deleteModel } from '@/services/inference';

function ModelList() {
  const [models, setModels] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [importVisible, setImportVisible] = useState(false);
  const [importForm] = Form.useForm();

  function fetchModels() {
    setLoading(true);
    listModels({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setModels(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载模型失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchModels(); }, [page, pageSize]);

  function handleImport(values: Record<string, unknown>) {
    importModel(values)
      .then(function () {
        message.success('模型已导入');
        setImportVisible(false);
        importForm.resetFields();
        fetchModels();
      })
      .catch(function () { message.error('导入失败'); });
  }

  function handleApprove(id: string) {
    approveModel(id)
      .then(function () {
        message.success('已审批');
        fetchModels();
      })
      .catch(function () { message.error('操作失败'); });
  }

  function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该模型吗？',
      onOk: function () {
        deleteModel(id)
          .then(function () { message.success('已删除'); fetchModels(); })
          .catch(function () { message.error('删除失败'); });
      },
    });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    pending: { color: 'orange', label: '待审批' },
    approved: { color: 'green', label: '已审批' },
    rejected: { color: 'red', label: '已拒绝' },
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '版本', dataIndex: 'version' },
    { title: '格式', dataIndex: 'format' },
    {
      title: '状态',
      dataIndex: 'status',
      render: function (status: string) {
        const cfg = STATUS_MAP[status] || { color: 'default', label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    { title: '来源', dataIndex: 'source' },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            {record.status === 'pending' ? (
              <>
                <Button size="small" type="primary" icon={<CheckOutlined />} onClick={function () { handleApprove(record.id as string); }}>审批</Button>
                <Button size="small" danger icon={<CloseOutlined />} onClick={function () { handleDelete(record.id as string); }}>拒绝</Button>
              </>
            ) : null}
            <Button size="small" danger onClick={function () { handleDelete(record.id as string); }}>删除</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>模型管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setImportVisible(true); }}>导入模型</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={models}
        loading={loading}
        pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }}
      />
      <Modal
        title="导入模型"
        open={importVisible}
        onCancel={function () { setImportVisible(false); }}
        onOk={function () { importForm.submit(); }}
      >
        <Form form={importForm} layout="vertical" onFinish={handleImport}>
          <Form.Item name="name" label="模型名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="source" label="来源" rules={[{ required: true }]}>
            <Select placeholder="选择来源">
              <Select.Option value="lab">实验室</Select.Option>
              <Select.Option value="upload">上传文件</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="format" label="格式" rules={[{ required: true }]}>
            <Select placeholder="选择格式">
              <Select.Option value="onnx">ONNX</Select.Option>
              <Select.Option value="pytorch">PyTorch</Select.Option>
              <Select.Option value="tensorflow">TensorFlow</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="version" label="版本">
            <Input placeholder="如 1.0.0" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default ModelList;
