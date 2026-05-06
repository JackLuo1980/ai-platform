import { useState } from 'react';
import { Button, List, Typography, Space, Badge, message } from 'antd';
import { MailOutlined, MailFilled, DeleteOutlined } from '@ant-design/icons';
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
      setMessages(res.data?.items || res.items || []);
      setTotal(res.data?.total || res.total || 0);
    } finally {
      setLoading(false);
    }
  }

  useState(function () {
    loadMessages();
  });

  async function handleToggleRead(item: Record<string, unknown>) {
    if (item.read) {
      await markMessageUnread(item.id as string);
    } else {
      await markMessageRead(item.id as string);
    }
    message.success('Updated');
    loadMessages();
  }

  async function handleDelete(id: string) {
    await deleteMessage(id);
    message.success('Deleted');
    loadMessages();
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button icon={<ReloadOutlined />} onClick={loadMessages}>Refresh</Button>
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
                  {item.read ? 'Unread' : 'Read'}
                </Button>,
                <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={function () { handleDelete(item.id); }}>Delete</Button>,
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
