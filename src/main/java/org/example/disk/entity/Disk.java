package org.example.disk.entity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 磁盘空间
 */
public class Disk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_DISK_COUNT = 128;
    private static final int MAX_DATA_SIZE = 64;

    public byte[][] bt; // 字节数组


    public Disk() {
        bt = new byte[MAX_DISK_COUNT][MAX_DATA_SIZE];
    }

    public byte[][] getBt() {
        return bt;
    }

    public void setBt(byte[][] bt) {
        this.bt = bt;
    }
}
