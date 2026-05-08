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
      title: '审批通过',
      content: '确定将该模型审批通过投入生产？',
      onOk: async function () {
        await approveArchive(id);
        message.success('已通过');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  async function handleReject(id: string) {
    Modal.confirm({
      title: '驳回模型',
      content: '确定驳回该模型？',
      okText: '驳回',
      okButtonProps: { danger: true },
      onOk: async function () {
        await rejectArchive(id, '审核人驳回');
        message.success('已驳回');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '模型名称', dataIndex: 'modelName', sorter: true },
    { title: '版本', dataIndex: 'version' },
    { title: '框架', dataIndex: 'framework' },
    { title: '指标', dataIndex: 'metrics', ellipsis: true },
    { title: '提交人', dataIndex: 'submittedBy' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '提交时间', dataIndex: 'submittedAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            {record.status === 'pending' ? (
              <>
                <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }} onClick={function () { handleApprove(record.id as string); }}>通过</Button>
                <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={function () { handleReject(record.id as string); }}>驳回</Button>
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
      searchFields={[{ key: 'modelName', label: '模型名称' }]}
    />
  );
}

export default ModelArchiveList;
