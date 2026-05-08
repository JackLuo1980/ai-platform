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
      .catch(function () { message.error('加载文件失败'); })
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
        message.success('文件已上传');
        fetchFiles(path);
        setUploadVisible(false);
      })
      .catch(function () { message.error('上传失败'); });
  }

  function handleCreateFolder(values: any) {
    const fullPath = path === '/' ? '/' + values.name : path + '/' + values.name;
    uploadFile(new FormData())
      .then(function () {
        message.success('文件夹已创建');
        setFolderVisible(false);
        folderForm.resetFields();
        fetchFiles(path);
      })
      .catch(function () { message.error('创建文件夹失败'); });
  }

  function handleDelete(name: string) {
    const fullPath = path === '/' ? '/' + name : path + '/' + name;
    deleteFile(fullPath)
      .then(function () { message.success('已删除'); fetchFiles(path); })
      .catch(function () { message.error('删除失败'); });
  }

  function handleDownload(record: any) {
    const fullPath = path === '/' ? '/' + record.name : path + '/' + record.name;
    downloadFile(fullPath)
      .then(function (res) {
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', record.name);
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch(function () { message.error('下载失败'); });
  }

  function handlePreview(record: any) {
    setPreviewContent(record);
    setPreviewVisible(true);
  }

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: function (name: string, record: any) {
        return record.type === 'folder'
          ? <a onClick={function () { navigateToFolder(name); }}>{name}/</a>
          : name;
      },
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: function (type: string) { return type === 'folder' ? '文件夹' : '文件'; },
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      render: function (size: number, record: any) {
        return record.type === 'folder' ? '-' : size !== undefined ? (size / 1024).toFixed(1) + ' KB' : '-';
      },
    },
    { title: '所有者', dataIndex: 'owner', key: 'owner' },
    { title: '修改时间', dataIndex: 'modifiedAt', key: 'modifiedAt' },
    {
      title: '操作',
      key: 'actions',
      render: function (_: any, record: any) {
        return (
          <Space>
            {record.type !== 'folder' ? (
              <>
                <Button size="small" icon={<EyeOutlined />} onClick={function () { handlePreview(record); }}>预览</Button>
                <Button size="small" icon={<DownloadOutlined />} onClick={function () { handleDownload(record); }}>下载</Button>
              </>
            ) : null}
            <Button size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(record.name); }}>删除</Button>
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
          <Button icon={<UploadOutlined />} onClick={function () { setUploadVisible(true); }}>上传</Button>
          <Button icon={<FolderAddOutlined />} onClick={function () { setFolderVisible(true); }}>新建文件夹</Button>
        </Space>
      </div>
      <Table rowKey="name" columns={columns} dataSource={files} loading={loading} pagination={false} />
      <Modal title="上传文件" open={uploadVisible} onCancel={function () { setUploadVisible(false); }} footer={null}>
        <Upload.Dragger customRequest={handleUpload} showUploadList={false}>
          <p><UploadOutlined style={{ fontSize: 32 }} /></p>
          <p>点击或拖拽文件上传</p>
        </Upload.Dragger>
      </Modal>
      <Modal title="新建文件夹" open={folderVisible} onCancel={function () { setFolderVisible(false); }} onOk={function () { folderForm.submit(); }}>
        <Form form={folderForm} layout="vertical" onFinish={handleCreateFolder}>
          <Form.Item name="name" label="文件夹名称" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
      <Modal title="文件预览" open={previewVisible} onCancel={function () { setPreviewVisible(false); }} footer={null} width={640}>
        {previewContent ? <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, maxHeight: 400, overflow: 'auto' }}>{previewContent.name}</pre> : null}
      </Modal>
    </div>
  );
}
