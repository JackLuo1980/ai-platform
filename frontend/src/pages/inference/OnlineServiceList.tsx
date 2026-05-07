import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Tag, Space } from 'antd';
import { PlusOutlined, ApiOutlined } from '@ant-design/icons';
import { listOnlineServices, deployOnlineService, predictOnlineService, stopOnlineService } from '@/services/inference';

function OnlineServiceList() {
  const [services, setServices] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [deployVisible, setDeployVisible] = useState(false);
  const [predictVisible, setPredictVisible] = useState(false);
  const [predictService, setPredictService] = useState<Record<string, unknown> | null>(null);
  const [predictInput, setPredictInput] = useState('');
  const [predictResult, setPredictResult] = useState<unknown>(null);
  const [predictLoading, setPredictLoading] = useState(false);
  const [deployForm] = Form.useForm();

  function fetchServices() {
    setLoading(true);
    listOnlineServices({ page, size: pageSize })
      .then(function (res: any) {
        const d = res?.data || res || {};
        setServices(d.items || d.content || []);
        setTotal(d.total || d.totalElements || 0);
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchServices(); }, [page, pageSize]);

  function handleDeploy(values: Record<string, unknown>) {
    deployOnlineService(values)
      .then(function () {
        message.success('服务已部署');
        setDeployVisible(false);
        deployForm.resetFields();
        fetchServices();
      })
      .catch(function () { message.error('部署失败'); });
  }

  function handlePredict() {
    if (!predictService) return;
    setPredictLoading(true);
    setPredictResult(null);
    let parsed: unknown;
    try { parsed = JSON.parse(predictInput); } catch { message.error('JSON 格式错误'); setPredictLoading(false); return; }
    predictOnlineService(predictService.id as string, { input: parsed })
      .then(function (res: any) {
        setPredictResult(res?.data || res);
        message.success('预测完成');
      })
      .catch(function () { message.error('预测失败'); })
      .finally(function () { setPredictLoading(false); });
  }

  function handleStop(id: string) {
    stopOnlineService(id)
      .then(function () { message.success('服务已停止'); fetchServices(); })
      .catch(function () { message.error('停止失败'); });
  }

  const STATUS_MAP: Record<string, { color: string; label: string }> = {
    running: { color: 'green', label: '运行中' },
    stopped: { color: 'red', label: '已停止' },
    deploying: { color: 'orange', label: '部署中' },
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '模型', dataIndex: 'modelName' },
    { title: '发布类型', dataIndex: 'releaseType' },
    { title: '副本数', dataIndex: 'replicas' },
    {
      title: '状态',
      dataIndex: 'status',
      render: function (status: string) {
        const cfg = STATUS_MAP[status] || { color: 'default', label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    { title: '端点', dataIndex: 'endpoint', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button size="small" icon={<ApiOutlined />} onClick={function () { setPredictService(record); setPredictVisible(true); setPredictInput(''); setPredictResult(null); }}>预测</Button>
            {record.status === 'running' ? <Button size="small" danger onClick={function () { handleStop(record.id as string); }}>停止</Button> : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>在线服务</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setDeployVisible(true); }}>部署服务</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={services} loading={loading} pagination={{ current: page + 1, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p - 1); setPageSize(ps); } }} />
      <Modal title="部署在线服务" open={deployVisible} onCancel={function () { setDeployVisible(false); }} onOk={function () { deployForm.submit(); }}>
        <Form form={deployForm} layout="vertical" onFinish={handleDeploy}>
          <Form.Item name="name" label="服务名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="modelId" label="模型" rules={[{ required: true }]}><Select placeholder="选择模型" /></Form.Item>
          <Form.Item name="replicas" label="副本数" initialValue={1}><InputNumber min={1} max={10} /></Form.Item>
          <Form.Item name="cpu" label="CPU (核)" initialValue={1}><InputNumber min={0.5} step={0.5} /></Form.Item>
          <Form.Item name="memory" label="内存 (GB)" initialValue={2}><InputNumber min={1} step={1} /></Form.Item>
        </Form>
      </Modal>
      <Modal title={"预测 - " + (predictService?.name || '')} open={predictVisible} onCancel={function () { setPredictVisible(false); }} onOk={handlePredict} confirmLoading={predictLoading} width={640}>
        <div style={{ marginBottom: 12 }}><label>JSON 输入:</label><Input.TextArea rows={6} value={predictInput} onChange={function (e) { setPredictInput(e.target.value); }} placeholder='{"key": "value"}' /></div>
        {predictResult ? <div><label>结果:</label><pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, maxHeight: 300, overflow: 'auto' }}>{JSON.stringify(predictResult, null, 2)}</pre></div> : null}
      </Modal>
    </div>
  );
}

export default OnlineServiceList;
