import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Tag, Space } from 'antd';
import { PlusOutlined, ApiOutlined } from '@ant-design/icons';
import { getOnlineServices, deployOnline, predictOnline, stopOnline } from '@/services/inference';

const { Option } = Select;
const { TextArea } = Input;

export default function OnlineServiceListPage() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [deployVisible, setDeployVisible] = useState(false);
  const [predictVisible, setPredictVisible] = useState(false);
  const [predictService, setPredictService] = useState<any>(null);
  const [predictInput, setPredictInput] = useState('');
  const [predictResult, setPredictResult] = useState<any>(null);
  const [predictLoading, setPredictLoading] = useState(false);
  const [deployForm] = Form.useForm();

  function fetchServices() {
    setLoading(true);
    getOnlineServices({ page, pageSize })
      .then(function (res) {
        setServices(res.data.data.items || []);
        setTotal(res.data.data.total || 0);
      })
      .catch(function () { message.error('Failed to load services'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchServices(); }, [page, pageSize]);

  function handleDeploy(values: any) {
    deployOnline(values)
      .then(function () {
        message.success('Service deployed');
        setDeployVisible(false);
        deployForm.resetFields();
        fetchServices();
      })
      .catch(function () { message.error('Deploy failed'); });
  }

  function handlePredict() {
    if (!predictService) return;
    setPredictLoading(true);
    setPredictResult(null);
    let parsed: any;
    try { parsed = JSON.parse(predictInput); } catch (e) { message.error('Invalid JSON'); setPredictLoading(false); return; }
    predictOnline(predictService.id, { input: parsed })
      .then(function (res) {
        setPredictResult(res.data.data);
        message.success('Prediction complete');
      })
      .catch(function () { message.error('Prediction failed'); })
      .finally(function () { setPredictLoading(false); });
  }

  function handleStop(id: string) {
    stopOnline(id)
      .then(function () { message.success('Service stopped'); fetchServices(); })
      .catch(function () { message.error('Stop failed'); });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Model', dataIndex: 'model', key: 'model' },
    { title: 'Release Type', dataIndex: 'releaseType', key: 'releaseType' },
    { title: 'Replicas', dataIndex: 'replicas', key: 'replicas' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: function (status: string) {
        const colors: Record<string, string> = { running: 'green', stopped: 'red', deploying: 'orange' };
        return <Tag color={colors[status] || 'default'}>{status}</Tag>;
      },
    },
    { title: 'Endpoint', dataIndex: 'endpoint', key: 'endpoint', ellipsis: true },
    { title: 'Created At', dataIndex: 'createdAt', key: 'createdAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            <Button size="small" icon={<ApiOutlined />} onClick={function () { setPredictService(record); setPredictVisible(true); setPredictInput(''); setPredictResult(null); }}>Predict</Button>
            {record.status === 'running' ? <Button size="small" danger onClick={function () { handleStop(record.id); }}>Stop</Button> : null}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Online Services</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setDeployVisible(true); }}>Deploy</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={services} loading={loading} pagination={{ current: page, pageSize: pageSize, total: total, onChange: function (p, ps) { setPage(p); setPageSize(ps); } }} />
      <Modal title="Deploy Online Service" open={deployVisible} onCancel={function () { setDeployVisible(false); }} onOk={function () { deployForm.submit(); }}>
        <Form form={deployForm} layout="vertical" onFinish={handleDeploy}>
          <Form.Item name="name" label="Service Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="modelId" label="Model" rules={[{ required: true }]}><Select placeholder="Select model"><Option value="">Select</Option></Select></Form.Item>
          <Form.Item name="replicas" label="Replicas" initialValue={1}><InputNumber min={1} max={10} /></Form.Item>
          <Form.Item name="cpu" label="CPU (cores)" initialValue={1}><InputNumber min={0.5} step={0.5} /></Form.Item>
          <Form.Item name="memory" label="Memory (GB)" initialValue={2}><InputNumber min={1} step={1} /></Form.Item>
        </Form>
      </Modal>
      <Modal title={"Predict - " + (predictService?.name || '')} open={predictVisible} onCancel={function () { setPredictVisible(false); }} onOk={handlePredict} confirmLoading={predictLoading} width={640}>
        <div style={{ marginBottom: 12 }}><label>JSON Input:</label><TextArea rows={6} value={predictInput} onChange={function (e) { setPredictInput(e.target.value); }} placeholder='{"key": "value"}' /></div>
        {predictResult ? <div><label>Result:</label><pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, maxHeight: 300, overflow: 'auto' }}>{JSON.stringify(predictResult, null, 2)}</pre></div> : null}
      </Modal>
    </div>
  );
}
