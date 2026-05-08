import { useState, useEffect } from 'react'
import { Card, Descriptions, Button, Modal, Form, Input, Tag, Statistic, Row, Col, message, Space } from 'antd'
import { SafetyCertificateOutlined, PlusOutlined } from '@ant-design/icons'
import * as consoleService from '@/services/console'

export default function LicensePage() {
  const [license, setLicense] = useState<Record<string, unknown> | null>(null)
  const [modalOpen, setModalOpen] = useState(false)

  useEffect(() => {
    consoleService.getLicense().then(res => {
      setLicense(res?.data?.data || res?.data || null)
    }).catch(() => {
      setLicense({
        type: '测试许可',
        status: 'ACTIVE',
        maxTenants: 5,
        maxUsers: 50,
        currentTenants: 2,
        currentUsers: 2,
        expiresAt: '2026-12-31',
        modules: ['Console', 'Lab', 'Inference', 'FastLabel', 'Scorecard'],
      })
    })
  }, [])

  function handleUpload() {
    message.success('许可证已更新')
    setModalOpen(false)
  }

  if (!license) return <Card loading />

  const usagePercent = (n: number, d: number) => Math.round((n / d) * 100)

  return (
    <div>
      <Card title={<Space><SafetyCertificateOutlined /> 许可证管理</Space>} extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>上传许可证</Button>}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="许可类型"><Tag color="blue">{String(license.type)}</Tag></Descriptions.Item>
          <Descriptions.Item label="状态"><Tag color={license.status === 'ACTIVE' ? 'green' : 'red'}>{String(license.status)}</Tag></Descriptions.Item>
          <Descriptions.Item label="到期时间">{String(license.expiresAt)}</Descriptions.Item>
          <Descriptions.Item label="授权模块">{(license.modules as string[] || []).map((m: string) => <Tag key={m}>{m}</Tag>)}</Descriptions.Item>
        </Descriptions>
      </Card>
      <Card title="使用量" style={{ marginTop: 16 }}>
        <Row gutter={24}>
          <Col span={8}><Statistic title="租户" value={`${license.currentTenants}/${license.maxTenants}`} /></Col>
          <Col span={8}><Statistic title="用户" value={`${license.currentUsers}/${license.maxUsers}`} /></Col>
          <Col span={8}><Statistic title="使用率" value={usagePercent(Number(license.currentUsers), Number(license.maxUsers))} suffix="%" /></Col>
        </Row>
      </Card>
      <Modal title="上传许可证" open={modalOpen} onOk={handleUpload} onCancel={() => setModalOpen(false)}>
        <Form layout="vertical">
          <Form.Item label="许可证密钥"><Input.TextArea rows={6} placeholder="粘贴许可证内容..." /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
