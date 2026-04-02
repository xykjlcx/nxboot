import { useCallback, useEffect, useRef, type CSSProperties } from "react";
import { AgGridReact, type AgGridReactProps } from "ag-grid-react";
import type { ColDef, ColumnState } from "ag-grid-community";
import { AllCommunityModule, ModuleRegistry } from "ag-grid-community";

// 注册 AG Grid 社区模块
ModuleRegistry.registerModules([AllCommunityModule]);

interface NxTableProps<T> extends Omit<AgGridReactProps<T>, "defaultColDef"> {
  /** 用于持久化列配置的 key */
  storageKey?: string;
  /** 容器样式 */
  style?: CSSProperties;
  /** 列定义 */
  columnDefs: ColDef<T>[];
}

/** AG Grid 薄封装：列配置持久化 + 自适应高度 + 默认列设置 */
export function NxTable<T>({ storageKey, style, columnDefs, ...rest }: NxTableProps<T>) {
  const gridRef = useRef<AgGridReact<T>>(null);

  // 恢复列配置
  useEffect(() => {
    if (!storageKey) return;
    const saved = localStorage.getItem(`nx-col-${storageKey}`);
    if (saved) {
      try {
        const state = JSON.parse(saved) as ColumnState[];
        gridRef.current?.api?.applyColumnState({ state, applyOrder: true });
      } catch {
        // 忽略无效数据
      }
    }
  }, [storageKey]);

  // 保存列配置
  const handleColumnChanged = useCallback(() => {
    if (!storageKey || !gridRef.current?.api) return;
    const state = gridRef.current.api.getColumnState();
    localStorage.setItem(`nx-col-${storageKey}`, JSON.stringify(state));
  }, [storageKey]);

  const defaultColDef: ColDef<T> = {
    resizable: true,
    sortable: true,
    minWidth: 80,
  };

  return (
    <div style={{ width: "100%", height: "100%", flex: 1, ...style }}>
      <AgGridReact<T>
        ref={gridRef}
        columnDefs={columnDefs}
        defaultColDef={defaultColDef}
        onColumnResized={handleColumnChanged}
        onColumnMoved={handleColumnChanged}
        onSortChanged={handleColumnChanged}
        suppressMovableColumns={false}
        animateRows
        {...rest}
      />
    </div>
  );
}
