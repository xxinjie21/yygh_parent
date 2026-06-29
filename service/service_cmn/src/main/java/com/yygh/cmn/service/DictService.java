package com.yygh.cmn.service;

import com.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 数据字典服务接口
 * @author XXJ
 */
public interface DictService extends IService<Dict> {
    List<Dict> findChlidData(Long id);
    //导出数据字典，返回Excel字节数组
    byte[] exportDictData();

    //导入数据字典
    void importDictData(MultipartFile file);

    //根据dictcode和value查询
    String getDictName(String dictCode, String value);

    //根据dictCode获取下级节点
    List<Dict> findByDictCode(String dictCode);
}
