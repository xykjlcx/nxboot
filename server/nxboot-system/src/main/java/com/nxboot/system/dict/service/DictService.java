package com.nxboot.system.dict.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.dict.model.DictCommand;
import com.nxboot.system.dict.model.DictDataVO;
import com.nxboot.system.dict.model.DictTypeVO;
import com.nxboot.system.dict.repository.DictRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务
 */
@Service
public class DictService {

    private final DictRepository dictRepository;

    public DictService(DictRepository dictRepository) {
        this.dictRepository = dictRepository;
    }

    // ========== 字典类型 ==========

    public PageResult<DictTypeVO> pageTypes(PageQuery query, String keyword) {
        return dictRepository.pageTypes(query.offset(), query.pageSize(), keyword);
    }

    public DictTypeVO getTypeById(Long id) {
        DictTypeVO type = dictRepository.findTypeById(id);
        AssertUtils.notNull(type, "字典类型", id);
        return type;
    }

    @Transactional
    public Long createType(DictCommand.TypeCreate cmd) {
        AssertUtils.isTrue(!dictRepository.existsByDictType(cmd.dictType()),
                ErrorCode.BIZ_DUPLICATE, "字典类型已存在: " + cmd.dictType());
        String operator = SecurityUtils.getCurrentUsername();
        return dictRepository.insertType(cmd.dictType(), cmd.dictName(), cmd.enabled(), cmd.remark(), operator);
    }

    @Transactional
    public void updateType(Long id, DictCommand.TypeUpdate cmd) {
        AssertUtils.notNull(dictRepository.findTypeById(id), "字典类型", id);
        String operator = SecurityUtils.getCurrentUsername();
        dictRepository.updateType(id, cmd.dictName(), cmd.enabled(), cmd.remark(), operator);
    }

    @Transactional
    public void deleteType(Long id) {
        AssertUtils.notNull(dictRepository.findTypeById(id), "字典类型", id);
        String operator = SecurityUtils.getCurrentUsername();
        dictRepository.softDeleteType(id, operator);
    }

    // ========== 字典数据 ==========

    @Cacheable(value = "system:dict", key = "#dictType")
    public List<DictDataVO> listDataByType(String dictType) {
        return dictRepository.findDataByType(dictType);
    }

    public DictDataVO getDataById(Long id) {
        DictDataVO data = dictRepository.findDataById(id);
        AssertUtils.notNull(data, "字典数据", id);
        return data;
    }

    @CacheEvict(value = "system:dict", allEntries = true)
    @Transactional
    public Long createData(DictCommand.DataCreate cmd) {
        String operator = SecurityUtils.getCurrentUsername();
        return dictRepository.insertData(cmd.dictType(), cmd.dictLabel(), cmd.dictValue(),
                cmd.sortOrder(), cmd.enabled(), cmd.remark(), operator);
    }

    @CacheEvict(value = "system:dict", allEntries = true)
    @Transactional
    public void updateData(Long id, DictCommand.DataUpdate cmd) {
        AssertUtils.notNull(dictRepository.findDataById(id), "字典数据", id);
        String operator = SecurityUtils.getCurrentUsername();
        dictRepository.updateData(id, cmd.dictLabel(), cmd.dictValue(),
                cmd.sortOrder(), cmd.enabled(), cmd.remark(), operator);
    }

    @CacheEvict(value = "system:dict", allEntries = true)
    @Transactional
    public void deleteData(Long id) {
        AssertUtils.notNull(dictRepository.findDataById(id), "字典数据", id);
        String operator = SecurityUtils.getCurrentUsername();
        dictRepository.softDeleteData(id, operator);
    }
}
