import { useEffect } from "react";
import { Form, Input, Button, Space, message } from "antd";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { useCreateConfig, useUpdateConfig } from "../api";
import type { ConfigVO, ConfigCommand } from "../types";

export function ConfigForm() {
  const { data, hide } = NxDrawer.useContext<ConfigVO | null>();
  const [form] = Form.useForm<ConfigCommand.Create>();
  const createConfig = useCreateConfig();
  const updateConfig = useUpdateConfig();

  const isEdit = !!data?.id;

  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        configName: data.configName,
        configKey: data.configKey,
        configValue: data.configValue,
        remark: data.remark,
      });
    } else {
      form.resetFields();
    }
  }, [data, form]);

  const handleSubmit = async (values: ConfigCommand.Create) => {
    try {
      if (isEdit) {
        await updateConfig.mutateAsync({ ...values, id: data.id });
        message.success("更新成功");
      } else {
        await createConfig.mutateAsync(values);
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="configName" label="配置名称" rules={[{ required: true }]}>
        <Input placeholder="请输入配置名称" />
      </Form.Item>
      <Form.Item name="configKey" label="配置键" rules={[{ required: true }]}>
        <Input placeholder="请输入配置键" />
      </Form.Item>
      <Form.Item name="configValue" label="配置值" rules={[{ required: true }]}>
        <Input.TextArea rows={3} placeholder="请输入配置值" />
      </Form.Item>
      <Form.Item name="remark" label="备注">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createConfig.isPending || updateConfig.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
