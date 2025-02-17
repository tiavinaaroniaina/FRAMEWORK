package classes.mvc;

public class FileUpload {
    private String fileName;
    private String filePath;
    private byte[] bytes;

    public FileUpload(String fileName, String filePath, byte[] bytes) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.bytes = bytes;
    }

    public FileUpload() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
