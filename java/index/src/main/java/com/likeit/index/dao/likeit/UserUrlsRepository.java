package com.likeit.index.dao.likeit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author mafeichao
 */
@Mapper
public interface UserUrlsRepository {
    @Select("select * from user_urls where flag = 1 order by id limit #{start},#{size}")
    List<UserUrlsEntity> getUrls(@Param("start") int start, @Param("size") int size);

    @Select("select * from user_urls where url = #{url}")
    List<UserUrlsEntity> getByUrl(@Param("url") String url);
}