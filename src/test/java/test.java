import java.util.Arrays;
import java.util.SortedMap;

public class test {
    public static void main(String[] args) {
//        int bNum = 3 - (3 / 64) * 64; // 3 - (3/64) * 64
//        System.out.println(bNum);
//
//        String path = "/test/a/t.txt";
//        int index = path.lastIndexOf('/');
//        String file = path.substring(index);
//        System.out.println(index + " " + file);

//        String path1 = "/test/t/t1";
//        System.out.println(getCorrectFileName(path1));
//        System.out.println(getCorrectDirName(path1));

//        int number = 1; // 整数 1
//        byte b = (byte) number; // 强制类型转换为 byte
//
//        String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
//        System.out.println(binaryString);
//
//
//        System.out.println("整数 1 转换为 byte: " + b);
//        System.out.println("整数 1 转换为 8位比特: " + b);
//
//        char ch = 'i';
//        b = (byte) ch;
//
//        binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
//        System.out.println(binaryString);
//
//
//        System.out.println("整数 1 转换为 byte: " + b);
//        System.out.println("整数 1 转换为 8位比特: " + b);

//        int[] array = new int[10];
//        array[0] = 5;
//        t(array);
//        System.out.println(array[0]);

//        byte bt = (byte) 3;
//        String binaryString = String.format("%8s", Integer.toBinaryString(bt & 0xFF)).replace(' ', '0');
//        System.out.println(binaryString);
//        System.out.println(String.valueOf(bt));

//        System.out.println(getFileType("~/fil.txt"));
//
//        System.out.println(isLegalDirName("~/tmp"));

//        String name = "fil.";
//        String type = name.substring(name.lastIndexOf(".")+1);
//        System.out.println(type);

//        int count = 0;
//        char[] buffer = new char[64];
//        for(int i = 0; i < 64; i++){
//            if(buffer[i] != '\u0000'){
//                count++;
//            }
//        }
//        System.out.println(count);

//        byte[] bt = new byte[10];
//        bt[1] = (byte)128;
//        bt[2] = (byte)20;
//        System.out.println((int) bt[2]);

//        char ch = '3';
//        System.out.println((byte) Character.getNumericValue(ch));
//        int number = 3;
//        System.out.println((byte)number);
//        String binaryString = String.format("%8s", Integer.toBinaryString((byte)number & 0xFF)).replace(' ', '0');
//        System.out.println(binaryString);

//        String path = "~/tmp/var/a.sh";
//// 使用String的split方法按'/'分割字符串
//        String[] parts = path.split("/");
//// 过滤掉空字符串（因为分割后的第一个元素是空的，由于路径以'~'开头）
//        String[] directories = Arrays.stream(parts)
//                .filter(part -> !part.isEmpty())
//                .toArray(String[]::new);
//
//// 打印出tmp和var目录名
//        System.out.println("Directories: " + Arrays.toString(directories));


        int[] a = new int[10];

    }

    private static boolean isLegalDirName(String name) {
        System.out.println("222" + name);
        return name.matches("^[a-zA-Z0-9[^$./]]+$");
    }

    private static boolean isLegalFileName(String name) {
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex == -1) {
            dotIndex = name.length();
        }
        name = name.substring(0, dotIndex);

        return name.matches("a-zA-Z0-9\\p{Punct}&&[^$.]+");
    }
    private static String getFileType(String name){
        return name.substring(0, name.lastIndexOf("."));
    }
    private static String getCorrectFileName(String name) {
        // 记录反斜杠最后出现的位置
        int split = name.lastIndexOf('/');

        name = name.substring(split+1); // 文件名
        return name;
    }

    private static String getCorrectDirName(String name) {
        // 记录反斜杠最后出现的位置
        int split = name.lastIndexOf('/');

        name = name.substring(0, split+1); // 父目录
        return name;
    }

    private static void t(int[] a) {
        a[1] = 0;
    }
}
