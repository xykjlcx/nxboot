import { useEffect } from "react";
import { Form, Input, InputNumber, Select, Button, Space, TreeSelect, message } from "antd";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { useCreateMenu, useUpdateMenu, useMenuTree } from "../api";
import type { MenuVO, MenuCommand } from "../types";

interface TreeSelectNode {
  value: string;
  title: string;
  children?: TreeSelectNode[];
}

function toTreeSelectData(menus: MenuVO[]): TreeSelectNode[] {
  return menus.map((m) => ({
    value: m.id,
    title: m.menuName,
    children: m.children ? toTreeSelectData(m.children) : undefined,
  }));
}

/** 菜单新增/编辑表单 */
export function MenuForm() {
  const { data, hide } = NxDrawer.useContext<MenuVO | null>();
  const [form] = Form.useForm<MenuCommand.Create>();
  const createMenu = useCreateMenu();
  const updateMenu = useUpdateMenu();
  const { data: menuTree = [] } = useMenuTree();

  const isEdit = !!data?.id;

  useEffect(() => {
    if (data) {
      form.setFieldsValue({
        menuName: data.menuName,
        parentId: data.parentId || undefined,
        sortOrder: data.sortOrder,
        path: data.path,
        component: data.component,
        menuType: data.menuType,
        visible: data.visible,
        enabled: data.enabled,
        permission: data.permission,
        icon: data.icon,
      });
    } else {
      form.resetFields();
    }
  }, [data, form]);

  const handleSubmit = async (values: MenuCommand.Create) => {
    try {
      if (isEdit) {
        await updateMenu.mutateAsync({ ...values, id: data.id });
        message.success("更新成功");
      } else {
        await createMenu.mutateAsync(values);
        message.success("创建成功");
      }
      hide();
    } catch {
      // 错误已由拦截器处理
    }
  };

  const treeSelectData = toTreeSelectData(menuTree);

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="menuType" label="菜单类型" rules={[{ required: true }]} initialValue="C">
        <Select
          options={[
            { value: "M", label: "目录" },
            { value: "C", label: "菜单" },
            { value: "F", label: "按钮" },
          ]}
        />
      </Form.Item>
      <Form.Item name="menuName" label="菜单名称" rules={[{ required: true, message: "请输入菜单名称" }]}>
        <Input placeholder="请输入菜单名称" />
      </Form.Item>
      <Form.Item name="parentId" label="上级菜单">
        <TreeSelect
          treeData={treeSelectData}
          placeholder="请选择上级菜单"
          allowClear
          treeDefaultExpandAll
        />
      </Form.Item>
      <Form.Item name="sortOrder" label="排序" initialValue={0}>
        <InputNumber min={0} style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item name="path" label="路由路径">
        <Input placeholder="请输入路由路径" />
      </Form.Item>
      <Form.Item name="component" label="组件路径">
        <Input placeholder="请输入组件路径" />
      </Form.Item>
      <Form.Item name="permission" label="权限标识">
        <Input placeholder="如 system:user:list" />
      </Form.Item>
      <Form.Item name="icon" label="图标">
        <Input placeholder="请输入图标名称" />
      </Form.Item>
      <Form.Item name="visible" label="是否可见" initialValue={true}>
        <Select
          options={[
            { value: true, label: "显示" },
            { value: false, label: "隐藏" },
          ]}
        />
      </Form.Item>
      <Form.Item name="enabled" label="状态" initialValue={true}>
        <Select
          options={[
            { value: true, label: "正常" },
            { value: false, label: "停用" },
          ]}
        />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={createMenu.isPending || updateMenu.isPending}>
            {isEdit ? "更新" : "创建"}
          </Button>
          <Button onClick={hide}>取消</Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
