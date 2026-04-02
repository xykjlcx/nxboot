import { useEffect } from "react";
import { Form, Input, Switch, Button, Space, message } from "antd";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { useCreateUser, useUpdateUser } from "../api";
import type { UserVO, UserCommand } from "../types";

/** 用户新增/编辑表单（Drawer 内） */
export function UserForm() {
  const { data, hide } = NxDrawer.useContext<UserVO | null>();
  const [form] = Form.useForm();
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();

  const isEdit = !!data?.id;

  // 编辑模式下填充表单
  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        username: data.username,
        nickname: data.nickname,
        email: data.email,
        phone: data.phone,
        remark: data.remark,
        enabled: data.enabled,
        roleIds: data.roleIds,
      });
    } else {
      form.resetFields();
    }
  }, [data, form]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (isEdit) {
        const payload: UserCommand.Update = {
          id: data.id,
          nickname: values.nickname as string,
          email: values.email as string,
          phone: values.phone as string,
          remark: values.remark as string,
          enabled: values.enabled as boolean,
          roleIds: values.roleIds as string[],
        };
        await updateUser.mutateAsync(payload);
        message.success("更新成功");
      } else {
        const payload: UserCommand.Create = {
          username: values.username as string,
          password: values.password as string,
          nickname: values.nickname as string,
          email: values.email as string,
          phone: values.phone as string,
          remark: values.remark as string,
          roleIds: values.roleIds as string[],
        };
        await createUser.mutateAsync(payload);
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="username" label="用户名" rules={[{ required: true, message: "请输入用户名" }]}>
        <Input placeholder="请输入用户名" disabled={isEdit} />
      </Form.Item>
      {!isEdit && (
        <Form.Item name="password" label="密码" rules={[{ required: true, message: "请输入密码" }]}>
          <Input.Password placeholder="请输入密码" />
        </Form.Item>
      )}
      <Form.Item name="nickname" label="昵称">
        <Input placeholder="请输入昵称" />
      </Form.Item>
      <Form.Item name="email" label="邮箱">
        <Input placeholder="请输入邮箱" />
      </Form.Item>
      <Form.Item name="phone" label="手机号">
        <Input placeholder="请输入手机号" />
      </Form.Item>
      <Form.Item name="remark" label="备注">
        <Input.TextArea rows={3} placeholder="请输入备注" />
      </Form.Item>
      <Form.Item name="enabled" label="状态" valuePropName="checked" initialValue={true}>
        <Switch checkedChildren="正常" unCheckedChildren="停用" />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createUser.isPending || updateUser.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
