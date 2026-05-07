import { Tag } from 'antd';

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  active: { color: 'green', label: '活跃' },
  inactive: { color: 'default', label: '停用' },
  running: { color: 'processing', label: '运行中' },
  stopped: { color: 'default', label: '已停止' },
  pending: { color: 'warning', label: '等待中' },
  approved: { color: 'green', label: '已审批' },
  rejected: { color: 'error', label: '已拒绝' },
  failed: { color: 'error', label: '失败' },
  success: { color: 'green', label: '成功' },
  warning: { color: 'warning', label: '警告' },
  deploying: { color: 'processing', label: '部署中' },
  completed: { color: 'green', label: '已完成' },
  draft: { color: 'default', label: '草稿' },
  error: { color: 'error', label: '错误' },
  healthy: { color: 'green', label: '健康' },
  unhealthy: { color: 'error', label: '不健康' },
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
