/**
 * Blob 文件下载工具。
 *
 * @example
 * const blob = await fetch("/api/export").then(r => r.blob());
 * downloadBlob(blob, "用户列表.xlsx");
 */
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  // 延迟释放，确保浏览器已消费完 URL
  setTimeout(() => URL.revokeObjectURL(url), 200);
}
