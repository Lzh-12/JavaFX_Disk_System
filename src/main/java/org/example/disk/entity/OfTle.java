package org.example.disk.entity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 已打开文件表
 */
public class OfTle {
    /* 实验中系统允许打开文件的最大数量 */
    public static final int OPEN_FILE_TABLE_LENGTH = 5;
    String name; // 文件绝对路径
    char attribute; // 文件的属性
    int number; // 文件起始盘块号
    int length; // 文件长度，文件占用的字节数
    int flag; // 操作类型，用“0”表示以读操作方式打开文件，用“1”表示以写操作方式打开文件
    Pointer read; //读文件的位置，文件打开时 dNum 为文件起始盘块号，bNum 为“0”
    Pointer write; //写文件的位置，文件刚建立时 dNum 为文件起始盘块号，bNum 为“0 ，打开文件时 dNum 和 bNum 为文件的末尾位置


    public OfTle(String name, char attribute, int number, int length, int flag, Pointer read, Pointer write) {
        this.name = name;
        this.attribute = attribute;
        this.number = number;
        this.length = length;
        this.flag = flag;
        this.read = read;
        this.write = write;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public Pointer getRead() {
        return read;
    }

    public void setRead(Pointer read) {
        this.read = read;
    }

    public Pointer getWrite() {
        return write;
    }

    public void setWrite(Pointer write) {
        this.write = write;
    }
}
