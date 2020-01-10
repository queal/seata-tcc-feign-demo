package com.fly.seata.dao;

import com.fly.seata.dto.OrderDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * order dao
 * @author: peijiepang
 * @date 2019-11-13
 * @Description:
 */
@Mapper
public interface OrderDao {

  /**
   * 插入订单
   * @param order
   */
  @Insert({"INSERT INTO `order`(`order_no`,`user_id`, `product_id`, `count`, `money`) VALUES (#{orderNo},#{userId},#{productId},#{count},#{money})"})
  void insert(OrderDTO order);

  /**
   * 删除订单
   * @param orderNo
   */
  @Delete({"delete from `order` where order_no = #{orderNo}"})
  void delete(@Param("orderNo") String orderNo);
}
