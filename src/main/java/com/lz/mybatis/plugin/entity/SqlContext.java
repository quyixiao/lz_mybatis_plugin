package com.lz.mybatis.plugin.entity;

import java.util.ArrayList;
import java.util.List;

public class SqlContext {


    public EntryInfo primaryEntryInfo;

    public List<EntryInfo> otherEntryInfo = new ArrayList<>();
    public List<String> asList = new ArrayList<>();

    public EntryInfo getPrimaryEntryInfo() {
        return primaryEntryInfo;
    }

    public void setPrimaryEntryInfo(EntryInfo primaryEntryInfo) {
        this.primaryEntryInfo = primaryEntryInfo;
    }

    public List<EntryInfo> getOtherEntryInfo() {
        return otherEntryInfo;
    }

    public void setOtherEntryInfo(List<EntryInfo> otherEntryInfo) {
        this.otherEntryInfo = otherEntryInfo;
    }

}
