import { Button, DatePicker, Input, Select, Space } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import { listAuditLogs } from '@/services/console';

const { RangePicker } = DatePicker;

function AuditLogList() {
  const columns = [
    { title: 'Time', dataIndex: 'createdAt', sorter: true, width: 180 },
    { title: 'User', dataIndex: 'userName', width: 120 },
    { title: 'Action', dataIndex: 'action', width: 120 },
    { title: 'Resource', dataIndex: 'resourceType', width: 120 },
    { title: 'Resource ID', dataIndex: 'resourceId', width: 200 },
    { title: 'Detail', dataIndex: 'detail', ellipsis: true },
    { title: 'IP', dataIndex: 'ip', width: 140 },
    { title: 'Status', dataIndex: 'status', width: 100 },
  ];

  return (
    <div>
      <ProTable
        columns={columns}
        fetchData={listAuditLogs}
        searchFields={[
          { key: 'userName', label: 'User' },
          { key: 'action', label: 'Action' },
        ]}
      />
    </div>
  );
}

export default AuditLogList;
