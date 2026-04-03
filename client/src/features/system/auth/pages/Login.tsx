import { Form, Input, Button, message } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { post } from "@/app/request";
import { useAuth } from "@/shared/hooks/useAuth";
import styles from "./Login.module.css";

interface LoginForm {
  username: string;
  password: string;
}

interface LoginResult {
  token: string;
  refreshToken: string;
}

export default function Login() {
  const navigate = useNavigate();
  const setToken = useAuth((s) => s.setToken);
  const [form] = Form.useForm<LoginForm>();

  const handleSubmit = async (values: LoginForm) => {
    try {
      const { token, refreshToken } = await post<LoginResult>("/api/v1/auth/login", values);
      setToken(token, refreshToken);
      message.success("登录成功");
      navigate("/", { replace: true });
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1 className={styles.title}>NxBoot</h1>
          <p className={styles.subtitle}>企业级管理系统脚手架</p>
        </div>
        <Form form={form} onFinish={handleSubmit} autoComplete="off" size="large">
          <Form.Item name="username" rules={[{ required: true, message: "请输入用户名" }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: "请输入密码" }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
