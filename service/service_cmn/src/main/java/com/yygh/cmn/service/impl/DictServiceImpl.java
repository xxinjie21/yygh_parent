package com.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;
import com.yygh.cmn.listener.DictListener;
import com.yygh.cmn.mapper.DictMapper;
import com.yygh.cmn.service.DictService;
import com.yygh.model.cmn.Dict;
import com.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
/**
 * 数据字典服务实现类
 * @author XXJ
 */
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //根据数据id查询子数据列表
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChlidData(Long id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        //向list集合每个dict对象中设置hasChildren
        for (Dict dict : dictList) {
            Long dictId = dict.getId();
            boolean isChild = this.isChildren(dictId);
            dict.setHasChildren(isChild);
        }
        return dictList;
    }

    //导出数据字典，返回Excel字节数组
    @Override
    public byte[] exportDictData() {
        List<Dict> dictList = baseMapper.selectList(null);
        List<DictEeVo> dictVoList = new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo);
            dictVoList.add(dictEeVo);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            EasyExcel.write(os, DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    //导入数据字典
    @CacheEvict(value = "dict", allEntries = true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //根据dictcode和value查询
    @Override
    public String getDictName(String dictCode, String value) {
        //dictCode为空,直接根据value查询
        if (StringUtils.isEmpty(dictCode)) {
            //根据value查询
            LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dict::getValue, value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        } else {//根据dictCode和value查询
            //根据dictCode查询dict对象,得到dict的id值
            Dict codeDict = this.getDictByDictCode(dictCode);
            Long parent_id = codeDict.getId();
            //根据parent_id和value值查询
            Dict finalDict = baseMapper.selectOne(new LambdaQueryWrapper<Dict>().eq(Dict::getParentId, parent_id)
                    .eq(Dict::getValue, value));
            return finalDict.getName();
        }
    }

    //根据dictCode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //根据dictcode获取对应id
        Dict dict=this.getDictByDictCode(dictCode);
        //根据id获取子节点
        List<Dict> list = this.findChlidData(dict.getId());
        return list;
    }

    private Dict getDictByDictCode(String dictCode) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictCode, dictCode);
        Dict codeDict = baseMapper.selectOne(wrapper);
        return codeDict;
    }

    //判断id下面是否有子节点
    private boolean isChildren(Long id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        Long count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}
