package com.lz.mybatis.plugin.service;

import com.lz.mybatis.plugin.utils.t.PluginTuple;

import java.util.List;

public interface MyBatisBaomidouService {

    void init(List<PluginTuple> pluginTuples, org.apache.ibatis.session.Configuration configuration, Class type);
}
