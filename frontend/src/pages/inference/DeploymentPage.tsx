import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Form, Select, Input, InputNumber, Tag, Space, message, Progress } from 'antd'
import { RocketOutlined, ThunderboltOutlined, SwapOutlined } from '@ant-design/icons'
import * as inferenceService from '@/services/inference'

export default function DeploymentPage() {
  const [services, setServices] = useState<Record<string, unknown>[]>([])
  const [loading, setLoading] = useState(false)
  const [deployModal, setDeployModal] = useState(false)
  const [deployType, setDeployType] = useState('formal')
  const [form] = Form.useForm()

  function fetchData() {
    setLoading(true)
    inferenceService.listOnlineServices().then(res => {
      const d = res?.data?.data?.items || res?.data?.items || res?.data || []
      setServices(Array.isArray(d) ? d : [])
    }).catch(() => message.error('加载失败')).finally(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [])

  function handleDeploy() {
    form.validateFields().then(values => {
      const typeLabels: Record<string, string> = { canary: '灰度发布', shadow: '影子发布', formal: '正式发布' }
      message.success(`${typeLabels[deployType] || '发布'}已提交`)
      setDeployModal(false)
      form.resetFields()
    })
  }

  const statusColors: Record<string, string> = { RUNNING: 'green', STOPPED: 'red', DEPLOYING: 'blue', PENDING: 'orange' }

  const columns = [
    { title: '服务名称', dataIndex: 'name', key: 'name' },
    { title: '模型', dataIndex: 'modelName', key: 'modelName' },
    { title: '版本', dataIndex: 'modelVersion', key: 'modelVersion' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={statusColors[s] || 'default'}>{s}</Tag> },
    { title: '副本数', dataIndex: 'replicas', key: 'replicas' },
    { title: '发布类型', dataIndex: 'releaseType', key: 'releaseType', render: (t: string) => {
      const colors: Record<string, string> = { canary: 'gold', shadow: 'purple', formal: 'green' }
      const labels: Record<string, string> = { canary: '灰度', shadow: '影子', formal: '正式' }
      return <Tag color={colors[t] || 'default'}>{labels[t] || t || '正式'}</Tag>
    }},
    { title: '流量比例', dataIndex: 'trafficPercent', key: 'trafficPercent', render: (v: number) => v ? <Progress percent={v} size="small" style={{width:100}} /> : '-' },
    { title: '操作', key: 'action', render: (_: unknown, record: Record<string, unknown>) => (
      <Space>
        <Button size="small" type="link" onClick={() => { setDeployType('canary'); setDeployModal(true); form.setFieldsValue({ serviceId: record.id }) }}>灰度</Button>
        <Button size="small" type="link" onClick={() => { setDeployType('shadow'); setDeployModal(true); form.setFieldsValue({ serviceId: record.id }) }}>影子</Button>
        <Button size="small" type="link" danger onClick={() => message.info('停止服务: ' + record.name)}>停止</Button>
      </Space>
    )},
  ]

  return (
    <Card title="模型部署管理" extra={
      <Space>
        <Button type="primary" icon={<RocketOutlined />} onClick={() => { setDeployType('formal'); setDeployModal(true) }}>正式发布</Button>
        <Button icon={<ThunderboltOutlined />} onClick={() => { setDeployType('canary'); setDeployModal(true) }}>灰度发布</Button>
        <Button icon={<SwapOutlined />} onClick={() => { setDeployType('shadow'); setDeployModal(true) }}>影子发布</Button>
      </Space>
    }>
      <Table columns={columns} dataSource={services} rowKey="id" loading={loading} pagination={{ pageSize: 20 }} />
      <Modal title={deployType === 'canary' ? '灰度发布' : deployType === 'shadow' ? '影子发布' : '正式发布'} open={deployModal} onOk={handleDeploy} onCancel={() => setDeployModal(false)} width={500}>
        <Form form={form} layout="vertical">
          <Form.Item label="服务名称" name="serviceName" rules={[{ required: true, message: '请输入服务名称' }]}><Input /></Form.Item>
          <Form.Item label="模型" name="modelId"><Select placeholder="选择模型" options={[]} /></Form.Item>
          {deployType === 'canary' ? <Form.Item label="灰度流量比例 (%)" name="trafficPercent" initialValue={10}><InputNumber min={1} max={50} style={{width:'100%'}} /></Form.Item> : null}
          <Form.Item label="副本数" name="replicas" initialValue={1}><InputNumber min={1} max={10} style={{width:'100%'}} /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
