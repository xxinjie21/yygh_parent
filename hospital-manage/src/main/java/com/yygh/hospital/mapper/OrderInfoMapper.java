package com.yygh.hospital.mapper;

import com.yygh.hospital.model.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 订单信息Mapper接口
 *
 * @author XXJ
 */
@Mapper
@Repository
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

}
