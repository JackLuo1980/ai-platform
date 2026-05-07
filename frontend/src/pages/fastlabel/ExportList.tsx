import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Select, message, Space } from 'antd';
import { ExportOutlined, CloudUploadOutlined } from '@ant-design/icons';
import { listExports, createExport, pushToLab } from '@/services/fastlabel';

function ExportList() {
  const [exports, setExports] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [exportVisible, setExportVisible] = useState(false);
  const [exportForm] = Form.useForm();

  function fetchExports() {
    setLoading(true);
    listExports({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setExports(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchExports(); }, [page, pageSize]);

  function handleExport(values: Record<string, unknown>) {
    createExport(values)
      .then(function () {
        message.success('导出已开始');
        setExportVisible(false);
        exportForm.resetFields();
        fetchExports();
      })
      .catch(function () { message.error('导出失败'); });
  }

  function handlePushToLab(id: string) {
    pushToLab(id)
      .then(function () { message.success('已推送到实验室'); fetchExports(); })
      .catch(function () { message.error('推送失败'); });
  }

  function handleDownload(record: Record<string, unknown>) {
    if (!record.downloadUrl) { message.warning('文件未就绪'); return; }
    window.open(record.downloadUrl as string, '_blank');
  }

  const columns = [
    { title: '任务', dataIndex: 'taskName' },
    { title: '格式', dataIndex: 'format' },
    { title: '数据量', dataIndex: 'itemCount' },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button size="small" onClick={function () { handleDownload(record); }}>下载</Button>
            <Button size="small" icon={<CloudUploadOutlined />} onClick={function () { handlePushToLab(record.id as string); }}>推送到实验室</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>导出管理</h2>
        <Button type="primary" icon={<ExportOutlined />} onClick={function () { setExportVisible(true); }}>导出</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={exports} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="导出标注" open={exportVisible} onCancel={function () { setExportVisible(false); }} onOk={function () { exportForm.submit(); }}>
        <Form form={exportForm} layout="vertical" onFinish={handleExport}>
          <Form.Item name="taskId" label="任务" rules={[{ required: true }]}>
            <Select placeholder="选择任务" />
          </Form.Item>
          <Form.Item name="format" label="格式" rules={[{ required: true }]}>
            <Select placeholder="选择格式">
              <Select.Option value="coco">COCO</Select.Option>
              <Select.Option value="json">JSON</Select.Option>
              <Select.Option value="csv">CSV</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default ExportList;
