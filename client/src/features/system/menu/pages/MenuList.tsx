import { useMemo } from "react";
import { Button, Table, Tag, Popconfirm, message, Space } from "antd";
import type { ColumnsType } from "antd/es/table";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useMenuTree, useDeleteMenu } from "../api";
import { MenuForm } from "./MenuForm";
import type { MenuVO } from "../types";

/** 菜单管理：使用 Ant Design Table 的 expandable 树形展示 */
export default function MenuList() {
  const { data: menuTree = [], isLoading } = useMenuTree();
  const deleteMenu = useDeleteMenu();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteMenu.mutateAsync(id);
    message.success("删除成功");
  };

  const columns: ColumnsType<MenuVO> = useMemo(
    () => [
      { title: "菜单名称", dataIndex: "menuName", key: "menuName", width: 200 },
      { title: "图标", dataIndex: "icon", key: "icon", width: 80 },
      {
        title: "类型",
        dataIndex: "menuType",
        key: "menuType",
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
      { title: "排序", dataIndex: "sortOrder", key: "sortOrder", width: 70 },
      { title: "路由路径", dataIndex: "path", key: "path", width: 180 },
      { title: "权限标识", dataIndex: "permission", key: "permission", width: 180 },
      {
        title: "状态",
        dataIndex: "enabled",
        key: "enabled",
        width: 80,
        render: (val: boolean) => (
          <Tag color={val ? "success" : "error"}>{val ? "正常" : "停用"}</Tag>
        ),
      },
      {
        title: "操作",
        key: "action",
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
      <div>
        <div style={{ marginBottom: 16 }}>
          {has("system:menu:create") && (
            <NxDrawer.Trigger<MenuVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增菜单
                </Button>
              )}
            </NxDrawer.Trigger>
          )}
        </div>

        <Table<MenuVO>
          columns={columns}
          dataSource={menuTree}
          rowKey="id"
          loading={isLoading}
          pagination={false}
          scroll={{ x: 1000 }}
          expandable={{ defaultExpandAllRows: true }}
        />

        <NxDrawer.Content title="菜单信息" width={520}>
          <MenuForm />
        </NxDrawer.Content>
      </div>
    </NxDrawer.Root>
  );
}
