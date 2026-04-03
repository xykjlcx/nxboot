import { useState } from "react";
import { Button, Input, Upload, Popconfirm, message } from "antd";
import type { UploadProps } from "antd";
import { UploadOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useQueryClient } from "@tanstack/react-query";
import { NxTable, type NxColumn } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { usePerm } from "@/shared/hooks/usePerm";
import { useFiles, useDeleteFile } from "../api";
import { fileColumns } from "../columns";
import type { FileVO, FileQuery } from "../types";

const initialFilter: FileQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function FileList() {
  const [query, setQuery] = useState<FileQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useFiles(query);
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

  const actionColumn: NxColumn<FileVO> = {
    field: "_action",
    title: "操作",
    width: 100,
    render: (_, record) => {
      if (!has("system:file:delete")) return null;
      return (
        <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.id)}>
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      );
    },
  };

  const columns: NxColumn<FileVO>[] = [...fileColumns, actionColumn];

  return (
    <>
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

      <NxBar
        left={
          has("system:file:upload") && (
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />} type="primary">上传文件</Button>
            </Upload>
          )
        }
        right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>}
      />

      <NxTable<FileVO>
        columns={columns}
        data={pageData?.list ?? []}
        loading={isLoading}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: pageData?.total ?? 0,
          onChange: (pageNum, pageSize) => setQuery((prev) => ({ ...prev, pageNum, pageSize })),
        }}
      />
    </>
  );
}
