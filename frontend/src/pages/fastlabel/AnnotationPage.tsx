import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Form, Input, Select, message, Space, Card, Tag, Descriptions } from 'antd';
import { LeftOutlined, RightOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { getItem, submitAnnotation, reviewItem } from '@/services/fastlabel';

const { TextArea } = Input;
const { Option } = Select;

export default function AnnotationPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const navigate = useNavigate();
  const [item, setItem] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [labels, setLabels] = useState<any>({});
  const [form] = Form.useForm();

  function fetchItem(index: number) {
    if (!taskId) return;
    setLoading(true);
    getItem(taskId, { index: index })
      .then(function (res) {
        const data = res.data.data;
        setItem(data);
        setTotalItems(data.total || 0);
        setCurrentIndex(index);
        setLabels({});
        form.resetFields();
      })
      .catch(function () { message.error('Failed to load item'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchItem(0); }, [taskId]);

  function handleSubmit() {
    if (!item) return;
    const values = form.getFieldsValue();
    submitAnnotation(item.id, { labels: values })
      .then(function () {
        message.success('Annotation saved');
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
        else { message.info('All items completed'); }
      })
      .catch(function () { message.error('Save failed'); });
  }

  function handleReview(action: 'approved' | 'rejected') {
    if (!item) return;
    reviewItem(item.id, { action: action })
      .then(function () {
        message.success('Review submitted: ' + action);
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
      })
      .catch(function () { message.error('Review failed'); });
  }

  function renderContent() {
    if (!item) return null;
    if (item.type === 'image') {
      return (
        <div style={{ textAlign: 'center', padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src={item.url} alt="item" style={{ maxWidth: '100%', maxHeight: 500, objectFit: 'contain' }} />
        </div>
      );
    }
    if (item.type === 'text') {
      return (
        <div style={{ padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, fontSize: 14, lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
          {item.spans ? item.spans.map(function (span: any, idx: number) {
            return <span key={idx} style={{ background: span.color || '#ffe58f', padding: '2px 4px', borderRadius: 2 }}>{span.text}</span>;
          }) : item.content}
        </div>
      );
    }
    return <div style={{ padding: 24, background: '#fafafa', borderRadius: 8 }}>Unsupported item type</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          <Button onClick={function () { navigate(-1); }}>Back</Button>
          <span>Item {currentIndex + 1} / {totalItems}</span>
        </Space>
        <Space>
          <Button icon={<LeftOutlined />} disabled={currentIndex <= 0} onClick={function () { fetchItem(currentIndex - 1); }}>Prev</Button>
          <Button icon={<RightOutlined />} disabled={currentIndex >= totalItems - 1} onClick={function () { fetchItem(currentIndex + 1); }}>Next</Button>
        </Space>
      </div>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>{renderContent()}</div>
        <div style={{ width: 360 }}>
          <Card title="Annotation" loading={loading}>
            <Form form={form} layout="vertical">
              <Form.Item name="category" label="Category">
                <Select placeholder="Select category" mode="multiple">
                  <Option value="positive">Positive</Option>
                  <Option value="negative">Negative</Option>
                  <Option value="neutral">Neutral</Option>
                </Select>
              </Form.Item>
              <Form.Item name="note" label="Note">
                <TextArea rows={3} />
              </Form.Item>
            </Form>
            <Space style={{ width: '100%', justifyContent: 'center', marginTop: 16 }}>
              <Button type="primary" onClick={handleSubmit}>Submit &amp; Next</Button>
              <Button icon={<CheckOutlined />} style={{ background: '#52c41a', color: '#fff' }} onClick={function () { handleReview('approved'); }}>Approve</Button>
              <Button danger icon={<CloseOutlined />} onClick={function () { handleReview('rejected'); }}>Reject</Button>
            </Space>
          </Card>
        </div>
      </div>
    </div>
  );
}
