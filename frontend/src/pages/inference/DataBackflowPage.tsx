import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Form, Input, Tag, Space, message, Statistic, Row, Col } from 'antd'
import { CloudUploadOutlined, ReloadOutlined } from '@ant-design/icons'
import { executeBackflow, listBackflowTasks } from '@/services/inference'

export default function DataBackflowPage() {
  const [tasks, setTasks] = useState<Record<string, unknown>[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [stats, setStats] = useState({ total: 0, completed: 0, failed: 0, records: 0 })

  function fetchTasks() {
    setLoading(true)
    listBackflowTasks()
      .then(function (res: any) {
        var d = res?.data?.data?.items || res?.data?.items || res?.data || []
        var list = Array.isArray(d) ? d : []
        setTasks(list)
        var total = list.length
        var completed = list.filter(function (t: any) { return t.status === 'COMPLETED' }).length
        var failed = list.filter(function (t: any) { return t.status === 'FAILED' }).length
        var records = list.reduce(function (sum: number, t: any) { return sum + (t.recordCount || 0) }, 0)
        setStats({ total: total, completed: completed, failed: failed, records: records })
      })
      .catch(function () { message.error('加载失败') })
      .finally(function () { setLoading(false) })
  }

  useEffect(function () { fetchTasks() }, [])

  function handleExecute() {
    form.validateFields().then(function (values) {
      executeBackflow(values)
        .then(function () {
          message.success('数据回流任务已提交')
          setModalVisible(false)
          form.resetFields()
          fetchTasks()
        })
        .catch(function () { message.error('提交失败') })
    })
  }

  var statusColors: Record<string, string> = {
    PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red'
  }

  var columns = [
    { title: '任务ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '服务ID', dataIndex: 'serviceId', key: 'serviceId', width: 80 },
    { title: '数据集名称', dataIndex: 'targetDatasetName', key: 'targetDatasetName' },
    { title: '记录数', dataIndex: 'recordCount', key: 'recordCount', width: 80 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: function (s: string) {
      return <Tag color={statusColors[s] || 'default'}>{s}</Tag>
    }},
    { title: '目标数据集ID', dataIndex: 'targetDatasetId', key: 'targetDatasetId', width: 100 },
    { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage', ellipsis: true },
    { title: '开始时间', dataIndex: 'startedAt', key: 'startedAt', width: 160 },
    { title: '完成时间', dataIndex: 'completedAt', key: 'completedAt', width: 160 },
  ]

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}><Card><Statistic title="回流任务总数" value={stats.total} /></Card></Col>
        <Col span={6}><Card><Statistic title="已完成" value={stats.completed} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="失败" value={stats.failed} valueStyle={{ color: '#ff4d4f' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="回流记录总数" value={stats.records} /></Card></Col>
      </Row>
      <Card title="数据回流" extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={fetchTasks}>刷新</Button>
          <Button type="primary" icon={<CloudUploadOutlined />} onClick={function () { setModalVisible(true) }}>新建回流任务</Button>
        </Space>
      }>
        <Table columns={columns} dataSource={tasks} rowKey="id" loading={loading} pagination={{ pageSize: 20 }} />
      </Card>
      <Modal title="新建数据回流任务" open={modalVisible} onOk={handleExecute} onCancel={function () { setModalVisible(false) }} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="serviceId" label="在线服务ID" rules={[{ required: true, message: '请输入服务ID' }]}>
            <Input type="number" placeholder="输入在线推理服务ID" />
          </Form.Item>
          <Form.Item name="datasetName" label="目标数据集名称">
            <Input placeholder="留空自动生成" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
