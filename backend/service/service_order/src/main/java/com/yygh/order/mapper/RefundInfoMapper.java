package com.yygh.order.mapper;

import com.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
/**
 * 退款信息Mapper接口
 * @author XXJ
 */
public interface RefundInfoMapper extends BaseMapper<RefundInfo> {
}
