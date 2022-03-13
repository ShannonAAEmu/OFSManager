package Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class Utils {

    public static boolean readOFSHeader(RandomAccessFile raf) throws Exception {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        String s = new String(bytes);
        if (s.equals("OFS3")) {
            return true;
        } else {
            System.out.println("Not an OFS3 file!");
        }
        return false;
    }

    public static boolean readSecondOFSHeader(RandomAccessFile raf) throws Exception {
        byte[] header = new byte[]{16, 0, 0, 0};    // 10 00 00 00 hex
        byte[] bytes = new byte[4];
        raf.read(bytes);
        return Arrays.equals(bytes, header);
    }

    public static byte readType(RandomAccessFile raf) throws Exception {
        return raf.readByte();
    }

    public static void readUnkBytes(RandomAccessFile raf) throws Exception {
        byte[] bytes = new byte[2];      // 00 04 hex
        raf.read(bytes);
    }

    public static void readBytes(RandomAccessFile raf, byte[] bytes) throws Exception {
        raf.read(bytes);
    }

    public static void writeBytes(RandomAccessFile raf, byte[] bytes) throws Exception {
        raf.write(bytes);
    }

    public static byte[] reverseBytesArray(byte[] array) {
        byte[] reverseArray = new byte[array.length];
        int j = array.length;
        for (byte b : array) {
            reverseArray[j - 1] = b;
            j = j - 1;
        }
        return reverseArray;
    }

    public static int byteArrayToInt(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(Utils.reverseBytesArray(bytes));
        return wrapped.getInt();
    }

    public static long byteArrayToLong(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : reverseBytesArray(bytes)) {
            sb.append(String.format("%02x", b));
        }
        return Long.parseLong(sb.toString(), 16);
    }

    public static byte[] reverse4Bytes(byte[] bytes) {
        byte[] reverseBytes = new byte[8];
        reverseBytes[0] = bytes[6];
        reverseBytes[1] = bytes[7];
        reverseBytes[2] = bytes[4];
        reverseBytes[3] = bytes[5];
        reverseBytes[4] = bytes[2];
        reverseBytes[5] = bytes[3];
        reverseBytes[6] = bytes[0];
        reverseBytes[7] = bytes[1];
        return reverseBytes;
    }

    public static String normalizeHexLength(String hex, int size) {
        StringBuilder hexBuilder = new StringBuilder(hex);
        while (hexBuilder.toString().length() != size) {
            hexBuilder.insert(0, "0");
        }
        hex = hexBuilder.toString();
        hexBuilder.setLength(0);
        return hex.toUpperCase();
    }

    public static String hexToString(byte arg) {
        String s = Integer.toHexString(arg & 0xff).toUpperCase();
        if (1 == s.length()) {
            return "0" + s;
        } else {
            return s;
        }
    }

    public static String convertByteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(hexToString(b));
        }
        return sb.toString();
    }

    public static void writeDataToJson(File jsonFile, Object object) throws Exception {
        Files.deleteIfExists(jsonFile.toPath());
        RandomAccessFile randomAccessFile = new RandomAccessFile(jsonFile, "rw");
        Gson gson = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
        String json;
        if (!object.getClass().equals(JSONArray.class)) {
            json = gson.toJson(object);
        } else {
            json = object.toString();
        }
        randomAccessFile.write(json.getBytes());
        randomAccessFile.close();
    }

}
