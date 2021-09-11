package com.lz.mybatis.plugin.entity;


import com.lz.mybatis.plugin.utils.StringUtils;

import java.util.List;

public class ParameterInfo {
    private String param;

    private String column;

    private String value;

    private boolean isAnd = true;

    private boolean isOr;

    private boolean isEq = true;


    private boolean isNe;

    private boolean isGt;

    private boolean isLt;

    private boolean isGe;

    private boolean isLe;

    private boolean isLike;

    private boolean isLLike;

    private boolean isRLike;


    private boolean isIn;

    private boolean notIn;

    private boolean isEmpty;

    private boolean isNull;

    private boolean isNotEmpty;

    private boolean isOrderByIdDesc;

    private boolean isNotNull;

    private boolean isDiv;

    private boolean isSub;

    private boolean isMul;

    private boolean isPlus;

    private boolean isIF;
    private List<String> ifParams;

    private boolean ifNull;
    private List<String> ifNullParams;

    private boolean isDateFormat;
    private String dateFormatParam;
    private boolean isBy;
    private String byParam;
    private boolean isPageSize;
    private boolean isCurrPage;
    private String pageSize;
    private String currPage;
    private boolean isOrderBy;
    private String[] bys;
    private boolean isAlias;
    private boolean isAvg;
    private boolean isFrom;
    private boolean isGroupBy;
    private boolean Having;
    private boolean isLeftJoinOn;
    private boolean isMapping;
    private boolean isMax;
    private boolean isMin;
    private boolean isWhere;
    private String aliasValue;


    public String getAliasValue() {
        return aliasValue;
    }

    public void setAliasValue(String aliasValue) {
        this.aliasValue = aliasValue;
    }



    public boolean isAlias() {
        return isAlias;
    }

    public void setAlias(boolean alias) {
        isAlias = alias;
    }

    public boolean isAvg() {
        return isAvg;
    }

    public void setAvg(boolean avg) {
        isAvg = avg;
    }

    public boolean isFrom() {
        return isFrom;
    }

    public void setFrom(boolean from) {
        isFrom = from;
    }

    public boolean isGroupBy() {
        return isGroupBy;
    }

    public void setGroupBy(boolean groupBy) {
        isGroupBy = groupBy;
    }

    public boolean isHaving() {
        return Having;
    }

    public void setHaving(boolean having) {
        Having = having;
    }

    public boolean isLeftJoinOn() {
        return isLeftJoinOn;
    }

    public void setLeftJoinOn(boolean leftJoinOn) {
        isLeftJoinOn = leftJoinOn;
    }

    public boolean isMapping() {
        return isMapping;
    }

    public void setMapping(boolean mapping) {
        isMapping = mapping;
    }

    public boolean isMax() {
        return isMax;
    }

    public void setMax(boolean max) {
        isMax = max;
    }

    public boolean isMin() {
        return isMin;
    }

    public void setMin(boolean min) {
        isMin = min;
    }

    public boolean isWhere() {
        return isWhere;
    }

    public void setWhere(boolean where) {
        isWhere = where;
    }

    public boolean isDiv() {
        return isDiv;
    }

    public void setDiv(boolean div) {
        isDiv = div;
    }

    public boolean isSub() {
        return isSub;
    }

    public void setSub(boolean sub) {
        isSub = sub;
    }

    public boolean isMul() {
        return isMul;
    }

    public void setMul(boolean mul) {
        isMul = mul;
    }

    public boolean isPlus() {
        return isPlus;
    }

    public void setPlus(boolean plus) {
        isPlus = plus;
    }

    public boolean isOrderByIdDesc() {
        return isOrderByIdDesc;
    }

    public void setOrderByIdDesc(boolean orderByIdDesc) {
        isOrderByIdDesc = orderByIdDesc;
    }

    public String[] getBys() {
        return bys;
    }

    public void setBys(String[] bys) {
        this.bys = bys;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isOrderBy() {
        return isOrderBy;
    }

    public void setOrderBy(boolean orderBy) {
        isOrderBy = orderBy;
    }

    public boolean isBy() {
        return isBy;
    }

    public void setBy(boolean by) {
        isBy = by;
    }

    public String getByParam() {
        return byParam;
    }

    public void setByParam(String byParam) {
        this.byParam = byParam;
    }

    public boolean isDateFormat() {
        return isDateFormat;
    }

    public void setDateFormat(boolean dateFormat) {
        isDateFormat = dateFormat;
    }

    public String getDateFormatParam() {
        return dateFormatParam;
    }

    public void setDateFormatParam(String dateFormatParam) {
        this.dateFormatParam = dateFormatParam;
    }

    public boolean isIF() {
        return isIF;
    }

    public void setIF(boolean IF) {
        isIF = IF;
    }

    public List<String> getIfParams() {
        return ifParams;
    }

    public void setIfParams(List<String> ifParams) {
        this.ifParams = ifParams;
    }

    public boolean isIfNull() {
        return ifNull;
    }

    public void setIfNull(boolean ifNull) {
        this.ifNull = ifNull;
    }

    public List<String> getIfNullParams() {
        return ifNullParams;
    }

    public void setIfNullParams(List<String> ifNullParams) {
        this.ifNullParams = ifNullParams;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        if (StringUtils.isEmpty(column)) { //如果设置的值为空，则默认不覆盖
            return;
        }
        this.column = column;
    }

    private List<String> by;//By 注解，用于更新


    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public boolean isAnd() {
        return isAnd;
    }

    public void setAnd(boolean and) {
        isAnd = and;
    }

    public boolean isOr() {
        return isOr;
    }

    public void setOr(boolean or) {
        isOr = or;
    }

    public boolean isEq() {
        return isEq;
    }

    public void setEq(boolean eq) {
        isEq = eq;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }


    public boolean isIn() {
        return isIn;
    }

    public void setIn(boolean in) {
        isIn = in;
    }


    public boolean isNotIn() {
        return notIn;
    }

    public void setNotIn(boolean notIn) {
        this.notIn = notIn;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean aNull) {
        isNull = aNull;
    }

    public boolean isNotEmpty() {
        return isNotEmpty;
    }

    public void setNotEmpty(boolean notEmpty) {
        isNotEmpty = notEmpty;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean notNull) {
        isNotNull = notNull;
    }

    public List<String> getBy() {
        return by;
    }

    public void setBy(List<String> by) {
        this.by = by;
    }

    public boolean isNe() {
        return isNe;
    }

    public void setNe(boolean ne) {
        isNe = ne;
    }


    public boolean isGt() {
        return isGt;
    }

    public void setGt(boolean gt) {
        isGt = gt;
    }


    public boolean isLt() {
        return isLt;
    }

    public void setLt(boolean lt) {
        isLt = lt;
    }


    public boolean isGe() {
        return isGe;
    }

    public void setGe(boolean ge) {
        isGe = ge;
    }

    public boolean isLe() {
        return isLe;
    }

    public void setLe(boolean le) {
        isLe = le;
    }

    public boolean isLLike() {
        return isLLike;
    }

    public void setLLike(boolean LLike) {
        isLLike = LLike;
    }

    public boolean isRLike() {
        return isRLike;
    }

    public void setRLike(boolean RLike) {
        isRLike = RLike;
    }


    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getCurrPage() {
        return currPage;
    }

    public void setCurrPage(String currPage) {
        this.currPage = currPage;
    }

    public boolean isPageSize() {
        return isPageSize;
    }

    public void setPageSize(boolean pageSize) {
        isPageSize = pageSize;
    }

    public boolean isCurrPage() {
        return isCurrPage;
    }

    public void setCurrPage(boolean currPage) {
        isCurrPage = currPage;
    }

    @Override
    public String toString() {
        return "ParameterInfo{" +
                "param='" + param + '\'' +
                ", column='" + column + '\'' +
                ", isAnd=" + isAnd +
                ", isOr=" + isOr +
                ", isEq=" + isEq +
                ", isNe=" + isNe +
                ", isGt=" + isGt +
                ", isLt=" + isLt +
                ", isGe=" + isGe +
                ", isLe=" + isLe +
                ", isLike=" + isLike +
                ", isLLike=" + isLLike +
                ", isRLike=" + isRLike +
                ", isIn=" + isIn +
                ", notIn=" + notIn +
                ", isEmpty=" + isEmpty +
                ", isNull=" + isNull +
                ", isNotEmpty=" + isNotEmpty +
                ", isNotNull=" + isNotNull +
                ", isIF=" + isIF +
                ", ifParams=" + ifParams +
                ", ifNull=" + ifNull +
                ", ifNullParams=" + ifNullParams +
                ", isDateFormat=" + isDateFormat +
                ", dateFormatParam='" + dateFormatParam + '\'' +
                ", isBy=" + isBy +
                ", byParam='" + byParam + '\'' +
                ", by=" + by +
                '}';
    }
}
