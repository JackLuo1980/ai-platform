import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Form, Input, Select, message, Space, Card } from 'antd';
import { LeftOutlined, RightOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { getItem, submitAnnotation, reviewItem } from '@/services/fastlabel';

function AnnotationPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const navigate = useNavigate();
  const [item, setItem] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [form] = Form.useForm();

  function fetchItem(index: number) {
    if (!taskId) return;
    setLoading(true);
    getItem(taskId, { index })
      .then(function (res: any) {
        const data = res?.data || res || {};
        setItem(data);
        setTotalItems(data.total || 0);
        setCurrentIndex(index);
        form.resetFields();
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchItem(0); }, [taskId]);

  function handleSubmit() {
    if (!item) return;
    const values = form.getFieldsValue();
    submitAnnotation(item.id as string, { labels: values })
      .then(function () {
        message.success('标注已保存');
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
        else { message.info('所有数据已完成'); }
      })
      .catch(function () { message.error('保存失败'); });
  }

  function handleReview(action: 'approved' | 'rejected') {
    if (!item) return;
    reviewItem(item.id as string, { action })
      .then(function () {
        message.success(action === 'approved' ? '已通过' : '已拒绝');
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
      })
      .catch(function () { message.error('操作失败'); });
  }

  function renderContent() {
    if (!item) return null;
    const type = item.type as string;
    if (type === 'image') {
      return (
        <div style={{ textAlign: 'center', padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src={item.url as string} alt="" style={{ maxWidth: '100%', maxHeight: 500, objectFit: 'contain' }} />
        </div>
      );
    }
    if (type === 'text') {
      return (
        <div style={{ padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, fontSize: 14, lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
          {(item.content as string) || ''}
        </div>
      );
    }
    return <div style={{ padding: 24, background: '#fafafa', borderRadius: 8 }}>不支持的数据类型</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          <Button onClick={function () { navigate(-1); }}>返回</Button>
          <span>第 {currentIndex + 1} / {totalItems} 条</span>
        </Space>
        <Space>
          <Button icon={<LeftOutlined />} disabled={currentIndex <= 0} onClick={function () { fetchItem(currentIndex - 1); }}>上一条</Button>
          <Button icon={<RightOutlined />} disabled={currentIndex >= totalItems - 1} onClick={function () { fetchItem(currentIndex + 1); }}>下一条</Button>
        </Space>
      </div>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>{renderContent()}</div>
        <div style={{ width: 360 }}>
          <Card title="标注" loading={loading}>
            <Form form={form} layout="vertical">
              <Form.Item name="category" label="分类">
                <Select placeholder="选择分类" mode="multiple">
                  <Select.Option value="positive">正面</Select.Option>
                  <Select.Option value="negative">负面</Select.Option>
                  <Select.Option value="neutral">中性</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item name="note" label="备注">
                <Input.TextArea rows={3} />
              </Form.Item>
            </Form>
            <Space style={{ width: '100%', justifyContent: 'center', marginTop: 16 }}>
              <Button type="primary" onClick={handleSubmit}>提交并下一条</Button>
              <Button icon={<CheckOutlined />} style={{ background: '#52c41a', color: '#fff' }} onClick={function () { handleReview('approved'); }}>通过</Button>
              <Button danger icon={<CloseOutlined />} onClick={function () { handleReview('rejected'); }}>拒绝</Button>
            </Space>
          </Card>
        </div>
      </div>
    </div>
  );
}

export default AnnotationPage;
