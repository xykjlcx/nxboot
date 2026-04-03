package com.nxboot.framework.excel;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导入导出工具。
 * <p>
 * 基于 EasyExcel，流式写入，低内存消耗。
 *
 * @example 导出示例（Controller 中）：
 * <pre>{@code
 * @GetMapping("/export")
 * public void export(HttpServletResponse response) throws IOException {
 *     List<UserVO> list = userService.listAll();
 *     ExcelHelper.write(response, "用户列表", UserVO.class, list);
 * }
 * }</pre>
 */
public final class ExcelHelper {

    private ExcelHelper() {
    }

    /**
     * 导出 Excel 到 HTTP 响应流。
     *
     * @param response  HTTP 响应
     * @param fileName  文件名（不含扩展名）
     * @param clazz     数据类（用 @ExcelProperty 标注列）
     * @param data      数据列表
     */
    public static <T> void write(HttpServletResponse response, String fileName,
                                  Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(fileName)
                .doWrite(data);
    }

    /**
     * 从 HTTP 请求流读取 Excel。
     *
     * @param inputStream  输入流
     * @param clazz        数据类
     * @return 解析后的数据列表
     */
    public static <T> List<T> read(java.io.InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream).head(clazz).sheet().doReadSync();
    }
}
