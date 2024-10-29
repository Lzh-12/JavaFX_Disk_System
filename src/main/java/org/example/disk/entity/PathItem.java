package org.example.disk.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件路径节点
 */
public class PathItem {
    private String pathName; // 文件路径
    private PathItem parent; // 父节点
    private List<PathItem> children; // 子节点

    public PathItem(String name, PathItem parent){
        this.pathName = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }
    public String getPathName(){return pathName;}

    public void setPathName(String pathName){this.pathName = pathName;}

    public PathItem getParent(){return parent;}

    public void setParent(PathItem parent){this.parent = parent;}

    public boolean hasParent(){return parent != null;}

    public List<PathItem> getChildren(){return children;}

    public void setChildren(List<PathItem> children){this.children = children;}

    public void addChildren(PathItem child){this.children.add(child);}

    public void removeChildren(PathItem child){this.children.remove(child);}

    public boolean hasChild(){return !children.isEmpty();}

    @Override
    public String toString(){return "Path [pathName="+pathName+"]";}
}
