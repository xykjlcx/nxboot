import { useMemo } from "react";
import { Button, Tag, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useMenuTree, useDeleteMenu } from "../api";
import { MenuForm } from "./MenuForm";
import type { MenuVO } from "../types";

export default function MenuList() {
  const { data: menuTree = [], isLoading } = useMenuTree();
  const deleteMenu = useDeleteMenu();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteMenu.mutateAsync(id);
    message.success("删除成功");
  };

  const columns: NxColumn<MenuVO>[] = useMemo(
    () => [
      { field: "menuName", title: "菜单名称", width: 200 },
      { field: "icon", title: "图标", width: 80 },
      {
        field: "menuType",
        title: "类型",
        width: 80,
        render: (val: string) => {
          const map: Record<string, { color: string; text: string }> = {
            M: { color: "blue", text: "目录" },
            C: { color: "green", text: "菜单" },
            F: { color: "orange", text: "按钮" },
          };
          const item = map[val];
          return item ? <Tag color={item.color}>{item.text}</Tag> : val;
        },
      },
      { field: "sortOrder", title: "排序", width: 70 },
      { field: "path", title: "路由路径", width: 180 },
      { field: "permission", title: "权限标识", width: 180 },
      {
        field: "enabled",
        title: "状态",
        width: 80,
        render: (val: boolean) => (
          <Tag color={val ? "success" : "error"}>{val ? "正常" : "停用"}</Tag>
        ),
      },
      {
        field: "_action",
        title: "操作",
        width: 160,
        fixed: "right",
        render: (_: unknown, record: MenuVO) => (
          <NxDrawer.Trigger<MenuVO | null>>
            {({ show }) => (
              <Space>
                {has("system:menu:update") && (
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                    编辑
                  </Button>
                )}
                {has("system:menu:delete") && (
                  <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.id)}>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                      删除
                    </Button>
                  </Popconfirm>
                )}
              </Space>
            )}
          </NxDrawer.Trigger>
        ),
      },
    ],
    [],
  );

  return (
    <NxDrawer.Root<MenuVO | null>>
      <NxBar
        left={
          has("system:menu:create") && (
            <NxDrawer.Trigger<MenuVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增菜单
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
      />

      <NxTable<MenuVO>
        columns={columns}
        data={menuTree}
        loading={isLoading}
        pagination={false}
        scroll={{ x: 1000 }}
        expandable={{ defaultExpandAllRows: true }}
      />

      <NxDrawer.Content title="菜单信息" width={520}>
        <MenuForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
