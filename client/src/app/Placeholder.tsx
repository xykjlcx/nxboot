import { Result } from "antd";

/** 占位页面：用于尚未开发的模块 */
export default function Placeholder() {
  return (
    <Result
      status="info"
      title="功能开发中"
      subTitle="该模块正在建设中，敬请期待"
    />
  );
}
