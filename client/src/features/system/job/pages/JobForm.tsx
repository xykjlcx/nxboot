import { useEffect } from "react";
import { Form, Input, Select, Button, Space, message } from "antd";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { useCreateJob, useUpdateJob } from "../api";
import type { JobVO, JobCommand } from "../types";

export function JobForm() {
  const { data, hide } = NxDrawer.useContext<JobVO | null>();
  const [form] = Form.useForm<JobCommand.Create>();
  const createJob = useCreateJob();
  const updateJob = useUpdateJob();

  const isEdit = !!data?.id;

  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        jobName: data.jobName,
        jobGroup: data.jobGroup,
        invokeTarget: data.invokeTarget,
        cronExpression: data.cronExpression,
        misfirePolicy: data.misfirePolicy,
        concurrent: data.concurrent,
        remark: data.remark,
      });
    } else {
      form.resetFields();
    }
  }, [data, form]);

  const handleSubmit = async (values: JobCommand.Create) => {
    try {
      if (isEdit) {
        await updateJob.mutateAsync({ ...values, id: data.id });
        message.success("更新成功");
      } else {
        await createJob.mutateAsync(values);
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="jobName" label="任务名称" rules={[{ required: true }]}>
        <Input placeholder="请输入任务名称" />
      </Form.Item>
      <Form.Item name="jobGroup" label="任务分组" initialValue="DEFAULT">
        <Input placeholder="请输入任务分组" />
      </Form.Item>
      <Form.Item name="invokeTarget" label="调用目标" rules={[{ required: true }]}>
        <Input placeholder="如 myTask.execute()" />
      </Form.Item>
      <Form.Item name="cronExpression" label="Cron 表达式" rules={[{ required: true }]}>
        <Input placeholder="如 0 0/10 * * * ?" />
      </Form.Item>
      <Form.Item name="misfirePolicy" label="执行策略" initialValue={1}>
        <Select
          options={[
            { value: 1, label: "立即执行" },
            { value: 2, label: "执行一次" },
            { value: 3, label: "放弃执行" },
          ]}
        />
      </Form.Item>
      <Form.Item name="concurrent" label="是否并发" initialValue={0}>
        <Select
          options={[
            { value: 0, label: "禁止" },
            { value: 1, label: "允许" },
          ]}
        />
      </Form.Item>
      <Form.Item name="remark" label="备注">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createJob.isPending || updateJob.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
