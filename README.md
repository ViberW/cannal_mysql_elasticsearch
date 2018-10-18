# 处理数据库中多表关联同步es处理,包括1对1,1对多的对应关系
* 具体映射设置参照[mapping.yml](https://github.com/Velsson/cannal_mysql_elasticsearch/blob/master/src/main/resources/mapping.yml)文件   
>ver:   
>>db-es:  
>>>mappings:   
>>>>index: canal  #索引index,默认以主表作为database作为index  
>>>>type: canal #索引type,默认以主表表名作为type 
>>>>dbs:    
>>>>>`-` database: canals  #数据库名  
>>>>>able: canal #表名  
>>>>>include:  #包含字段,默认全部,以逗号分割    
>>>>>exclude:         #不导入字段,以逗号分隔  
>>>>>pkstr: id      #作为索引的id的对应字段    
>>>>>main: 1          #表类型 1-主表 2-1对1表 3-1对n表    
>>>>>listkv: 'listA:id' #1对n时转化成list结构时的名称listA及对应主键 默认为表名称:id  
>>>>>convert: {fieldA:FIELD1,fieldB:FIELD2}  #需要转换的字段 字段fieldA转换为FIELD1,默认输入{}  


* 在处理全量同步时参考[实体](https://github.com/Velsson/cannal_mysql_elasticsearch/blob/master/src/main/java/com/veelur/sync/elasticsearch/model/request/SyncByIndexRequest.java)
* wiki在编写中...
* 在starcwang/canal_mysql_elasticsearch_sync [传送门](https://github.com/starcwang/canal_mysql_elasticsearch_sync)之上开发完成;
