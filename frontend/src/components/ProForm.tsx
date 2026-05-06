import { useRef } from 'react';
import { Form, Button, Space } from 'antd';

interface ProFormProps {
  initialValues?: Record<string, unknown>;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  onCancel?: () => void;
  children: React.ReactNode;
  loading?: boolean;
}

function ProForm(props: ProFormProps) {
  const { initialValues, onSubmit, onCancel, children, loading } = props;
  const [form] = Form.useForm();
  const formRef = useRef(form);

  async function handleFinish(values: Record<string, unknown>) {
    try {
      await onSubmit(values);
    } catch {}
  }

  return (
    <Form
      form={formRef.current}
      layout="vertical"
      initialValues={initialValues}
      onFinish={handleFinish}
    >
      {children}
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            Submit
          </Button>
          {onCancel ? (
            <Button onClick={onCancel}>Cancel</Button>
          ) : null}
        </Space>
      </Form.Item>
    </Form>
  );
}

export default ProForm;
