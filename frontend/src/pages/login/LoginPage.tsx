import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login } from '@/services/console';

function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleFinish(values: { username: string; password: string }) {
    setLoading(true);
    try {
      const res: any = await login(values);
      const token = res.data?.token || res.token;
      const user = res.data?.user || res.user || { username: values.username };
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      message.success('Login successful');
      navigate('/');
    } catch {
      message.error('Invalid username or password');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400, boxShadow: '0 2px 8px rgba(0,0,0,0.15)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <h1 style={{ fontSize: 28, fontWeight: 600, margin: 0 }}>AI Platform</h1>
          <p style={{ color: 'rgba(0,0,0,0.45)', marginTop: 8 }}>Enterprise AI/MLOps Platform</p>
        </div>
        <Form onFinish={handleFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: 'Please enter username' }]}>
            <Input prefix={<UserOutlined />} placeholder="Username" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: 'Please enter password' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Password" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Sign In
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default LoginPage;
