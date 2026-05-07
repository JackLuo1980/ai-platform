import { useState, useEffect } from 'react';
import { Button, List, Typography, Space, Badge, message } from 'antd';
import { MailOutlined, MailFilled, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { listMessages, markMessageRead, markMessageUnread, deleteMessage } from '@/services/console';

const { Text } = Typography;

function MessageList() {
  const [messages, setMessages] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);

  async function loadMessages() {
    setLoading(true);
    try {
      const res: any = await listMessages({ page: page - 1, size: 20 });
      const d = res?.data || res || {};
      setMessages(d.items || []);
      setTotal(d.total || 0);
    } catch {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  }

  useEffect(function () { loadMessages(); }, [page]);

  async function handleToggleRead(item: Record<string, unknown>) {
    try {
      if (item.read) {
        await markMessageUnread(item.id as string);
      } else {
        await markMessageRead(item.id as string);
      }
      message.success('已更新');
      loadMessages();
    } catch {
      message.error('操作失败');
    }
  }

  async function handleDelete(id: string) {
    try {
      await deleteMessage(id);
      message.success('已删除');
      loadMessages();
    } catch {
      message.error('删除失败');
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button icon={<ReloadOutlined />} onClick={loadMessages}>刷新</Button>
      </div>
      <List
        loading={loading}
        dataSource={messages}
        pagination={{ current: page, total: total, pageSize: 20, onChange: setPage }}
        renderItem={function (item: any) {
          return (
            <List.Item
              actions={[
                <Button type="link" size="small" icon={item.read ? <MailOutlined /> : <MailFilled />} onClick={function () { handleToggleRead(item); }}>
                  {item.read ? '未读' : '已读'}
                </Button>,
                <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(item.id); }}>删除</Button>,
              ]}
            >
              <List.Item.Meta
                title={
                  <Space>
                    <Badge dot={!item.read} />
                    <Text strong={!item.read}>{item.title}</Text>
                  </Space>
                }
                description={item.content}
              />
              <Text type="secondary">{item.createdAt}</Text>
            </List.Item>
          );
        }}
      />
    </div>
  );
}

export default MessageList;
