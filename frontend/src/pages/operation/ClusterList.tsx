import { useState } from 'react';
import { Row, Col, Card, Badge, Button, Modal, Form, Input, Select, Space, message, Statistic, Tag } from 'antd';
import { PlusOutlined, ReloadOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined } from '@ant-design/icons';
import StatusTag from '@/components/StatusTag';
import { listClusters, createCluster, updateCluster, deleteCluster, getClusterStatus } from '@/services/operation';

function ClusterList() {
  const [clusters, setClusters] = useState<Record<string, unknown>[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [form] = Form.useForm();

  async function loadClusters() {
    const res: any = await listClusters({ size: 999 });
    setClusters(res.data?.items || res.items || []);
  }

  useState(function () {
    loadClusters();
  });

  async function handleSubmit(values: Record<string, unknown>) {
    if (editing) {
      await updateCluster(editing.id as string, values);
    } else {
      await createCluster(values);
    }
    message.success('保存成功');
    setModalOpen(false);
    loadClusters();
  }

  function handleEdit(record: Record<string, unknown>) {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  }

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确认删除此集群？',
      onOk: async function () {
        await deleteCluster(id);
        message.success('已删除');
        loadClusters();
      },
    });
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadClusters}>刷新</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setEditing(null); form.resetFields(); setModalOpen(true); }}>新增集群</Button>
        </Space>
      </div>
      <Row gutter={[16, 16]}>
        {clusters.map(function (c: any) {
          return (
            <Col xs={24} sm={12} lg={8} xl={6} key={c.id}>
              <Card
                title={c.name}
                extra={<StatusTag status={c.status} />}
                actions={[
                  <Button type="link" onClick={function () { handleEdit(c); }}>编辑</Button>,
                  <Button type="link" danger onClick={function () { handleDelete(c.id); }}>删除</Button>,
                ]}
              >
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div><Tag>类型：{c.type}</Tag></div>
                  <Statistic title="节点数" value={c.nodeCount || 0} />
                  <Statistic title="CPU 总量" value={c.cpuTotal || 0} suffix="核" />
                  <Statistic title="GPU 总量" value={c.gpuTotal || 0} suffix="卡" />
                  <Statistic title="内存总量" value={c.memoryTotal || 0} suffix="GB" />
                  <Statistic title="存储总量" value={c.storageTotal || 0} suffix="GB" />
                </Space>
              </Card>
            </Col>
          );
        })}
      </Row>
      <Modal title={editing ? '编辑集群' : '新增集群'} open={modalOpen} onCancel={function () { setModalOpen(false); }} onOk={function () { form.submit(); }} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true }]}>
            <Select options={[{ label: 'Kubernetes', value: 'k8s' }, { label: '独立', value: 'standalone' }, { label: 'YARN', value: 'yarn' }]} />
          </Form.Item>
          <Form.Item name="endpoint" label="接入点">
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default ClusterList;
