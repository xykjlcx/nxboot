package com.nxboot.system.user.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import java.time.LocalDateTime;

/**
 * 用户 Excel 导出 VO。
 * <p>
 * 与 UserVO 分离，因为导出字段、列名、列宽是独立关注点。
 */
public class UserExcelVO {
    @ExcelProperty("用户名")
    @ColumnWidth(15)
    private String username;

    @ExcelProperty("昵称")
    @ColumnWidth(15)
    private String nickname;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String status;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;

    public UserExcelVO() {}

    /** 从 UserVO 转换 */
    public static UserExcelVO from(UserVO vo) {
        UserExcelVO excel = new UserExcelVO();
        excel.username = vo.username();
        excel.nickname = vo.nickname();
        excel.email = vo.email();
        excel.phone = vo.phone();
        excel.status = vo.enabled() ? "正常" : "停用";
        excel.createTime = vo.createTime();
        return excel;
    }

    // Getters（EasyExcel 需要）
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public LocalDateTime getCreateTime() { return createTime; }
}
