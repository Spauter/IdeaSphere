package com.spauter.extra.baseentity.builder;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
//sql生成器
public class SQLBuilder {
    protected ClassFieldSearcher searcher;
    public SQLBuilder(ClassFieldSearcher searcher){
        this.searcher=searcher;
    }

    public Class<?>getDestClazz(){
        return searcher.clazz();
    }

    //预留用于批处理
    public String getInsertSql() {
        StringBuilder stringBuilder = new StringBuilder("insert into ");
        stringBuilder.append(searcher.getTableName());
        stringBuilder.append("(");
        for (String key : searcher.getFiledRelation().keySet()) {
            stringBuilder.append(searcher.getFiledRelation().get(key));
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        stringBuilder.append(" values(");
        stringBuilder.append("?,".repeat(searcher.getFiledRelation().size()));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String getFIndAllSql(){
        return "select * from "+searcher.getTableName();
    }

    public String getFindByIdSql(){
        return "select * from "+searcher.getTableName()+" where "+searcher.getTablePk()+"= ? ";
    }

    public String deleteAllSql(){
        return "truncate table "+searcher.getTableName();
    }
}
