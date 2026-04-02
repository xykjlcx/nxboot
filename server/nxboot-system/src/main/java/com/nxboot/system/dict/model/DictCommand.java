package com.nxboot.system.dict.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 字典操作命令
 */
public final class DictCommand {

    private DictCommand() {
    }

    public record TypeCreate(
            @NotBlank(message = "字典类型不能为空")
            String dictType,

            @NotBlank(message = "字典名称不能为空")
            String dictName,

            Integer enabled,
            String remark
    ) {
    }

    public record TypeUpdate(
            String dictName,
            Integer enabled,
            String remark
    ) {
    }

    public record DataCreate(
            @NotBlank(message = "字典类型不能为空")
            String dictType,

            @NotBlank(message = "字典标签不能为空")
            String dictLabel,

            @NotBlank(message = "字典值不能为空")
            String dictValue,

            Integer sortOrder,
            Integer enabled,
            String remark
    ) {
    }

    public record DataUpdate(
            String dictLabel,
            String dictValue,
            Integer sortOrder,
            Integer enabled,
            String remark
    ) {
    }
}
