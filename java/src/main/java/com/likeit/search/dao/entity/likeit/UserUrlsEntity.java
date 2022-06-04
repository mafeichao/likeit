package com.likeit.search.dao.entity.likeit;

import com.likeit.search.utils.Consts;
import com.likeit.search.utils.Tools;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mafeichao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUrlsEntity {
    private Long id;
    private Long uid;
    private Date add_time;
    private String source;
    private String query;
    private String url;
    private String tags;
    private String summary;
    private Date update_time;
    private Integer flag;
    private String url_sign;

    public void setUrl(String url) {
        this.url = url;
        this.url_sign =  DigestUtils.md5Hex(this.url);
    }

    private String mergeStrs(String str, String s) {
        List<String> strs = new ArrayList<>();
        strs.addAll(Arrays.asList(str.split(Consts.STR_SPLITOR)));
        if(!strs.contains(s)) {
            strs.add(s);
        }
        return StringUtil.join(strs.stream().filter(x->!x.isEmpty()).collect(Collectors.toList()), Consts.STR_SPLITOR);
    }

    public void mergeAttr(UserUrlsEntity another) {
        //merge source
        source = mergeStrs(source, another.getSource());

        //merge query
        query = mergeStrs(query, another.getQuery());

        //merge tags
        List<String> _tags = new ArrayList<>();
        _tags.addAll(Arrays.asList(tags.split(Consts.STR_SPLITOR)));
        for(String t : another.getTags().split(Consts.STR_SPLITOR)) {
            if(!_tags.contains(t)) {
                _tags.add(t);
            }
        }
        tags = StringUtil.join(_tags.stream().filter(x->!x.isEmpty()).collect(Collectors.toList()), Consts.STR_SPLITOR);

        //merge summary
        if(summary == null || (another.summary != null && summary.length() < another.summary.length())) {
            summary = another.summary;
        }

        update_time = Tools.now();
    }
}
