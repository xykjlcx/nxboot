import { useState } from "react";
import { Button, Input, Select } from "antd";
import { ReloadOutlined } from "@ant-design/icons";
import { NxTable } from "@/shared/components/NxTable";
import { NxBar } from "@/shared/components/NxBar";
import { NxFilter } from "@/shared/components/NxFilter";
import { useLoginLogs } from "../api";
import { loginLogColumns } from "../columns";
import type { LoginLogQuery } from "../types";

const initialFilter: LoginLogQuery = { pageNum: 1, pageSize: 20, keyword: "" };

export default function LoginLogList() {
  const [query, setQuery] = useState<LoginLogQuery>(initialFilter);
  const { data: pageData, isLoading, refetch } = useLoginLogs(query);

  return (
    <>
      <NxFilter initialValues={initialFilter} onSearch={(v) => setQuery({ ...v, pageNum: 1 })}>
        {(values, onChange) => (
          <>
            <Input
              placeholder="用户名 / IP"
              value={values.keyword}
              onChange={(e) => onChange({ keyword: e.target.value })}
              style={{ width: 200 }}
              allowClear
            />
            <Select
              placeholder="状态"
              value={values.status}
              onChange={(v) => onChange({ status: v })}
              style={{ width: 120 }}
              allowClear
              options={[
                { value: 0, label: "成功" },
                { value: 1, label: "失败" },
              ]}
            />
          </>
        )}
      </NxFilter>

      <NxBar right={<Button icon={<ReloadOutlined />} onClick={() => refetch()}>刷新</Button>} />

      <NxTable
        columns={loginLogColumns}
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
