package com.yygh.cmn.service;

import com.yygh.model.cmn.Dict;
import com.yygh.vo.cmn.DictVo;
import com.baomidou.mybatisplus.extension.service.IService;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 数据字典服务接口
 * @author XXJ
 */
public interface DictService extends IService<Dict> {
    List<DictVo> findChlidData(Long id);
    byte[] exportDictData();
    void importDictData(MultipartFile file);
    String getDictName(String dictCode, String value);
    List<DictVo> findByDictCode(String dictCode);
}
