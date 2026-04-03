import type { Rule } from "antd/es/form";

/** 表单字段配置 */
export interface NxFormField {
  /** 字段名 */
  name: string;
  /** 标签 */
  label: string;
  /** 组件类型 */
  component:
    | "input"
    | "password"
    | "textarea"
    | "number"
    | "select"
    | "switch"
    | "radio"
    | "checkbox"
    | "datePicker"
    | "dateRangePicker";
  /** 占位文字 */
  placeholder?: string;
  /** 校验规则 */
  rules?: Rule[];
  /** 组件属性（透传给底层 antd 组件） */
  componentProps?: Record<string, unknown>;
  /** Select/Radio/Checkbox 的选项 */
  options?: { label: string; value: string | number | boolean }[];
  /** 初始值 */
  initialValue?: unknown;
  /** 是否隐藏（支持函数动态判断） */
  hidden?: boolean | ((values: Record<string, unknown>) => boolean);
  /** 是否禁用 */
  disabled?: boolean;
  /** 栅格列宽（1-24），默认 24（整行） */
  span?: number;
}

/** NxForm 组件属性 */
export interface NxFormProps {
  /** 字段配置 */
  fields: NxFormField[];
  /** 提交回调 */
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  /** 初始数据（编辑模式） */
  initialValues?: Record<string, unknown>;
  /** 提交按钮文字，默认"提交" */
  submitText?: string;
  /** 是否显示取消按钮 */
  onCancel?: () => void;
  /** 表单布局，默认 vertical */
  layout?: "horizontal" | "vertical";
  /** 提交中 loading */
  loading?: boolean;
}
