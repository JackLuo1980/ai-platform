import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Table, Tag, Progress, Timeline, Spin } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined, DashboardOutlined } from '@ant-design/icons'

interface ServiceHealth {
  key: string
  name: string
  status: string
  cpu: number
  memory: number
  uptime: string
  instances: number
}

export default function MonitoringDashboard() {
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setTimeout(() => setLoading(false), 500)
  }, [])

  const services: ServiceHealth[] = [
    { key: '1', name: 'Console', status: 'UP', cpu: 15, memory: 45, uptime: '6h 23m', instances: 1 },
    { key: '2', name: 'Operation', status: 'UP', cpu: 8, memory: 32, uptime: '6h 23m', instances: 1 },
    { key: '3', name: 'Lab', status: 'UP', cpu: 35, memory: 62, uptime: '2h 15m', instances: 1 },
    { key: '4', name: 'Inference', status: 'UP', cpu: 22, memory: 55, uptime: '2h 15m', instances: 1 },
    { key: '5', name: 'FastLabel', status: 'UP', cpu: 12, memory: 38, uptime: '2h 15m', instances: 1 },
    { key: '6', name: 'Scorecard', status: 'UP', cpu: 5, memory: 28, uptime: '2h 15m', instances: 1 },
    { key: '7', name: 'Gateway', status: 'UP', cpu: 18, memory: 41, uptime: '6h 23m', instances: 1 },
  ]

  const columns = [
    { title: '服务', dataIndex: 'name', key: 'name', render: function (n: string) { return <strong>{n}</strong> } },
    { title: '状态', dataIndex: 'status', key: 'status', render: function (s: string) { return <Tag icon={s === 'UP' ? <CheckCircleOutlined /> : <CloseCircleOutlined />} color={s === 'UP' ? 'success' : 'error'}>{s}</Tag> } },
    { title: 'CPU', dataIndex: 'cpu', key: 'cpu', render: function (v: number) { return <Progress percent={v} size="small" status={v > 80 ? 'exception' : 'active'} /> } },
    { title: '内存', dataIndex: 'memory', key: 'memory', render: function (v: number) { return <Progress percent={v} size="small" status={v > 80 ? 'exception' : 'active'} /> } },
    { title: '运行时间', dataIndex: 'uptime', key: 'uptime' },
    { title: '实例数', dataIndex: 'instances', key: 'instances' },
  ]

  if (loading) return <Card><Spin /></Card>

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}><Card><Statistic title="集群状态" value="健康" prefix={<DashboardOutlined />} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="总 CPU 使用率" value={16} suffix="%" valueStyle={{ color: '#1890ff' }} /><Progress percent={16} showInfo={false} /></Card></Col>
        <Col span={6}><Card><Statistic title="总内存使用率" value={43} suffix="%" valueStyle={{ color: '#722ed1' }} /><Progress percent={43} showInfo={false} /></Card></Col>
        <Col span={6}><Card><Statistic title="服务在线" value={7} suffix="/ 7" valueStyle={{ color: '#52c41a' }} /></Card></Col>
      </Row>

      <Card title="服务健康状态" style={{ marginBottom: 16 }}>
        <Table columns={columns} dataSource={services} size="small" pagination={false} />
      </Card>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="资源使用趋势">
            <div style={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#8c8c8c' }}>
              资源趋势图表（需要接入 Prometheus/Grafana）
            </div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="最近告警">
            <Timeline items={[
              { color: 'green', children: '全部服务正常运行' },
              { color: 'blue', children: 'Lab 服务 2小时前重启（版本更新）' },
              { color: 'blue', children: 'Inference 服务 2小时前重启（版本更新）' },
            ]} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
