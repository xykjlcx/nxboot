# Reviews Directory

本目录用于存放红蓝演练中的 submission 和 review 交接文件。

## 目标

让每一轮变更都有明确的：

- 交卷内容
- 审查边界
- 问题记录
- 复审痕迹

这样后续可以通过 `git diff` + submission 快速还原当时的意图和证据。

## 推荐文件命名

- `YYYY-MM-DD-<topic>-submission.md`
- `YYYY-MM-DD-<topic>-codex-review.md`

示例：

- `2026-04-03-online-menu-submission.md`
- `2026-04-03-online-menu-codex-review.md`

## 使用方式

### 蓝队

完成一个最小可审查批次后：

1. 复制根目录 [REVIEW_SUBMISSION_TEMPLATE.md](../../REVIEW_SUBMISSION_TEMPLATE.md)
2. 生成一份 submission
3. 将状态标记为 `READY_FOR_REVIEW`

### 红队

收到审查请求后：

1. 读取 submission
2. 检查 `git diff`、相关文件和验证结果
3. 输出 `PASS / FIX / BLOCK`
4. 必要时将 findings 落盘为 `*-codex-review.md`

## 注意

- submission 不是汇报作文，而是审查入口
- 没有证据的完成声明无效
- 如果当前任务仍在旧流程中，允许本轮不补 submission，从下一轮开始启用
