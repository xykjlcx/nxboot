import { useState } from "react";
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  List,
  message,
  Modal,
  Popconfirm,
  Row,
  Space,
  Tag,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import {
  useDictTypes,
  useDictData,
  useCreateDictType,
  useUpdateDictType,
  useDeleteDictType,
  useCreateDictData,
  useUpdateDictData,
  useDeleteDictData,
} from "../api";
import { usePerm } from "@/shared/hooks/usePerm";
import type { DictTypeVO, DictDataVO, DictTypeCommand, DictDataCommand } from "../types";

/** 字典管理：左侧字典类型列表 + 右侧字典数据列表 */
export default function DictList() {
  const [selectedType, setSelectedType] = useState<string | null>(null);
  const { has } = usePerm();
  const { data: typesPage, isLoading: typesLoading } = useDictTypes({ pageNum: 1, pageSize: 100 });
  const { data: dictDataList = [], isLoading: dataLoading } = useDictData(selectedType);

  // 字典类型 CRUD
  const createType = useCreateDictType();
  const updateType = useUpdateDictType();
  const deleteType = useDeleteDictType();

  // 字典数据 CRUD
  const createData = useCreateDictData();
  const updateData = useUpdateDictData();
  const deleteData = useDeleteDictData();

  // 字典类型 Modal
  const [typeModalOpen, setTypeModalOpen] = useState(false);
  const [editingType, setEditingType] = useState<DictTypeVO | null>(null);
  const [typeForm] = Form.useForm<DictTypeCommand.Create>();

  // 字典数据 Modal
  const [dataModalOpen, setDataModalOpen] = useState(false);
  const [editingData, setEditingData] = useState<DictDataVO | null>(null);
  const [dataForm] = Form.useForm<DictDataCommand.Create>();

  /* ---- 字典类型操作 ---- */

  const handleOpenTypeModal = (item?: DictTypeVO) => {
    if (item) {
      setEditingType(item);
      typeForm.setFieldsValue(item);
    } else {
      setEditingType(null);
      typeForm.resetFields();
    }
    setTypeModalOpen(true);
  };

  const handleTypeSubmit = async () => {
    const values = await typeForm.validateFields();
    if (editingType) {
      await updateType.mutateAsync({ ...values, id: editingType.id });
      message.success("更新成功");
    } else {
      await createType.mutateAsync(values);
      message.success("创建成功");
    }
    setTypeModalOpen(false);
  };

  const handleDeleteType = async (id: string) => {
    await deleteType.mutateAsync(id);
    if (selectedType === id) setSelectedType(null);
    message.success("删除成功");
  };

  /* ---- 字典数据操作 ---- */

  const handleOpenDataModal = (item?: DictDataVO) => {
    if (item) {
      setEditingData(item);
      dataForm.setFieldsValue(item);
    } else {
      setEditingData(null);
      dataForm.resetFields();
      if (selectedType) {
        dataForm.setFieldValue("dictType", selectedType);
      }
    }
    setDataModalOpen(true);
  };

  const handleDataSubmit = async () => {
    const values = await dataForm.validateFields();
    if (editingData) {
      await updateData.mutateAsync({ ...values, id: editingData.id });
      message.success("更新成功");
    } else {
      await createData.mutateAsync(values);
      message.success("创建成功");
    }
    setDataModalOpen(false);
  };

  const handleDeleteData = async (id: string) => {
    await deleteData.mutateAsync(id);
    message.success("删除成功");
  };

  const types = typesPage?.list ?? [];

  return (
    <Row gutter={16} style={{ height: "100%" }}>
      {/* 左侧：字典类型列表 */}
      <Col span={8}>
        <Card
          title="字典类型"
          size="small"
          extra={
            has("system:dict:create") && (
              <Button type="link" icon={<PlusOutlined />} onClick={() => handleOpenTypeModal()}>
                新增
              </Button>
            )
          }
        >
          <List<DictTypeVO>
            loading={typesLoading}
            dataSource={types}
            renderItem={(item) => (
              <List.Item
                style={{
                  cursor: "pointer",
                  background: selectedType === item.dictType ? "var(--color-primary-bg, #e6f4ff)" : undefined,
                  padding: "8px 12px",
                }}
                onClick={() => setSelectedType(item.dictType)}
                actions={[
                  has("system:dict:update") ? (
                    <Button
                      key="edit"
                      type="link"
                      size="small"
                      icon={<EditOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenTypeModal(item);
                      }}
                    />
                  ) : null,
                  has("system:dict:delete") ? (
                    <Popconfirm
                      key="del"
                      title="确认删除?"
                      onConfirm={(e) => {
                        e?.stopPropagation();
                        handleDeleteType(item.id);
                      }}
                    >
                      <Button
                        type="link"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={(e) => e.stopPropagation()}
                      />
                    </Popconfirm>
                  ) : null,
                ].filter(Boolean)}
              >
                <List.Item.Meta title={item.dictName} description={item.dictType} />
              </List.Item>
            )}
          />
        </Card>
      </Col>

      {/* 右侧：字典数据列表 */}
      <Col span={16}>
        <Card
          title={selectedType ? `字典数据 - ${selectedType}` : "字典数据"}
          size="small"
          extra={
            selectedType && has("system:dict:create") && (
              <Button type="link" icon={<PlusOutlined />} onClick={() => handleOpenDataModal()}>
                新增
              </Button>
            )
          }
        >
          {selectedType ? (
            <List<DictDataVO>
              loading={dataLoading}
              dataSource={dictDataList}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    has("system:dict:update") ? (
                      <Button
                        key="edit"
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => handleOpenDataModal(item)}
                      />
                    ) : null,
                    has("system:dict:delete") ? (
                      <Popconfirm key="del" title="确认删除?" onConfirm={() => handleDeleteData(item.id)}>
                        <Button type="link" size="small" danger icon={<DeleteOutlined />} />
                      </Popconfirm>
                    ) : null,
                  ].filter(Boolean)}
                >
                  <List.Item.Meta
                    title={
                      <Space>
                        <span>{item.dictLabel}</span>
                        <Tag>{item.dictValue}</Tag>
                      </Space>
                    }
                    description={item.remark}
                  />
                </List.Item>
              )}
            />
          ) : (
            <div style={{ textAlign: "center", color: "var(--color-text-secondary)", padding: 48 }}>请选择左侧字典类型</div>
          )}
        </Card>
      </Col>

      {/* 字典类型 Modal */}
      <Modal
        title={editingType ? "编辑字典类型" : "新增字典类型"}
        open={typeModalOpen}
        onOk={handleTypeSubmit}
        onCancel={() => setTypeModalOpen(false)}
        confirmLoading={createType.isPending || updateType.isPending}
      >
        <Form form={typeForm} layout="vertical">
          <Form.Item name="dictName" label="字典名称" rules={[{ required: true }]}>
            <Input placeholder="请输入字典名称" />
          </Form.Item>
          <Form.Item name="dictType" label="字典类型" rules={[{ required: true }]}>
            <Input placeholder="请输入字典类型" disabled={!!editingType} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 字典数据 Modal */}
      <Modal
        title={editingData ? "编辑字典数据" : "新增字典数据"}
        open={dataModalOpen}
        onOk={handleDataSubmit}
        onCancel={() => setDataModalOpen(false)}
        confirmLoading={createData.isPending || updateData.isPending}
      >
        <Form form={dataForm} layout="vertical">
          <Form.Item name="dictType" label="字典类型" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="dictLabel" label="数据标签" rules={[{ required: true }]}>
            <Input placeholder="请输入数据标签" />
          </Form.Item>
          <Form.Item name="dictValue" label="数据键值" rules={[{ required: true }]}>
            <Input placeholder="请输入数据键值" />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序" initialValue={0}>
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </Row>
  );
}
