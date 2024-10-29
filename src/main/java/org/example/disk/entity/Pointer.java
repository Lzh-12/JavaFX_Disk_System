package org.example.disk.entity;

/**
 * 文件读写指针
 */
public class Pointer {
    int dNum; //磁盘盘块号
    int bNum; //磁盘盘块内第几个字节

    public Pointer(int dNum, int bNum) {
        this.dNum = dNum;
        this.bNum = bNum;
    }

    public int getdNum() {
        return dNum;
    }

    public void setdNum(int dNum) {
        this.dNum = dNum;
    }

    public int getbNum() {
        return bNum;
    }

    public void setbNum(int bNum) {
        this.bNum = bNum;
    }
}
