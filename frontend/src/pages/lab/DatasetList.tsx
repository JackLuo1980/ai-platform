import { useState } from 'react';
import { Button, Modal, Space, Upload, message } from 'antd';
import { PlusOutlined, DeleteOutlined, UploadOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import ProTable from '@/components/ProTable';
import StatusTag from '@/components/StatusTag';
import { listDatasets, createDataset, deleteDataset } from '@/services/lab';

function DatasetList() {
  const [refreshKey, setRefreshKey] = useState(0);
  const [uploadOpen, setUploadOpen] = useState(false);
  const navigate = useNavigate();

  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定删除该数据集？',
      onOk: async function () {
        await deleteDataset(id);
        message.success('已删除');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: '名称', dataIndex: 'name', sorter: true },
    { title: '类型', dataIndex: 'type' },
    { title: '格式', dataIndex: 'format' },
    { title: '行数', dataIndex: 'rowCount' },
    { title: '列数', dataIndex: 'columnCount' },
    { title: '大小', dataIndex: 'size' },
    { title: '状态', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: '版本', dataIndex: 'version' },
    { title: '创建时间', dataIndex: 'createdAt', sorter: true },
    {
      title: '操作',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EyeOutlined />} onClick={function () { navigate(`/lab/datasets/${record.id}`); }}>详情</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>删除</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <ProTable
        key={refreshKey}
        columns={columns}
        fetchData={listDatasets}
        searchFields={[{ key: 'name', label: '名称' }, { key: 'type', label: '类型' }]}
        toolbar={
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setUploadOpen(true); }}>上传数据集</Button>
        }
      />
      <Modal title="上传数据集" open={uploadOpen} onCancel={function () { setUploadOpen(false); }} footer={null}>
        <Upload.Dragger
          name="file"
          action="/api/lab/datasets"
          headers={{ Authorization: `Bearer ${localStorage.getItem('token')}` }}
          onChange={function (info) {
            if (info.file.status === 'done') {
              message.success('上传成功');
              setUploadOpen(false);
              setRefreshKey(function (k) { return k + 1; });
            } else if (info.file.status === 'error') {
              message.error('上传失败');
            }
          }}
        >
          <p><UploadOutlined style={{ fontSize: 32, color: '#1890ff' }} /></p>
          <p>点击或拖拽CSV/Parquet/JSON文件上传</p>
        </Upload.Dragger>
      </Modal>
    </div>
  );
}

export default DatasetList;
