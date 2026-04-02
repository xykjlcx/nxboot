import { Card, Form, Input, Button, message } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { post } from "@/app/request";
import { useAuth } from "@/shared/hooks/useAuth";

interface LoginForm {
  username: string;
  password: string;
}

interface LoginResult {
  token: string;
}

export default function Login() {
  const navigate = useNavigate();
  const setToken = useAuth((s) => s.setToken);
  const [form] = Form.useForm<LoginForm>();

  const handleSubmit = async (values: LoginForm) => {
    try {
      const { token } = await post<LoginResult>("/api/v1/auth/login", values);
      setToken(token);
      message.success("登录成功");
      navigate("/", { replace: true });
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
        background: "var(--color-bg-page)",
      }}
    >
      <Card title="NxBoot 管理系统" style={{ width: 400 }}>
        <Form form={form} onFinish={handleSubmit} autoComplete="off" size="large">
          <Form.Item name="username" rules={[{ required: true, message: "请输入用户名" }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: "请输入密码" }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
