package com.likeit.importer.dao.repository.likeit;

import com.likeit.importer.dao.entity.likeit.UserUrlsEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author mafeichao
 */
@Mapper
public interface UserUrlsRepository {
    @Insert("insert into user_urls(uid, add_time, source, query, url, tags, summary) " +
            " value(#{uid}, #{addTime}, #{source}, #{query}, #{url}, #{tags}, #{summary})")
    void insert(UserUrlsEntity entity);
}
