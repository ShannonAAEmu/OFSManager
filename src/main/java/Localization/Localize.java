package Localization;

import com.google.gson.annotations.Expose;

import java.util.Arrays;
import java.util.Objects;

public class Localize {

    @Expose()
    int id;

    long offset;

    @Expose()
    String originalText;

    @Expose()
    String newText;

    @Expose()
    boolean isUnique;

    @Expose()
    int newLines;

    byte[] importBytes;

    public Localize(long offset, int id) {
        this.offset = offset;
        this.id = id;
        this.newText = "";
        this.isUnique = true;
        this.newLines = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public int getNewLines() {
        return newLines;
    }

    public void setNewLines(int newLines) {
        this.newLines = newLines;
    }

    public byte[] getImportBytes() {
        return importBytes;
    }

    public void setImportBytes(byte[] importBytes) {
        this.importBytes = importBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localize localize = (Localize) o;
        return id == localize.id && offset == localize.offset && isUnique == localize.isUnique && newLines == localize.newLines && Objects.equals(originalText, localize.originalText) && Objects.equals(newText, localize.newText) && Arrays.equals(importBytes, localize.importBytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, offset, originalText, newText, isUnique, newLines);
        result = 31 * result + Arrays.hashCode(importBytes);
        return result;
    }

    @Override
    public String toString() {
        return "Localize{" +
                "id=" + id +
                ", offset=" + offset +
                ", originalText='" + originalText + '\'' +
                ", newText='" + newText + '\'' +
                ", isUnique=" + isUnique +
                ", newLines=" + newLines +
                ", importBytes=" + Arrays.toString(importBytes) +
                '}';
    }
}
