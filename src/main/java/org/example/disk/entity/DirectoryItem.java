package org.example.disk.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录信息
 */
public class DirectoryItem{
    private String directoryName; // 目录名
    private char attribute; // 目录属性
    private int number; // 起始磁盘块

    private String location; // 目录的位置
    private PathItem path; // 路径

    public DirectoryItem(String directoryName, char attribute, int number, String location) {
        this.directoryName = directoryName;
        this.attribute = attribute;
        this.number = number;
        this.location = location;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public char getAttribute() {
        return attribute;
    }

    public void setAttribute(char attribute) {
        this.attribute = attribute;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    public PathItem getPath() {
        return path;
    }

    public void setPath(PathItem path) {
        this.path = path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "DirectoryItem{" +
                "directoryName='" + directoryName + '\'' +
                ", attribute=" + attribute +
                ", number=" + number +
                '}';
    }
}
