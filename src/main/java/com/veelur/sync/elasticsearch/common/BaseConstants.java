package com.veelur.sync.elasticsearch.common;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
public class BaseConstants {

    public static final String USUAL_SIGN = "*";
    public static final String DEFAULT_ID = "id";
    public static final String DEFAULT_SPLIT = ",";
    public static final String DEFAULT_SPLIT_2 = ":";

    public static final String SCHEMA = "schema";
    public static final String TABLE = "table";

    public static final String INDEX = "index";
    public static final String TYPE = "type";

    public static final String EXCLUDE_FIELD = "exclude-field";
    public static final String INCLUDE_FIELD = "include-field";

    public static final String ID_KEY = "id-key";

    public static final String _PKSTR_MAIN = "1101"; //作为保存pkstr的key

    /********************类型***********************/
    public static final String TYPE_STRING = "string";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_DATE = "date";

    /********************stored_script***********************/
    public static final String SCRIPT_INSET_LIST = "data_mysql_es_insert_list";
    public static final String SCRIPT_UPDATE_LIST = "data_mysql_es_update_list";
    public static final String SCRIPT_DELETE_LIST = "data_mysql_es_delete_list";
    public static final String SCRIPT_DELETE_NULL = "data_mysql_es_delete_null";
}
