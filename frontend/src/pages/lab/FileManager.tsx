import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Upload, message, Breadcrumb, Space, Dropdown } from 'antd';
import { UploadOutlined, FolderAddOutlined, DeleteOutlined, DownloadOutlined, CopyOutlined, ScissorOutlined, EyeOutlined } from '@ant-design/icons';
import { getFiles, uploadFile, moveFile, copyFile, deleteFile, downloadFile } from '@/services/lab';

export default function FileManagerPage() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [path, setPath] = useState('/');
  const [pathParts, setPathParts] = useState<string[]>(['/']);
  const [uploadVisible, setUploadVisible] = useState(false);
  const [folderVisible, setFolderVisible] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [previewContent, setPreviewContent] = useState<any>(null);
  const [folderForm] = Form.useForm();

  function buildPath(parts: string[]) {
    return parts.join('/').replace(/\/+/g, '/') || '/';
  }

  function fetchFiles(currentPath: string) {
    setLoading(true);
    getFiles({ path: currentPath })
      .then(function (res) {
        setFiles(res.data.data.items || res.data.data || []);
      })
      .catch(function () { message.error('Failed to load files'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchFiles(path); }, [path]);

  function navigateToFolder(folderName: string) {
    const newPath = path === '/' ? '/' + folderName : path + '/' + folderName;
    setPath(newPath);
    setPathParts(newPath.split('/').filter(Boolean));
  }

  function navigateToBreadcrumb(index: number) {
    if (index === 0) { setPath('/'); setPathParts([]); return; }
    const parts = pathParts.slice(0, index);
    setPath('/' + parts.join('/'));
    setPathParts(parts);
  }

  function handleUpload(info: any) {
    const formData = new FormData();
    formData.append('file', info.file);
    formData.append('path', path);
    uploadFile(formData)
      .then(function () {
        message.success('File uploaded');
        fetchFiles(path);
        setUploadVisible(false);
      })
      .catch(function () { message.error('Upload failed'); });
  }

  function handleCreateFolder(values: any) {
    const fullPath = path === '/' ? '/' + values.name : path + '/' + values.name;
    uploadFile({ path: fullPath, type: 'folder' })
      .then(function () {
        message.success('Folder created');
        setFolderVisible(false);
        folderForm.resetFields();
        fetchFiles(path);
      })
      .catch(function () { message.error('Create folder failed'); });
  }

  function handleDelete(name: string) {
    const fullPath = path === '/' ? '/' + name : path + '/' + name;
    deleteFile({ path: fullPath })
      .then(function () { message.success('Deleted'); fetchFiles(path); })
      .catch(function () { message.error('Delete failed'); });
  }

  function handleDownload(record: any) {
    const fullPath = path === '/' ? '/' + record.name : path + '/' + record.name;
    downloadFile({ path: fullPath })
      .then(function (res) {
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', record.name);
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch(function () { message.error('Download failed'); });
  }

  function handlePreview(record: any) {
    setPreviewContent(record);
    setPreviewVisible(true);
  }

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: function (name: string, record: any) {
        return record.type === 'folder'
          ? <a onClick={function () { navigateToFolder(name); }}>{name}/</a>
          : name;
      },
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: function (type: string) { return type === 'folder' ? 'Folder' : 'File'; },
    },
    {
      title: 'Size',
      dataIndex: 'size',
      key: 'size',
      render: function (size: number, record: any) {
        return record.type === 'folder' ? '-' : size !== undefined ? (size / 1024).toFixed(1) + ' KB' : '-';
      },
    },
    { title: 'Owner', dataIndex: 'owner', key: 'owner' },
    { title: 'Modified', dataIndex: 'modifiedAt', key: 'modifiedAt' },
    {
      title: 'Actions',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            {record.type !== 'folder' ? (
              <>
                <Button size="small" icon={<EyeOutlined />} onClick={function () { handlePreview(record); }}>Preview</Button>
                <Button size="small" icon={<DownloadOutlined />} onClick={function () { handleDownload(record); }}>Download</Button>
              </>
            ) : null}
            <Button size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.name); }}>Delete</Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Breadcrumb
          items={[
            { title: <a onClick={function () { navigateToBreadcrumb(0); }}>/</a> },
            ...pathParts.map(function (part, idx) {
              return { title: <a onClick={function () { navigateToBreadcrumb(idx + 1); }}>{part}</a> };
            }),
          ]}
        />
        <Space>
          <Button icon={<UploadOutlined />} onClick={function () { setUploadVisible(true); }}>Upload</Button>
          <Button icon={<FolderAddOutlined />} onClick={function () { setFolderVisible(true); }}>New Folder</Button>
        </Space>
      </div>
      <Table rowKey="name" columns={columns} dataSource={files} loading={loading} pagination={false} />
      <Modal title="Upload File" open={uploadVisible} onCancel={function () { setUploadVisible(false); }} footer={null}>
        <Upload.Dragger customRequest={handleUpload} showUploadList={false}>
          <p><UploadOutlined style={{ fontSize: 32 }} /></p>
          <p>Click or drag file to upload</p>
        </Upload.Dragger>
      </Modal>
      <Modal title="New Folder" open={folderVisible} onCancel={function () { setFolderVisible(false); }} onOk={function () { folderForm.submit(); }}>
        <Form form={folderForm} layout="vertical" onFinish={handleCreateFolder}>
          <Form.Item name="name" label="Folder Name" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
      <Modal title="File Preview" open={previewVisible} onCancel={function () { setPreviewVisible(false); }} footer={null} width={640}>
        {previewContent ? <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, maxHeight: 400, overflow: 'auto' }}>{previewContent.name}</pre> : null}
      </Modal>
    </div>
  );
}
