import { Row, Col, Card, Progress, Descriptions, Spin, Result, Button } from "antd";
import { useMonitorServer } from "../api";

function Monitor() {
  const { data, isLoading, isError, error, refetch } = useMonitorServer();

  if (isLoading) return <Spin tip="加载中..." style={{ display: "block", marginTop: 100 }} />;

  if (isError) {
    return (
      <Result
        status="error"
        title="加载失败"
        subTitle={error instanceof Error ? error.message : "获取服务器监控数据失败，请检查网络或权限。"}
        extra={<Button type="primary" onClick={() => refetch()}>重试</Button>}
      />
    );
  }

  if (!data) return <Result status="warning" title="暂无数据" />;

  const { cpu, memory, jvm, disk } = data;
  const usageColor = (v: number) => (v > 90 ? "#ff4d4f" : v > 70 ? "#faad14" : "#52c41a");

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} sm={12} xl={6}>
        <Card title="CPU">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={cpu.usage} strokeColor={usageColor(cpu.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="型号">{cpu.name}</Descriptions.Item>
            <Descriptions.Item label="核心数">{cpu.cores}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="内存">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={memory.usage} strokeColor={usageColor(memory.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="已用">{memory.used}</Descriptions.Item>
            <Descriptions.Item label="空闲">{memory.free}</Descriptions.Item>
            <Descriptions.Item label="总量">{memory.total}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="JVM">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={jvm.heapUsage} strokeColor={usageColor(jvm.heapUsage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="堆内存">{jvm.heapUsed} / {jvm.heapMax}</Descriptions.Item>
            <Descriptions.Item label="Java 版本">{jvm.javaVersion}</Descriptions.Item>
            <Descriptions.Item label="运行时间">{jvm.uptime}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
      <Col xs={24} sm={12} xl={6}>
        <Card title="磁盘">
          <div style={{ textAlign: "center" }}>
            <Progress type="dashboard" percent={disk.usage} strokeColor={usageColor(disk.usage)} />
          </div>
          <Descriptions column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="已用">{disk.used}</Descriptions.Item>
            <Descriptions.Item label="空闲">{disk.free}</Descriptions.Item>
            <Descriptions.Item label="总量">{disk.total}</Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>
    </Row>
  );
}

export default Monitor;
