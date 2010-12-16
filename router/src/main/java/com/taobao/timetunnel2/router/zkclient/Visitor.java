package com.taobao.timetunnel2.router.zkclient;

import java.util.List;

public interface Visitor {
    void onNodeChildrenChanged(String path, List<String> children);

    void onNodeCreated(String path);

    void onNodeDataChanged(String path);

    void onNodeDeleted(String path);
}
