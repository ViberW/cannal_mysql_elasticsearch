package com.veelur.sync.component.canal;

import org.mybatis.spring.SqlSessionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Admin
 * @date: 2019/5/13
 * @Description: {相关描述}
 */
public class BaseDaoSupport /*implements BaseDao*/ {

    private SqlSessionTemplate sqlSessionTemplate;

    public BaseDaoSupport(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public List<Map<String, Object>> selectByPKWithPage(String database, String table,
                                                        int start, int limit, String pkStr, Object pk, String orderSign,
                                                        Object begin, Object end) {
        return sqlSessionTemplate.selectList(
                "BaseMapper.selectByPKWithPage",
                new HashMap<String, Object>() {{
                    put("databaseName", database);
                    put("tableName", table);
                    put("start", start);
                    put("limit", limit);
                    put("pkStr", pkStr);
                    put("pk", pk);
                    put("orderSign", orderSign);
                    put("begin", begin);
                    put("end", end);
                }});
    }

    public List<Map<String, Object>> selectByPKStr(String database, String table, String pkStr, List<Object> pkStrs) {
        return sqlSessionTemplate.selectList(
                "BaseMapper.selectByPKStr",
                new HashMap<String, Object>() {{
                    put("databaseName", database);
                    put("tableName", table);
                    put("pkStr", pkStr);
                    put("pkStrs", pkStrs);
                }});
    }

}
