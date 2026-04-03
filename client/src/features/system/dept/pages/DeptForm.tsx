import { useEffect } from "react";
import { Form, Input, InputNumber, Switch, Button, Space, TreeSelect, message } from "antd";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { useCreateDept, useUpdateDept, useDeptTree } from "../api";
import type { DeptVO, DeptCommand } from "../types";

/** 部门新增/编辑表单 */
export function DeptForm() {
  const { data, hide } = NxDrawer.useContext<DeptVO | null>();
  const [form] = Form.useForm();
  const createDept = useCreateDept();
  const updateDept = useUpdateDept();
  const { data: treeData = [] } = useDeptTree();

  const isEdit = !!data?.id;

  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        parentId: data.parentId === "0" ? undefined : data.parentId,
        deptName: data.deptName,
        sortOrder: data.sortOrder,
        leader: data.leader,
        phone: data.phone,
        email: data.email,
        enabled: data.enabled,
      });
    } else {
      form.resetFields();
    }
  }, [data, form]);

  // 将树形数据转为 TreeSelect 格式
  type TreeNode = { title: string; value: string; children?: TreeNode[] };
  const toTreeData = (nodes: DeptVO[]): TreeNode[] =>
    nodes.map((n) => ({
      title: n.deptName,
      value: n.id,
      children: n.children ? toTreeData(n.children) : undefined,
    }));

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (isEdit) {
        const updateData = values as unknown as DeptCommand.Update;
        await updateDept.mutateAsync({ ...updateData, id: data.id });
        message.success("更新成功");
      } else {
        const createData = values as unknown as DeptCommand.Create;
        await createDept.mutateAsync({ ...createData, parentId: createData.parentId ?? "0" });
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="parentId" label="上级部门">
        <TreeSelect
          treeData={toTreeData(treeData)}
          placeholder="顶级部门"
          allowClear
          treeDefaultExpandAll
        />
      </Form.Item>
      <Form.Item name="deptName" label="部门名称" rules={[{ required: true, message: "请输入部门名称" }]}>
        <Input placeholder="请输入部门名称" />
      </Form.Item>
      <Form.Item name="sortOrder" label="排序" initialValue={0}>
        <InputNumber min={0} style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item name="leader" label="负责人">
        <Input placeholder="请输入负责人" />
      </Form.Item>
      <Form.Item name="phone" label="电话">
        <Input placeholder="请输入电话" />
      </Form.Item>
      <Form.Item name="email" label="邮箱" rules={[{ type: "email", message: "请输入有效邮箱" }]}>
        <Input placeholder="请输入邮箱" />
      </Form.Item>
      {isEdit && (
        <Form.Item name="enabled" label="状态" valuePropName="checked">
          <Switch checkedChildren="正常" unCheckedChildren="停用" />
        </Form.Item>
      )}
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createDept.isPending || updateDept.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
