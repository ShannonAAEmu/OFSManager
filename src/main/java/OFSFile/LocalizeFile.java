package OFSFile;

import Localization.Localize;
import com.google.gson.annotations.Expose;

import java.util.Arrays;
import java.util.Objects;

public class LocalizeFile {

    @Expose()
    int id;

    @Expose()
    int textCount;

    long totalOffset;

    long totalSize;

    long localSize;

    long localOffsetSize;

    @Expose()
    Localize[] localizesText;

    public LocalizeFile(int id, long ofsFileTotalOffset, long ofsFileTotalSize) {
        this.id = id;
        this.totalOffset = ofsFileTotalOffset;
        this.totalSize = ofsFileTotalSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTextCount() {
        return textCount;
    }

    public void setTextCount(int textCount) {
        this.textCount = textCount;
    }

    public long getTotalOffset() {
        return totalOffset;
    }

    public void setTotalOffset(long totalOffset) {
        this.totalOffset = totalOffset;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getLocalSize() {
        return localSize;
    }

    public void setLocalSize(long localSize) {
        this.localSize = localSize;
    }

    public long getLocalOffsetSize() {
        return localOffsetSize;
    }

    public void setLocalOffsetSize(long localOffsetSize) {
        this.localOffsetSize = localOffsetSize;
    }

    public Localize[] getLocalizesText() {
        return localizesText;
    }

    public void setLocalizesText(Localize[] localizesText) {
        this.localizesText = localizesText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalizeFile that = (LocalizeFile) o;
        return id == that.id && textCount == that.textCount && totalOffset == that.totalOffset && totalSize == that.totalSize && localSize == that.localSize && localOffsetSize == that.localOffsetSize && Arrays.equals(localizesText, that.localizesText);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, textCount, totalOffset, totalSize, localSize, localOffsetSize);
        result = 31 * result + Arrays.hashCode(localizesText);
        return result;
    }

    @Override
    public String toString() {
        return "LocalizeFile{" +
                "id=" + id +
                ", textCount=" + textCount +
                ", totalOffset=" + totalOffset +
                ", totalSize=" + totalSize +
                ", localSize=" + localSize +
                ", localOffsetSize=" + localOffsetSize +
                ", localizesText=" + Arrays.toString(localizesText) +
                '}';
    }
}
