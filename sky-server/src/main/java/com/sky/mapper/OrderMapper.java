package com.sky.mapper;

import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    void update(Orders orders);
}
