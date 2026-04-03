import { useMemo } from "react";
import { Button, Popconfirm, message, Space } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxDrawer } from "@/shared/components/NxDrawer";
import { usePerm } from "@/shared/hooks/usePerm";
import { useDeptTree, useDeleteDept } from "../api";
import { deptColumns } from "../columns";
import { DeptForm } from "./DeptForm";
import type { DeptVO } from "../types";

/** 部门管理：树形表格 */
export default function DeptList() {
  const { data: deptTree = [], isLoading } = useDeptTree();
  const deleteDept = useDeleteDept();
  const { has } = usePerm();

  const handleDelete = async (id: string) => {
    await deleteDept.mutateAsync(id);
    message.success("删除成功");
  };

  const columns: NxColumn<DeptVO>[] = useMemo(
    () => [
      ...deptColumns,
      {
        field: "_action",
        title: "操作",
        width: 160,
        fixed: "right",
        render: (_: unknown, record: DeptVO) => (
          <NxDrawer.Trigger<DeptVO | null>>
            {({ show }) => (
              <Space>
                {has("system:dept:update") && (
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={() => show(record)}>
                    编辑
                  </Button>
                )}
                {has("system:dept:delete") && (
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
    <NxDrawer.Root<DeptVO | null>>
      <NxBar
        left={
          has("system:dept:create") && (
            <NxDrawer.Trigger<DeptVO | null>>
              {({ show }) => (
                <Button type="primary" icon={<PlusOutlined />} onClick={() => show(null)}>
                  新增部门
                </Button>
              )}
            </NxDrawer.Trigger>
          )
        }
      />

      <NxTable<DeptVO>
        columns={columns}
        data={deptTree}
        loading={isLoading}
        pagination={false}
        expandable={{ defaultExpandAllRows: true }}
      />

      <NxDrawer.Content title="部门信息" width={520}>
        <DeptForm />
      </NxDrawer.Content>
    </NxDrawer.Root>
  );
}
