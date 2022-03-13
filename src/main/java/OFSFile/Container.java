package OFSFile;

import Localization.Localize;
import Utils.Utils;
import com.google.gson.annotations.Expose;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Container {

    long containerTotalSize;

    @Expose()
    int ofsFilesCount;

    @Expose()
    LocalizeFile[] localizeFilesArray;

    public Container(long containerTotalSize, int ofsFilesCount) {
        this.containerTotalSize = containerTotalSize;
        this.ofsFilesCount = ofsFilesCount;
        this.localizeFilesArray = new LocalizeFile[ofsFilesCount];
    }

    public void exportData(RandomAccessFile raf) throws Exception {
        byte[] ofsFileTotalOffset = new byte[4];
        byte[] ofsFileTotalSize = new byte[4];
        for (int i = 0; i < localizeFilesArray.length; i++) {
            raf.read(ofsFileTotalOffset);
            raf.read(ofsFileTotalSize);
            localizeFilesArray[i] = new LocalizeFile(i, Utils.byteArrayToLong(ofsFileTotalOffset), Utils.byteArrayToLong(ofsFileTotalSize));
        }
        for (LocalizeFile localizeFile : localizeFilesArray) {
            readOFSFile(localizeFile, raf);
        }
    }

    private void readOFSFile(LocalizeFile localizeFile, RandomAccessFile raf) throws Exception {
        if (Utils.readOFSHeader(raf) && Utils.readSecondOFSHeader(raf) && Utils.readType(raf) == 0) {
            Utils.readUnkBytes(raf);
            if (Utils.readType(raf) == 1) {
                byte[] localSize = new byte[4];
                byte[] textsCount = new byte[4];
                Utils.readBytes(raf, localSize);
                Utils.readBytes(raf, textsCount);
                localizeFile.setLocalSize(Utils.byteArrayToLong(localSize));
                localizeFile.setTextCount(Utils.byteArrayToInt(textsCount));
                byte[] textOffset = new byte[4];
                byte[] textId = new byte[4];
                localizeFile.setLocalizesText(new Localize[localizeFile.getTextCount()]);
                for (int i = 0; i < localizeFile.getTextCount(); i++) {
                    Utils.readBytes(raf, textOffset);
                    Utils.readBytes(raf, textId);
                    localizeFile.getLocalizesText()[i] = new Localize(Utils.byteArrayToLong(textOffset), Utils.byteArrayToInt(textId));
                }
                byte[] textBytes;
                byte[] correctTextBytes = new byte[0];
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < localizeFile.getLocalizesText().length; j++) {
                    if (j != localizeFile.getLocalizesText().length - 1) {
                        textBytes = new byte[(int) (localizeFile.getLocalizesText()[j + 1].getOffset() - localizeFile.getLocalizesText()[j].getOffset())];
                    } else {
                        textBytes = new byte[(int) (localizeFile.getLocalSize() - localizeFile.getLocalizesText()[j].getOffset())];
                    }
                    Utils.readBytes(raf, textBytes);
                    for (int k = 0; k < textBytes.length; k++) {
                        if (textBytes[k] == 0) {
                            correctTextBytes = new byte[k];
                            break;
                        }
                    }
                    System.arraycopy(textBytes, 0, correctTextBytes, 0, correctTextBytes.length);
                    String hex = Utils.convertByteToHex(correctTextBytes);
                    sb.append(new String(Hex.decodeHex(hex)));
                    localizeFile.getLocalizesText()[j].setOriginalText(sb.toString());
                    localizeFile.getLocalizesText()[j].setNewLines(StringUtils.countMatches(sb.toString(), "\n"));
                    sb.setLength(0);
                }
                Set<String> allText = new HashSet<>();
                for (Localize localize : localizeFile.getLocalizesText()) {
                    if (!allText.add(localize.getOriginalText())) {
                        localize.setUnique(false);
                        for (Localize localizePrevious : localizeFile.getLocalizesText()) {
                            if (localize.getOriginalText().equals(localizePrevious.getOriginalText()))
                                localizePrevious.setUnique(false);
                        }
                    }
                }
            } else {
                System.out.println("Not an OFS3 localize file");
            }
        }
    }

    public void importData(RandomAccessFile localizeMsgRAF) throws Exception {
        updateOriginalText();
        normalizeTextLength();
        calculateSizes();
        writeData(localizeMsgRAF);
    }

    private void updateOriginalText() {
        for (LocalizeFile localizeFile : this.getLocalizeFilesArray()) {
            for (Localize localize : localizeFile.getLocalizesText()) {
                if (!"".equals(localize.getNewText()))
                    localize.setOriginalText(localize.getNewText());
            }
        }
    }

    private void normalizeTextLength() {
        byte[] correctBytes;
        for (LocalizeFile localizeFile : this.getLocalizeFilesArray()) {
            for (Localize localize : localizeFile.getLocalizesText()) {
                int correctLength = localize.getOriginalText().getBytes().length + 1;
                while (correctLength % 4 != 0)
                    correctLength++;
                correctBytes = new byte[correctLength];
                System.arraycopy(localize.getOriginalText().getBytes(), 0, correctBytes, 0, localize.getOriginalText().getBytes().length);
                localize.setImportBytes(correctBytes);
            }
        }
    }

    private void calculateSizes() {
        long allTextLength;
        for (LocalizeFile localizeFile : this.getLocalizeFilesArray()) {
            localizeFile.setLocalOffsetSize(4 + localizeFile.getTextCount() * 8L);
            allTextLength = 0;
            for (Localize localize : localizeFile.getLocalizesText()) {
                allTextLength += localize.getImportBytes().length;
            }
            localizeFile.setLocalSize(localizeFile.getLocalOffsetSize() + allTextLength);
            localizeFile.setTotalSize(localizeFile.getLocalSize() + 16);
        }
    }

    private void writeData(RandomAccessFile localizeMsgRAF) throws Exception {
        String hexStr;
        long totalSize = 0;
        for (LocalizeFile localizeFile : this.getLocalizeFilesArray()) {
            totalSize += localizeFile.getTotalSize();
        }
        totalSize += 4 + this.getOfsFilesCount() * 8L;
        Utils.writeBytes(localizeMsgRAF, Hex.decodeHex("4F4653331000000000000400"));
        hexStr = Long.toHexString(totalSize);
        writeBytes(hexStr, localizeMsgRAF);
        hexStr = Integer.toHexString(this.getOfsFilesCount());
        writeBytes(hexStr, localizeMsgRAF);
        long offset = 4 + this.getOfsFilesCount() * 8L;
        for (int i = 0; i < this.getOfsFilesCount(); i++) {
            hexStr = Long.toHexString(offset);
            writeBytes(hexStr, localizeMsgRAF);
            hexStr = Long.toHexString(this.getLocalizeFilesArray()[i].getTotalSize());
            writeBytes(hexStr, localizeMsgRAF);
            offset += this.getLocalizeFilesArray()[i].getTotalSize();
        }
        for (LocalizeFile localizeFile : this.getLocalizeFilesArray()) {
            Utils.writeBytes(localizeMsgRAF, Hex.decodeHex("4F4653331000000000000401"));
            hexStr = Long.toHexString(localizeFile.getLocalSize());
            writeBytes(hexStr, localizeMsgRAF);
            hexStr = Integer.toHexString(localizeFile.getTextCount());
            writeBytes(hexStr, localizeMsgRAF);
            offset = 4 + localizeFile.getTextCount() * 8L;
            for (Localize localize : localizeFile.getLocalizesText()) {
                hexStr = Long.toHexString(offset);
                writeBytes(hexStr, localizeMsgRAF);
                offset += localize.getImportBytes().length;
                hexStr = Integer.toHexString(localize.getId());
                writeBytes(hexStr, localizeMsgRAF);
            }
            for (Localize localize : localizeFile.getLocalizesText()) {
                Utils.writeBytes(localizeMsgRAF, localize.getImportBytes());
            }
        }
    }

    private void writeBytes(String hexStr, RandomAccessFile localizeMsgRAF) throws Exception {
        hexStr = Utils.normalizeHexLength(hexStr, 8);
        hexStr = new String(Utils.reverse4Bytes(hexStr.getBytes()));
        Utils.writeBytes(localizeMsgRAF, Hex.decodeHex(hexStr));
    }

    public long getContainerTotalSize() {
        return containerTotalSize;
    }

    public void setContainerTotalSize(long containerTotalSize) {
        this.containerTotalSize = containerTotalSize;
    }

    public int getOfsFilesCount() {
        return ofsFilesCount;
    }

    public void setOfsFilesCount(int ofsFilesCount) {
        this.ofsFilesCount = ofsFilesCount;
    }

    public LocalizeFile[] getLocalizeFilesArray() {
        return localizeFilesArray;
    }

    public void setLocalizeFilesArray(LocalizeFile[] localizeFilesArray) {
        this.localizeFilesArray = localizeFilesArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return containerTotalSize == container.containerTotalSize && ofsFilesCount == container.ofsFilesCount && Arrays.equals(localizeFilesArray, container.localizeFilesArray);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(containerTotalSize, ofsFilesCount);
        result = 31 * result + Arrays.hashCode(localizeFilesArray);
        return result;
    }

    @Override
    public String toString() {
        return "Container{" +
                "containerTotalSize=" + containerTotalSize +
                ", ofsFilesCount=" + ofsFilesCount +
                ", localizeFilesArray=" + Arrays.toString(localizeFilesArray) +
                '}';
    }

}
