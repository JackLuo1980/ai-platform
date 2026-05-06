import { Tag } from 'antd';

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  active: { color: 'green', label: 'Active' },
  inactive: { color: 'default', label: 'Inactive' },
  running: { color: 'processing', label: 'Running' },
  stopped: { color: 'default', label: 'Stopped' },
  pending: { color: 'warning', label: 'Pending' },
  approved: { color: 'green', label: 'Approved' },
  rejected: { color: 'error', label: 'Rejected' },
  failed: { color: 'error', label: 'Failed' },
  success: { color: 'green', label: 'Success' },
  warning: { color: 'warning', label: 'Warning' },
  deploying: { color: 'processing', label: 'Deploying' },
  completed: { color: 'green', label: 'Completed' },
  draft: { color: 'default', label: 'Draft' },
  error: { color: 'error', label: 'Error' },
  healthy: { color: 'green', label: 'Healthy' },
  unhealthy: { color: 'error', label: 'Unhealthy' },
};

interface StatusTagProps {
  status: string;
  customConfig?: Record<string, { color: string; label: string }>;
}

function StatusTag(props: StatusTagProps) {
  const { status, customConfig } = props;
  const config = { ...STATUS_CONFIG, ...customConfig };
  const matched = config[status];
  return (
    <Tag color={matched ? matched.color : 'default'}>
      {matched ? matched.label : status}
    </Tag>
  );
}

export default StatusTag;
