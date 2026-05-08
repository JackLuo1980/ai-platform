import { Button, DatePicker, Input, Select, Space } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import ProTable from '@/components/ProTable';
import { listAuditLogs } from '@/services/console';

const { RangePicker } = DatePicker;

function AuditLogList() {
  const columns = [
    { title: '时间', dataIndex: 'createdAt', sorter: true, width: 180 },
    { title: '用户', dataIndex: 'userName', width: 120 },
    { title: '操作', dataIndex: 'action', width: 120 },
    { title: '资源', dataIndex: 'resourceType', width: 120 },
    { title: '资源ID', dataIndex: 'resourceId', width: 200 },
    { title: '详情', dataIndex: 'detail', ellipsis: true },
    { title: 'IP', dataIndex: 'ip', width: 140 },
    { title: '状态', dataIndex: 'status', width: 100 },
  ];

  return (
    <div>
      <ProTable
        columns={columns}
        fetchData={listAuditLogs}
        searchFields={[
          { key: 'userName', label: '用户' },
          { key: 'action', label: '操作' },
        ]}
      />
    </div>
  );
}

export default AuditLogList;
