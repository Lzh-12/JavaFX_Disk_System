package org.example.disk.entity;

import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;

/**
 * 文件信息
 */
public class FileItem {

    private String fileName; // 文件名
    private String fileType; // 文件类型名  "s"
    private char attribute; // 文件属性
    private int number; // 文件起始盘块号
    private int length; // 文件长度(长度单位为盘块)

    private String path; // 文件绝对路径

    // 构造文件
    public FileItem(String fileName, String fileType, char attribute, int number, String path) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.attribute = attribute;
        this.number = number;
        this.length = 1; // 文件长度为1个盘块
        this.path = path;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", attribute=" + attribute +
                ", number=" + number +
                ", length=" + length +
                '}';
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
