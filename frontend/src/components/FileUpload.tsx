import { useState } from 'react';
import { Upload, message } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';

interface FileUploadProps {
  accept?: string;
  multiple?: boolean;
  action: string;
  maxFileSize?: number;
  onUploadComplete?: (fileList: unknown[]) => void;
}

function FileUpload(props: FileUploadProps) {
  const { accept, multiple, action, maxFileSize = 500, onUploadComplete } = props;
  const [fileList, setFileList] = useState<unknown[]>([]);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: multiple || false,
    action: action,
    accept: accept,
    fileList: fileList as any,
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    },
    beforeUpload: function (file) {
      const sizeMB = file.size / 1024 / 1024;
      if (sizeMB > maxFileSize) {
        message.error(`File must be smaller than ${maxFileSize}MB`);
        return Upload.LIST_IGNORE;
      }
      return true;
    },
    onChange: function (info) {
      setFileList(info.fileList);
      if (info.file.status === 'done') {
        message.success(`${info.file.name} uploaded successfully`);
        if (onUploadComplete) {
          onUploadComplete(info.fileList);
        }
      } else if (info.file.status === 'error') {
        message.error(`${info.file.name} upload failed`);
      }
    },
  };

  return (
    <Upload.Dragger {...uploadProps}>
      <p style={{ fontSize: 48, color: '#1890ff', marginBottom: 8 }}>
        <InboxOutlined />
      </p>
      <p style={{ fontSize: 16, color: 'rgba(0,0,0,0.85)' }}>
        Click or drag files to upload
      </p>
      <p style={{ color: 'rgba(0,0,0,0.45)' }}>
        Support for single or bulk upload
      </p>
    </Upload.Dragger>
  );
}

export default FileUpload;
