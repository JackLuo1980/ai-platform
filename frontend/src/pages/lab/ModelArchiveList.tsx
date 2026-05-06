import { useState } from 'react';
import { Button, Modal, Space, message } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import StatusTag from '@/components/StatusTag';
import { listArchives, approveArchive, rejectArchive } from '@/services/lab';

function ModelArchiveList() {
  const [refreshKey, setRefreshKey] = useState(0);

  async function handleApprove(id: string) {
    Modal.confirm({
      title: 'Approve Model',
      content: 'Approve this model for production?',
      onOk: async function () {
        await approveArchive(id);
        message.success('Approved');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleReject(id: string) {
    Modal.confirm({
      title: 'Reject Model',
      content: 'Reject this model?',
      okText: 'Reject',
      okButtonProps: { danger: true },
      onOk: async function () {
        await rejectArchive(id, 'Rejected by reviewer');
        message.success('Rejected');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Model Name', dataIndex: 'modelName', sorter: true },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Framework', dataIndex: 'framework' },
    { title: 'Metrics', dataIndex: 'metrics', ellipsis: true },
    { title: 'Submitted By', dataIndex: 'submittedBy' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Submitted', dataIndex: 'submittedAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            {record.status === 'pending' ? (
              <>
                <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }} onClick={function () { handleApprove(record.id as string); }}>Approve</Button>
                <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={function () { handleReject(record.id as string); }}>Reject</Button>
              </>
            ) : null}
          </Space>
        );
      },
    },
  ];

  return (
    <ProTable
      key={refreshKey}
      columns={columns}
      fetchData={listArchives}
      searchFields={[{ key: 'modelName', label: 'Model Name' }]}
    />
  );
}

export default ModelArchiveList;
