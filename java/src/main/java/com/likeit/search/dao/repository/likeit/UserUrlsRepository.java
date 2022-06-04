package com.likeit.search.dao.repository.likeit;

import com.likeit.search.dao.entity.likeit.UserUrlsEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author mafeichao
 */
@Mapper
public interface UserUrlsRepository {
    @Select("select * from user_urls where flag != 1 order by id limit #{start},#{size}")
    List<UserUrlsEntity> getNF1Urls(@Param("start") int start, @Param("size") int size);

    @Select("select * from user_urls where flag = 1 order by id limit #{start},#{size}")
    List<UserUrlsEntity> getF1Urls(@Param("start") int start, @Param("size") int size);

    @Update("update user_urls set flag = #{flag} where id = #{id}")
    void updateFlag(@Param("id") long id, @Param("flag") int flag);

    @Options(useGeneratedKeys = true, keyColumn = "id")
    @Insert("insert into user_urls(uid, add_time, source, query, url, tags, summary, url_sign) " +
            " value(#{uid}, #{add_time}, #{source}, #{query}, #{url}, #{tags}, #{summary}, #{url_sign})")
    void insert(UserUrlsEntity entity);

    @Select("select * from user_urls where uid = #{uid} and url_sign = #{url_sign}")
    UserUrlsEntity getByUidUrl(@Param("uid") long uid, @Param("url_sign") String sign);

    @Update("update user_urls set source = #{source}, query = #{query}, tags = #{tags}, summary = #{summary}, update_time = #{update_time} where id = #{id}")
    void updateAttrs(UserUrlsEntity entity);
}
