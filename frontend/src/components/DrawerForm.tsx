import { useState } from 'react';
import { Drawer, Form, Button, Space, message } from 'antd';

interface DrawerFormProps {
  title: string;
  open: boolean;
  onClose: () => void;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  initialValues?: Record<string, unknown>;
  width?: number;
  children: React.ReactNode;
}

function DrawerForm(props: DrawerFormProps) {
  const { title, open, onClose, onSubmit, initialValues, width = 520, children } = props;
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  function handleOpen() {
    if (initialValues) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }

  async function handleFinish(values: Record<string, unknown>) {
    setLoading(true);
    try {
      await onSubmit(values);
      message.success('Saved successfully');
      onClose();
    } catch {
      message.error('Save failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <Drawer title={title} open={open} onClose={onClose} width={width} afterOpenChange={handleOpen} destroyOnClose>
      <Form form={form} layout="vertical" onFinish={handleFinish} initialValues={initialValues}>
        {children}
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              Save
            </Button>
            <Button onClick={onClose}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Drawer>
  );
}

export default DrawerForm;
