/**
 * 移除 index.html 中的全局品牌加载页（带 fade-out 动画）。
 * 多次调用安全——已移除后不再执行。
 */
export function dismissGlobalLoading() {
  const el = document.getElementById("app-loading");
  if (!el) return;
  el.classList.add("fade-out");
  setTimeout(() => el.remove(), 300);
}
