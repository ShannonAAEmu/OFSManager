import OFSFile.Container;
import Utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {

    private static final File rootFolder = new File(System.getProperty("user.dir"));
    private static RandomAccessFile localizeMsgRAF;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 1 to export");
        System.out.println("Enter 2 to import");
        String state = scanner.next();
        if (1 == Integer.parseInt(state)) {
            File localizeMsgDat = new File(rootFolder + "\\localize_msg.dat");
            File localizeMsgJson = new File(rootFolder + "\\localize_msg.json");
            exportLocalize(localizeMsgDat, localizeMsgJson);
        } else if (2 == Integer.parseInt(state)) {
            File localizeMsgDat = new File(rootFolder + "\\localize_msg_new.dat");
            Files.deleteIfExists(localizeMsgDat.toPath());
            File localizeMsgJson = new File(rootFolder + "\\localize_msg.json");
            importLocalize(localizeMsgDat, localizeMsgJson);
        } else {
            System.out.println("Invalid input. Restart the program.");
            scanner.next();
        }
    }

    private static void exportLocalize(File localizeMsgDat, File localizeMsgJson) throws Exception {
        initRandomAccessFile(localizeMsgDat);
        if (Utils.readOFSHeader(localizeMsgRAF) && Utils.readSecondOFSHeader(localizeMsgRAF) && Utils.readType(localizeMsgRAF) == 0) {
            Utils.readUnkBytes(localizeMsgRAF);
            if (Utils.readType(localizeMsgRAF) == 0) {
                byte[] containerTotalSize = new byte[4];
                byte[] ofsFilesCount = new byte[4];
                Utils.readBytes(localizeMsgRAF, containerTotalSize);
                Utils.readBytes(localizeMsgRAF, ofsFilesCount);
                Container container = new Container(Utils.byteArrayToLong(containerTotalSize), Utils.byteArrayToInt(ofsFilesCount));
                container.exportData(localizeMsgRAF);
                Utils.writeDataToJson(localizeMsgJson, container);
            } else {
                System.out.println("Not an OFS3 container!");
            }
        } else {
            System.out.println("Not an OFS3 localize file!");
        }
        closeRAF();
    }

    private static void importLocalize(File localizeMsgDat, File localizeMsgJson) throws Exception {
        initRandomAccessFile(localizeMsgDat);
        Gson gson = new Gson();
        Files.lines(localizeMsgJson.toPath(), StandardCharsets.UTF_8);
        Reader reader = Files.newBufferedReader(localizeMsgJson.toPath());
        Container container = gson.fromJson(reader, Container.class);
        container.importData(localizeMsgRAF);
        reader.close();
        closeRAF();
    }

    private static void initRandomAccessFile(File localizeMsgDat) throws Exception {
        localizeMsgRAF = new RandomAccessFile(localizeMsgDat, "rw");
    }

    private static void closeRAF() throws Exception {
        localizeMsgRAF.close();
    }

}
