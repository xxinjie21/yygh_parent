package com.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.yygh.model.cmn.Dict;
import com.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典Excel导入监听器（批量插入模式）
 * @author XXJ
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {

    /** 批量插入缓存列表 */
    private final List<Dict> dictList = new ArrayList<>();

    //逐行读取，攒批
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        dictList.add(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 数据由调用方在doRead()之后批量写入
    }

    /** 获取累积的Dict列表，供调用方批量插入 */
    public List<Dict> getDictList() {
        return dictList;
    }
}
