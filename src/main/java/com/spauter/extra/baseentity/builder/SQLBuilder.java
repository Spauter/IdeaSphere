package com.spauter.extra.baseentity.builder;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
//sql生成器
public class SQLBuilder {
    protected ClassFieldSearcher searcher;
    public SQLBuilder(ClassFieldSearcher searcher){
        this.searcher=searcher;
    }

    /**
     * 生成插入sql
     * @return insert XXX(XXX,XXX) values(?,?)
     */
    public String getInsertSql() {
        var stringBuilder = new StringBuilder("insert into ");
        stringBuilder.append(searcher.getTableName());
        stringBuilder.append("(");
        for (String key : searcher.getFiledRelation().keySet()) {
            stringBuilder.append(key);
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

    /**
     * 生成查询所有sql
     * @return select * from XXX
     */
    public String getFIndAllSql(){
        return "select * from "+searcher.getTableName();
    }

    /**
     * 生成根据id查询sql
     * @return select * from XXX where id=?
     */
    public String getFindByIdSql(){
        return "select * from "+searcher.getTableName()+" where "+searcher.getTablePk()+"= ? ";
    }

    /**
     * 生成清空表sql
     */
    public String deleteAllSql(){
        return "truncate table "+searcher.getTableName();
    }
}
