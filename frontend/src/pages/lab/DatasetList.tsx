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
      title: 'Confirm Delete',
      content: 'Delete this dataset?',
      onOk: async function () {
        await deleteDataset(id);
        message.success('Deleted');
        setRefreshKey(function (k) { return k + 1; });
      },
    });
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', sorter: true },
    { title: 'Type', dataIndex: 'type' },
    { title: 'Format', dataIndex: 'format' },
    { title: 'Rows', dataIndex: 'rowCount' },
    { title: 'Columns', dataIndex: 'columnCount' },
    { title: 'Size', dataIndex: 'size' },
    { title: 'Status', dataIndex: 'status', render: function (status: string) { return <StatusTag status={status} />; } },
    { title: 'Version', dataIndex: 'version' },
    { title: 'Created', dataIndex: 'createdAt', sorter: true },
    {
      title: 'Actions',
      render: function (_: unknown, record: Record<string, unknown>) {
        return (
          <Space>
            <Button type="link" size="small" icon={<EyeOutlined />} onClick={function () { navigate(`/lab/datasets/${record.id}`); }}>Detail</Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.id as string); }}>Delete</Button>
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
        searchFields={[{ key: 'name', label: 'Name' }, { key: 'type', label: 'Type' }]}
        toolbar={
          <Button type="primary" icon={<PlusOutlined />} onClick={function () { setUploadOpen(true); }}>Upload Dataset</Button>
        }
      />
      <Modal title="Upload Dataset" open={uploadOpen} onCancel={function () { setUploadOpen(false); }} footer={null}>
        <Upload.Dragger
          name="file"
          action="/api/lab/datasets"
          headers={{ Authorization: `Bearer ${localStorage.getItem('token')}` }}
          onChange={function (info) {
            if (info.file.status === 'done') {
              message.success('Uploaded');
              setUploadOpen(false);
              setRefreshKey(function (k) { return k + 1; });
            } else if (info.file.status === 'error') {
              message.error('Upload failed');
            }
          }}
        >
          <p><UploadOutlined style={{ fontSize: 32, color: '#1890ff' }} /></p>
          <p>Click or drag CSV/Parquet/JSON files to upload</p>
        </Upload.Dragger>
      </Modal>
    </div>
  );
}

export default DatasetList;
