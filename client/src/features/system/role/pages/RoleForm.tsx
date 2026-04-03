import { useEffect, useState } from "react";
import { Form, Input, InputNumber, Switch, Button, Space, Tree, message } from "antd";
import { useQuery } from "@tanstack/react-query";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { get } from "@/app/request";
import { useCreateRole, useUpdateRole } from "../api";
import type { RoleVO, RoleCommand } from "../types";
import type { MenuVO } from "../../menu/types";

interface MenuTreeNode {
  key: string;
  title: string;
  children?: MenuTreeNode[];
}

/** 将菜单列表转为 Tree 数据 */
function toTreeData(menus: MenuVO[]): MenuTreeNode[] {
  return menus.map((m) => ({
    key: m.id,
    title: m.menuName,
    children: m.children ? toTreeData(m.children) : undefined,
  }));
}

/**
 * 角色新增/编辑表单（含菜单权限树选择）
 *
 * 使用 checkStrictly 模式保证编辑保真：
 * - 回显时精确还原角色真实持有的 menuIds，不做叶子过滤
 * - 保存时提交用户实际勾选的 menuIds，后端自动补齐祖先节点
 * - 避免"只有 C 页面权限没有 F 按钮权限"的角色被误显示为空授权
 */
export function RoleForm() {
  const { data, hide } = NxDrawer.useContext<RoleVO | null>();
  const [form] = Form.useForm();
  const createRole = useCreateRole();
  const updateRole = useUpdateRole();
  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);

  const isEdit = !!data?.id;

  // 获取当前用户可分配的菜单树（非 admin 只能看到自己拥有的菜单）
  const { data: menuTree = [] } = useQuery({
    queryKey: ["system", "menu", "assignable"],
    queryFn: () => get<MenuVO[]>("/api/v1/system/menus/assignable"),
  });

  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        roleName: data.roleName,
        roleKey: data.roleKey,
        sortOrder: data.sortOrder,
        enabled: data.enabled,
        remark: data.remark,
      });
      // checkStrictly 模式：直接回显角色真实持有的全部 menuIds
      setCheckedKeys((data.menuIds ?? []).map(String));
    } else {
      form.resetFields();
      setCheckedKeys([]);
    }
  }, [data, form]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (isEdit) {
        const payload: RoleCommand.Update = {
          id: data.id,
          roleName: values.roleName as string,
          sortOrder: values.sortOrder as number,
          enabled: values.enabled as boolean,
          remark: values.remark as string,
          menuIds: checkedKeys,
        };
        await updateRole.mutateAsync(payload);
        message.success("更新成功");
      } else {
        const payload: RoleCommand.Create = {
          roleKey: values.roleKey as string,
          roleName: values.roleName as string,
          sortOrder: values.sortOrder as number,
          remark: values.remark as string,
          menuIds: checkedKeys,
        };
        await createRole.mutateAsync(payload);
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  const treeData = toTreeData(menuTree);

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="roleName" label="角色名称" rules={[{ required: true, message: "请输入角色名称" }]}>
        <Input placeholder="请输入角色名称" />
      </Form.Item>
      <Form.Item name="roleKey" label="权限标识" rules={[{ required: true, message: "请输入权限标识" }]}>
        <Input placeholder="请输入权限标识" disabled={isEdit} />
      </Form.Item>
      <Form.Item name="sortOrder" label="排序" initialValue={0}>
        <InputNumber min={0} style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item name="enabled" label="状态" valuePropName="checked" initialValue={true}>
        <Switch checkedChildren="正常" unCheckedChildren="停用" />
      </Form.Item>
      <Form.Item name="remark" label="备注">
        <Input.TextArea rows={3} placeholder="请输入备注" />
      </Form.Item>
      <Form.Item label="菜单权限">
        <Tree
          checkable
          checkStrictly
          treeData={treeData}
          checkedKeys={checkedKeys}
          onCheck={(checked) => {
            if (Array.isArray(checked)) {
              setCheckedKeys(checked as string[]);
            } else {
              setCheckedKeys(checked.checked as string[]);
            }
          }}
          style={{ maxHeight: 300, overflow: "auto" }}
        />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createRole.isPending || updateRole.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
