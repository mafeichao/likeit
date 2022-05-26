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
    @Insert("insert into user_urls(uid, add_time, source, query, url, tags, summary) " +
            " value(#{uid}, #{addTime}, #{source}, #{query}, #{url}, #{tags}, #{summary})")
    void insert(UserUrlsEntity entity);
}
