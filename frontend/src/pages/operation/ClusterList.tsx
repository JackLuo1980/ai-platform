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
    message.success('Saved');
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
      title: 'Confirm Delete',
      content: 'Delete this cluster?',
      onOk: async function () {
        await deleteCluster(id);
        message.success('Deleted');
        loadClusters();
      },
    });
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadClusters}>Refresh</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setEditing(null); form.resetFields(); setModalOpen(true); }}>Add Cluster</Button>
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
                  <Button type="link" onClick={function () { handleEdit(c); }}>Edit</Button>,
                  <Button type="link" danger onClick={function () { handleDelete(c.id); }}>Delete</Button>,
                ]}
              >
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div><Tag>Type: {c.type}</Tag></div>
                  <Statistic title="Nodes" value={c.nodeCount || 0} />
                  <Statistic title="CPU Total" value={c.cpuTotal || 0} suffix="cores" />
                  <Statistic title="GPU Total" value={c.gpuTotal || 0} suffix="cards" />
                  <Statistic title="Memory Total" value={c.memoryTotal || 0} suffix="GB" />
                  <Statistic title="Storage Total" value={c.storageTotal || 0} suffix="GB" />
                </Space>
              </Card>
            </Col>
          );
        })}
      </Row>
      <Modal title={editing ? 'Edit Cluster' : 'Add Cluster'} open={modalOpen} onCancel={function () { setModalOpen(false); }} onOk={function () { form.submit(); }} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="Type" rules={[{ required: true }]}>
            <Select options={[{ label: 'Kubernetes', value: 'k8s' }, { label: 'Standalone', value: 'standalone' }, { label: 'YARN', value: 'yarn' }]} />
          </Form.Item>
          <Form.Item name="endpoint" label="Endpoint">
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default ClusterList;
