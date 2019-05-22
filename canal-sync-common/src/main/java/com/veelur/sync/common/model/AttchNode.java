package com.veelur.sync.common.model;

import java.util.List;

/**
 * @author: veelur
 * @date: 18-11-16
 * @Description: {相关描述}
 */
public class AttchNode {
    //check为true,则为字段，否则为null
    private String field;
    //check为true,则为字段值集合，否则为null
    private List<String> values;
    //是否相等
    private Boolean equal = true;
    //是否不为括号内容
    private Boolean check = true;
    //括号中的判断顺序
    private AttchNode next;
    //当前不是判断语句即check=false时cur不为空
    private AttchNode cur;
    //是否为并且关系，否则为或者
    private Boolean logicAnd;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public Boolean getEqual() {
        return equal;
    }

    public void setEqual(Boolean equal) {
        this.equal = equal;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public AttchNode getCur() {
        return cur;
    }

    public void setCur(AttchNode cur) {
        this.cur = cur;
    }

    public AttchNode getNext() {
        return next;
    }

    public void setNext(AttchNode next) {
        this.next = next;
    }

    public Boolean getLogicAnd() {
        return logicAnd;
    }

    public void setLogicAnd(Boolean logicAnd) {
        this.logicAnd = logicAnd;
    }
}
