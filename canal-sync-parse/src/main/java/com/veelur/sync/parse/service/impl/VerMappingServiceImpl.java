package com.veelur.sync.parse.service.impl;

import com.veelur.sync.parse.service.VerElasticsearchService;
import com.veelur.sync.parse.service.VerMappingService;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.veelur.sync.common.constant.BaseConstants;
import com.veelur.sync.common.constant.MainTypeEnum;
import com.veelur.sync.common.exception.InfoNotRightException;
import com.veelur.sync.common.model.*;
import com.veelur.sync.common.model.base.DatabaseTableModel;
import com.veelur.sync.common.util.CollectionUtils;
import com.veelur.sync.common.util.DateUtils;
import com.veelur.sync.dao.MapperEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: veelur
 * @date: 18-9-21
 * @Description: {相关描述}
 */
@Service
@ConfigurationProperties(prefix = "ver.db-es")
public class VerMappingServiceImpl implements VerMappingService, InitializingBean {

    private List<ConvertModel> mappings = new ArrayList<>();

    @Autowired
    private VerElasticsearchService verElasticsearchService;

    public List<ConvertModel> getMappings() {
        return mappings;
    }

    public void setMappings(List<ConvertModel> mappings) {
        this.mappings = mappings;
    }

    private BiMap<DatabaseModel, VerIndexTypeModel> dbEsBiMapping;
    private Map<String, VerMappingServiceImpl.Converter> mysqlTypeElasticsearchTypeMapping;
    private BiMap<DatabaseTableModel, List<ConnectModel>> dbSingleMapping;

    @Override
    public DatabaseModel getDatabaseWithIndexType(String index, String type) {
        return dbEsBiMapping.inverse().get(new VerIndexTypeModel(index, type));
    }

    @Override
    public List<ConnectModel> getColumnWithData(String database, String table) {
        return dbSingleMapping.get(new VerDatabaseTableModel(database, table));
    }

    @Override
    public Object getElasticsearchTypeObject(String mysqlType, String data) {
        Optional<Map.Entry<String, VerMappingServiceImpl.Converter>> result = mysqlTypeElasticsearchTypeMapping.entrySet()
                .parallelStream().filter(entry -> mysqlType.toLowerCase().contains(entry.getKey())).findFirst();
        return (result.isPresent() ? result.get().getValue() : (VerMappingServiceImpl.Converter) data1 -> data1).convert(data);
    }

    @Override
    public Set<VerIndexTypeModel> getIndexTypeModels() {
        if (CollectionUtils.isNotEmpty(dbEsBiMapping)) {
            return dbEsBiMapping.values();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws InfoNotRightException {
        dbEsBiMapping = HashBiMap.create();
        if (CollectionUtils.isEmpty(mappings)) {
            throw new InfoNotRightException("mapping映射为空");
        }
        DatabaseModel databaseModel;
        VerIndexTypeModel verIndexTypeModel;
        List<VerDatabaseTableModel> models;
        VerDatabaseTableModel tableModel;
        ConnectModel connectModel;
        String include;
        String exclude;
        String[] split;
        String attchstr;
        dbSingleMapping = HashBiMap.create();
        Map<VerIndexTypeModel, Map<String, List<String>>> objectAndNeteds = new HashMap<>();
        for (ConvertModel model : mappings) {
            boolean havaMain = false;
            verIndexTypeModel = new VerIndexTypeModel();
            verIndexTypeModel.setIndex(model.getIndex());
            verIndexTypeModel.setType(model.getType());
            //
            Map<String, List<String>> objectAndNested = new HashMap<>();
            List<String> initObjects = new ArrayList<>();
            objectAndNested.put("object", initObjects);
            List<String> initNesteds = new ArrayList<>();
            objectAndNested.put("nested", initNesteds);
            objectAndNeteds.put(verIndexTypeModel, objectAndNested);
            //获取数据库
            databaseModel = new DatabaseModel();
            List<DbConvertModel> dbs = model.getDbs();
            models = new ArrayList<>();
            for (DbConvertModel dbConvertModel : dbs) {
                tableModel = new VerDatabaseTableModel();
                if (StringUtils.isEmpty(dbConvertModel.getDatabase())
                        || StringUtils.isEmpty(dbConvertModel.getTable())) {
                    throw new InfoNotRightException("请核实elastic索引" + model.getIndex() + "内库名和表名");
                }
                tableModel.setDatabase(dbConvertModel.getDatabase());
                tableModel.setTable(dbConvertModel.getTable());
                if (models.contains(tableModel)) {
                    throw new InfoNotRightException("同一个索引下包含相同的库表信息,database:" + dbConvertModel.getDatabase() + ",table:" + dbConvertModel.getTable());
                }
                models.add(tableModel);
                tableModel.setMain(null != dbConvertModel.getMain() ? dbConvertModel.getMain() : MainTypeEnum.MAIN.getCode());
                if (havaMain && MainTypeEnum.MAIN.getCode().equals(tableModel.getMain())) {
                    throw new InfoNotRightException("包含重复的mapping-main信息");
                }
                /*if (StringUtils.isNotBlank(dbConvertModel.getYears())) {
                    String years = dbConvertModel.getYears();
                    String[] yearList = years.split(BaseConstants.DEFAULT_SPLIT);
                    if (yearList.length != 0) {
                        tableModel.setYears(Arrays.asList(yearList));
                    }
                }*/
                if (MainTypeEnum.ONE_TO_OBJECT.getCode().equals(tableModel.getMain())) {
                    tableModel.setAddition(true);
                    tableModel.setAdditionField(StringUtils.isBlank(dbConvertModel.getListname()) ? dbConvertModel.getTable() : dbConvertModel.getListname());
                    initObjects.add(tableModel.getAdditionField());
                }
                if (MainTypeEnum.ONE_TO_MORE.getCode().equals(tableModel.getMain())) {
                    String listname = dbConvertModel.getListname();
                    String listkey = dbConvertModel.getListkey();
                    tableModel.setListname(StringUtils.isNotEmpty(listname) ? listname : tableModel.getTable());
                    tableModel.setMainKey(StringUtils.isNotEmpty(listkey) ? listkey : BaseConstants.DEFAULT_ID);
                    initNesteds.add(tableModel.getListname());
                }
                tableModel.setPkStr(StringUtils.isEmpty(dbConvertModel.getPkstr()) ? BaseConstants.DEFAULT_ID : dbConvertModel.getPkstr());
                include = dbConvertModel.getInclude();
                if (StringUtils.isNotEmpty(include)) {
                    split = include.split(BaseConstants.DEFAULT_SPLIT);
                    tableModel.setIncludeField(Arrays.asList(split));
                }
                exclude = dbConvertModel.getExclude();
                if (StringUtils.isNotEmpty(exclude)) {
                    split = exclude.split(BaseConstants.DEFAULT_SPLIT);
                    tableModel.setExcludeField(Arrays.asList(split));
                }
                if (MapperEnum.contain(dbConvertModel.getDatasource())) {
                    tableModel.setDatasource(dbConvertModel.getDatasource());
                }
                tableModel.setConvert(dbConvertModel.getConvert());
                if (MainTypeEnum.MAIN.getCode().equals(tableModel.getMain())) {
                    havaMain = true;
                    if (StringUtils.isEmpty(verIndexTypeModel.getIndex())) {
                        verIndexTypeModel.setIndex(tableModel.getDatabase());
                    }
                    if (StringUtils.isEmpty(verIndexTypeModel.getType())) {
                        verIndexTypeModel.setIndex(tableModel.getTable());
                    }
                }
                if (StringUtils.isNotBlank(dbConvertModel.getAttchstr())) {
                    //说明有附加属性
                    attchstr = dbConvertModel.getAttchstr();
                    AttchNode attchNode = new AttchNode();
                    List<String> attchKeys = new ArrayList<>();
                    parseAttchs(attchstr, attchNode, attchKeys);
                    attchKeys = attchKeys.stream().distinct().collect(Collectors.toList());
                    tableModel.setAttchKeys(attchKeys);
                    tableModel.setAttchs(attchNode.getNext());
                }
                connectModel = new ConnectModel();
                connectModel.setDbModel(tableModel);
                connectModel.setEsModel(verIndexTypeModel);
//                dbSingleMapping.put(tableModel, dadaConnectModel);
                List<ConnectModel> dadaConnectModels = dbSingleMapping
                        .computeIfAbsent(new DatabaseTableModel(tableModel.getDatabase(), tableModel.getTable()),
                                databaseTableModel -> new ArrayList<>());
                dadaConnectModels.add(connectModel);
            }
            databaseModel.setModels(models);
            dbEsBiMapping.put(databaseModel, verIndexTypeModel);
        }
        for (Map.Entry<VerIndexTypeModel, Map<String, List<String>>> entry : objectAndNeteds.entrySet()) {
            if (CollectionUtils.isNotEmpty(entry.getValue())) {
                VerIndexTypeModel key = entry.getKey();
                //初始化object字段
                verElasticsearchService.checkAndSetIndex(key.getIndex(), key.getType(), entry.getValue());
            }
        }

        mysqlTypeElasticsearchTypeMapping = Maps.newHashMap();
        mysqlTypeElasticsearchTypeMapping.put("char", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("text", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("blob", data -> data);
        mysqlTypeElasticsearchTypeMapping.put("int", Long::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("date", DateUtils::convertDate);
        mysqlTypeElasticsearchTypeMapping.put("time", DateUtils::convertDate);
        mysqlTypeElasticsearchTypeMapping.put("float", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("double", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("decimal", Double::valueOf);
        mysqlTypeElasticsearchTypeMapping.put("year", String::valueOf);
    }

    public static final Pattern COMMA_LOGIC_PATTERN = Pattern.compile("\\s*[&|]+\\s*");
    public static final Pattern COMMA_EQUAL_PATTERN = Pattern.compile("\\s*[=!]+\\s*");

    private int parseAttchs(String attchstr, AttchNode model, List<String> attchKeys) throws InfoNotRightException {
        //获取到的是外层没有逻辑语句的信息 & | !() ()
        if (StringUtils.isBlank(attchstr)) {
            model.setNext(null);
            return 0;
        }
        String[] split = COMMA_LOGIC_PATTERN.split(attchstr);
        if (split.length == 0) {
            throw new InfoNotRightException("attchs表达式错误");
        }
        AttchNode pre = null;
        AttchNode cur;
        AttchNode node;
        //获取到信息
        String str;
        int count = 0;
        int index;
        int subIndex = 0;
        boolean logicAnd;
        AttchNode first = null;
        int j;
        int total = 0;
        for (int i = 0; i < split.length; i++) {
            if (count > 0) {
                count--;
                continue;
            }
            logicAnd = true;
            str = split[i];
            index = str.indexOf(str.charAt(0));
            if (index > 0) {
                throw new InfoNotRightException("attchs表达式错误");
            }
            node = new AttchNode();
            if (str.startsWith("(") || str.startsWith("!(")) {
                if (str.startsWith("!(")) {
                    attchstr = attchstr.substring(1);
                    node.setEqual(false);
                }
                Stack<Character> characters = new Stack<>();
                for (j = 0; j < attchstr.length(); j++) {
                    if (BaseConstants.ATTCH_BRACKET_LEFT == attchstr.charAt(j)) {
                        characters.push(attchstr.charAt(j));
                    } else if (BaseConstants.ATTCH_BRACKET_RIGHT == attchstr.charAt(j)) {
                        characters.pop();
                        if (characters.isEmpty()) {
                            //说明获取到了完整的空格中内容
                            cur = new AttchNode();
                            count = parseAttchs(attchstr.substring(1, j), cur, attchKeys);
                            total += count;
                            subIndex = j + 1;
                            node.setCheck(false);
                            node.setCur(cur.getNext());
                            break;
                        }
                    }
                }
                if (!characters.isEmpty()) {
                    throw new InfoNotRightException("attchs表达式错误");
                }
            } else {
                //不是以(开头，即为判断语句
                String[] kv = COMMA_EQUAL_PATTERN.split(str);
                if (kv.length != 2) {
                    throw new InfoNotRightException("attchs表达式错误");
                }
                node.setField(kv[0]);
                String[] values = kv[1].split(",");
                if (values.length < 1) {
                    throw new InfoNotRightException("attchs表达式错误");
                }
                node.setValues(Arrays.asList(values));
                node.setEqual(!str.contains(BaseConstants.ATTCH_Str_NOT));
                subIndex = str.length();
                attchKeys.add(node.getField());
                total++;
            }
            if (i != split.length - 1) {
                index = attchstr.indexOf(split[i + 1].charAt(0));
                for (j = subIndex; j < index; j++) {
                    if ((int) BaseConstants.ATTCH_OR == (int) attchstr.charAt(j)) {
                        logicAnd = false;
                        break;
                    }
                }
                attchstr = attchstr.substring(index);
            }
            if (pre == null) {
                pre = node;
                first = node;
            } else {
                pre.setNext(node);
                pre = node;
            }
            node.setLogicAnd(logicAnd);
        }
        model.setNext(first);
        return total;
    }


    @FunctionalInterface
    private interface Converter {
        Object convert(String data);
    }
}
