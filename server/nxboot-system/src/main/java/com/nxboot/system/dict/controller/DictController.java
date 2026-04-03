package com.nxboot.system.dict.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.dict.model.DictCommand;
import com.nxboot.system.dict.model.DictDataVO;
import com.nxboot.system.dict.model.DictTypeVO;
import com.nxboot.system.dict.service.DictService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理接口
 */
@RestController
@RequestMapping("/api/v1/system/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    // ========== 字典类型 ==========

    @GetMapping("/types")
    @PreAuthorize("@perm.has('system:dict:list')")
    public R<PageResult<DictTypeVO>> pageTypes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(dictService.pageTypes(new PageQuery(pageNum, pageSize), keyword));
    }

    @GetMapping("/types/{id}")
    @PreAuthorize("@perm.has('system:dict:list')")
    public R<DictTypeVO> getType(@PathVariable Long id) {
        return R.ok(dictService.getTypeById(id));
    }

    @Log(module = "字典管理", operation = "新增字典类型")
    @PostMapping("/types")
    @PreAuthorize("@perm.has('system:dict:create')")
    public R<Long> createType(@Valid @RequestBody DictCommand.TypeCreate cmd) {
        return R.ok(dictService.createType(cmd));
    }

    @Log(module = "字典管理", operation = "修改字典类型")
    @PutMapping("/types/{id}")
    @PreAuthorize("@perm.has('system:dict:update')")
    public R<Void> updateType(@PathVariable Long id, @Valid @RequestBody DictCommand.TypeUpdate cmd) {
        dictService.updateType(id, cmd);
        return R.ok();
    }

    @Log(module = "字典管理", operation = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @PreAuthorize("@perm.has('system:dict:delete')")
    public R<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return R.ok();
    }

    // ========== 字典数据 ==========

    @GetMapping("/data/{dictType}")
    @PreAuthorize("@perm.has('system:dict:list')")
    public R<List<DictDataVO>> listData(@PathVariable String dictType) {
        return R.ok(dictService.listDataByType(dictType));
    }

    @GetMapping("/data/id/{id}")
    @PreAuthorize("@perm.has('system:dict:list')")
    public R<DictDataVO> getData(@PathVariable Long id) {
        return R.ok(dictService.getDataById(id));
    }

    @Log(module = "字典管理", operation = "新增字典数据")
    @PostMapping("/data")
    @PreAuthorize("@perm.has('system:dict:create')")
    public R<Long> createData(@Valid @RequestBody DictCommand.DataCreate cmd) {
        return R.ok(dictService.createData(cmd));
    }

    @Log(module = "字典管理", operation = "修改字典数据")
    @PutMapping("/data/{id}")
    @PreAuthorize("@perm.has('system:dict:update')")
    public R<Void> updateData(@PathVariable Long id, @Valid @RequestBody DictCommand.DataUpdate cmd) {
        dictService.updateData(id, cmd);
        return R.ok();
    }

    @Log(module = "字典管理", operation = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @PreAuthorize("@perm.has('system:dict:delete')")
    public R<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return R.ok();
    }
}
