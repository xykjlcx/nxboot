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

/** 收集树中所有叶子节点 key（用于 checkedKeys 回显，排除自动勾选的父节点） */
function collectLeafKeys(menus: MenuVO[], checkedSet: Set<string>): string[] {
  const result: string[] = [];
  for (const m of menus) {
    const hasChildren = m.children && m.children.length > 0;
    if (hasChildren) {
      result.push(...collectLeafKeys(m.children!, checkedSet));
    } else if (checkedSet.has(String(m.id))) {
      result.push(String(m.id));
    }
  }
  return result;
}

/** 角色新增/编辑表单（含菜单权限树选择） */
export function RoleForm() {
  const { data, hide } = NxDrawer.useContext<RoleVO | null>();
  const [form] = Form.useForm();
  const createRole = useCreateRole();
  const updateRole = useUpdateRole();
  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);
  const [halfCheckedKeys, setHalfCheckedKeys] = useState<string[]>([]);

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
      // Tree 非 checkStrictly 模式下，checkedKeys 只设叶子节点
      // 父节点会根据子节点选中状态自动半选/全选
      const checkedSet = new Set((data.menuIds ?? []).map(String));
      setCheckedKeys(collectLeafKeys(menuTree, checkedSet));
      setHalfCheckedKeys([]);
    } else {
      form.resetFields();
      setCheckedKeys([]);
      setHalfCheckedKeys([]);
    }
  }, [data, form, menuTree]);

  const handleSubmit = async (values: Record<string, unknown>) => {
    // 提交时合并 checkedKeys（叶子）+ halfCheckedKeys（半选父节点）= 完整选中列表
    const allSelectedIds = [...checkedKeys, ...halfCheckedKeys];

    try {
      if (isEdit) {
        const payload: RoleCommand.Update = {
          id: data.id,
          roleName: values.roleName as string,
          sortOrder: values.sortOrder as number,
          enabled: values.enabled as boolean,
          remark: values.remark as string,
          menuIds: allSelectedIds,
        };
        await updateRole.mutateAsync(payload);
        message.success("更新成功");
      } else {
        const payload: RoleCommand.Create = {
          roleKey: values.roleKey as string,
          roleName: values.roleName as string,
          sortOrder: values.sortOrder as number,
          remark: values.remark as string,
          menuIds: allSelectedIds,
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
          treeData={treeData}
          checkedKeys={checkedKeys}
          onCheck={(checked, info) => {
            if (Array.isArray(checked)) {
              setCheckedKeys(checked as string[]);
              setHalfCheckedKeys(info.halfCheckedKeys as string[] ?? []);
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
