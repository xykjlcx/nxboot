import { useState, useMemo } from "react";
import { Button, Input, Upload, Popconfirm, message } from "antd";
import { UploadOutlined, DeleteOutlined } from "@ant-design/icons";
import type { UploadProps } from "antd";
import type { ColDef } from "ag-grid-community";
import { useQueryClient } from "@tanstack/react-query";
import { NxTable } from "@/shared/components/NxTable";
import { NxFilter } from "@/shared/components/NxFilter";
import { NxPagination } from "@/shared/components/NxPagination";
import { usePerm } from "@/shared/hooks/usePerm";
import { useFiles, useDeleteFile } from "../api";
import { fileColumns } from "../columns";
import type { FileVO, FileQuery } from "../types";

const initialFilter: FileQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function FileList() {
  const [query, setQuery] = useState<FileQuery>(initialFilter);
  const { data: pageData, isLoading } = useFiles(query);
  const deleteFile = useDeleteFile();
  const { has } = usePerm();
  const qc = useQueryClient();

  const handleDelete = async (id: string) => {
    await deleteFile.mutateAsync(id);
    message.success("删除成功");
  };

  const uploadProps: UploadProps = {
    name: "file",
    action: "/api/v1/system/files/upload",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token") ?? ""}`,
    },
    showUploadList: false,
    onChange(info) {
      if (info.file.status === "done") {
        message.success("上传成功");
        qc.invalidateQueries({ queryKey: ["system", "file"] });
      } else if (info.file.status === "error") {
        message.error("上传失败");
      }
    },
  };

  const actionColumn: ColDef<FileVO> = useMemo(
    () => ({
      headerName: "操作",
      width: 100,
      pinned: "right",
      sortable: false,
      resizable: false,
      cellRenderer: (params: { data: FileVO | undefined }) => {
        const record = params.data;
        if (!record) return null;
        if (!has("system:file:delete")) return null;
        return (
          <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        );
      },
    }),
    [],
  );

  const columns = useMemo(() => [...fileColumns, actionColumn], [actionColumn]);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <Input
            placeholder="文件名"
            value={values.keyword}
            onChange={(e) => onChange({ keyword: e.target.value })}
            style={{ width: 200 }}
            allowClear
          />
        )}
      </NxFilter>

      <div style={{ marginBottom: 12 }}>
        {has("system:file:create") && (
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />} type="primary">
              上传文件
            </Button>
          </Upload>
        )}
      </div>

      <NxTable<FileVO>
        storageKey="system-file"
        columnDefs={columns}
        rowData={pageData?.list ?? []}
        loading={isLoading}
        getRowId={(params) => params.data.id}
      />

      <NxPagination
        current={query.pageNum ?? 1}
        pageSize={query.pageSize ?? 20}
        total={pageData?.total ?? 0}
        onChange={(pageNum, pageSize) => setQuery((prev) => ({ ...prev, pageNum, pageSize }))}
      />
    </div>
  );
}
