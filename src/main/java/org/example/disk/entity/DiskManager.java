package org.example.disk.entity;


import java.io.*;

public class DiskManager {
    public static Disk diskInstance;

    public static String path = "DISK.ser";

    private DiskManager(){
    }

    public static Disk getDiskInstance(){
        if(diskInstance == null){
            File file = new File(path);
            if(!file.exists()){
                // 创建
                diskInstance = new Disk();
            } else {
                try (FileInputStream fileInputStream = new FileInputStream(path);
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    diskInstance = (Disk) objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return diskInstance;
    }


    public static void saveDiskInstance(){
        try (FileOutputStream fileOutputStream = new FileOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(diskInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
