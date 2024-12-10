package io.tapdata.modules.api.net.data;

import java.io.File;
import java.io.InputStream;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/2 18:17
 */
public class FileMeta {
    private String filename;
    private Long fileSize;
    private InputStream fileInputStream;
    private String code;
    private boolean transferFile;

    public FileMeta() {

    }

    public FileMeta(String filename, Long fileSize, InputStream fileInputStream, String code, boolean transferFile) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileInputStream = fileInputStream;
        this.code = code;
        this.transferFile = transferFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public void setFileInputStream(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isTransferFile() {
        return transferFile;
    }

    public void setTransferFile(boolean transferFile) {
        this.transferFile = transferFile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filename;
        private Long fileSize;
        private InputStream fileInputStream;
        private String code;
        private boolean transferFile;

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }
        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }
        public Builder fileInputStream(InputStream fileInputStream) {
            this.fileInputStream = fileInputStream;
            return this;
        }
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        public Builder transferFile(boolean transferFile) {
            this.transferFile = transferFile;
            return this;
        }

        public FileMeta build() {
            return new FileMeta(filename, fileSize, fileInputStream, code, transferFile);
        }
    }
}
