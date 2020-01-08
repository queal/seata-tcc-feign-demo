package com.fly.seata.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author: peijiepang
 * @date 2019-11-13
 * @Description:
 */
@Mapper
public interface StorageDao {

  /**
   * 扣减库存-解冻
   * @param productId
   * @param count
   * @return
   */
  @Update("update storage set used = used + #{count},forzen = forzen - #{count} where id = #{productId} and forzen > 0")
  int reduce(@Param("productId") Long productId, @Param("count") Integer count);

  /**
   * 冻结库存
   * @param productId
   * @param count
   * @return
   */
  @Update("update storage set forzen = forzen + #{count},residue = residue - #{count} where id = #{productId} and residue > 0")
  int fozen(@Param("productId") Long productId, @Param("count") Integer count);

  /**
   * 回滚库存
   * @param productId
   * @param count
   * @return
   */
  @Update("update storage set forzen = forzen - #{count},residue = residue + #{count} where id = #{productId} and forzen > 0")
  int rollback(@Param("productId") Long productId, @Param("count") Integer count);


  /**
   * 插入库存
   * @return
   */
  @Insert("INSERT INTO `storage`(`product_id`, `total`, `used`, `residue`) VALUES (1000, 1000000, 0, 1000000)")
  int insert();

  /**
   * 删除库存
   * @param id
   */
  @Delete("delete from `storage` where id = #{id}")
  void delete(Long id);
}
