import { useEffect, type ReactNode } from "react";
import {
  Form,
  Input,
  InputNumber,
  Select,
  Switch,
  Radio,
  Checkbox,
  DatePicker,
  Button,
  Space,
  Row,
  Col,
} from "antd";
import type { NxFormField, NxFormProps } from "./types";

export type { NxFormField, NxFormProps } from "./types";

/** 根据字段配置渲染对应的 antd 组件 */
function renderComponent(field: NxFormField): ReactNode {
  const { placeholder, disabled, componentProps } = field;
  const baseProps = { disabled, ...componentProps };

  switch (field.component) {
    case "input":
      return <Input placeholder={placeholder} {...baseProps} />;
    case "password":
      return <Input.Password placeholder={placeholder} {...baseProps} />;
    case "textarea":
      return <Input.TextArea placeholder={placeholder} {...baseProps} />;
    case "number":
      return <InputNumber style={{ width: "100%" }} placeholder={placeholder} {...baseProps} />;
    case "select":
      return <Select options={field.options} placeholder={placeholder} {...baseProps} />;
    case "switch":
      return <Switch {...baseProps} />;
    case "radio":
      return <Radio.Group options={field.options} {...baseProps} />;
    case "checkbox":
      return <Checkbox.Group options={field.options} {...baseProps} />;
    case "datePicker":
      return <DatePicker style={{ width: "100%" }} placeholder={placeholder} {...baseProps} />;
    case "dateRangePicker": {
      // RangePicker 的 placeholder 类型是 [string, string]
      const rangePlaceholder = placeholder
        ? ([placeholder, placeholder] as [string, string])
        : undefined;
      return <DatePicker.RangePicker style={{ width: "100%" }} placeholder={rangePlaceholder} {...baseProps} />;
    }
    default:
      return <Input placeholder={placeholder} {...baseProps} />;
  }
}

/** Schema 驱动的表单组件，覆盖 80% CRUD 表单场景 */
export function NxForm({
  fields,
  onSubmit,
  initialValues,
  submitText = "提交",
  onCancel,
  layout = "vertical",
  loading,
}: NxFormProps) {
  const [form] = Form.useForm();

  // 编辑模式填充 / 新增模式重置
  useEffect(() => {
    if (initialValues) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [initialValues, form]);

  // 监听表单值，支持动态隐藏字段
  const formValues = Form.useWatch([], form) ?? {};

  return (
    <Form form={form} layout={layout} onFinish={onSubmit}>
      <Row gutter={16}>
        {fields.map((field) => {
          // 处理动态隐藏
          const isHidden =
            typeof field.hidden === "function"
              ? field.hidden(formValues as Record<string, unknown>)
              : field.hidden;
          if (isHidden) return null;

          return (
            <Col key={field.name} span={field.span ?? 24}>
              <Form.Item
                name={field.name}
                label={field.label}
                rules={field.rules}
                initialValue={field.initialValue}
                valuePropName={field.component === "switch" ? "checked" : "value"}
              >
                {renderComponent(field)}
              </Form.Item>
            </Col>
          );
        })}
      </Row>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            {submitText}
          </Button>
          {onCancel && <Button onClick={onCancel}>取消</Button>}
        </Space>
      </Form.Item>
    </Form>
  );
}
