package io.tapdata.modules.api.net.data;

import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/23 15:21
 */
public class TransferFileMessage extends IncomingData{
    public static final byte TYPE = 62;
    private FileMeta fileMeta;

    public TransferFileMessage() {
        super(TYPE);
    }

    public TransferFileMessage(byte[] data, Byte encode) {
        this();

        setData(data);
        setEncode(encode);
        resurrect();
    }

    public TransferFileMessage fileMeta(FileMeta fileMeta) {
        this.fileMeta = fileMeta;
        return this;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dis = dataInputStream(inputStream);
        super.from(dis);
        boolean hasFile = dis.readBoolean();
        if (hasFile) {
            fileMeta = FileMeta.builder()
                    .filename(dis.readUTF())
                    .fileSize(dis.readLong())
                    .code(dis.readUTF())
                    .transferFile(dis.readBoolean()).build();
        }

    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dos = dataOutputStream(outputStream);
        super.to(dos);
        if (fileMeta != null) {
            dos.writeBoolean(true);
            dos.writeUTF(fileMeta.getFilename());
            dos.writeLong(fileMeta.getFileSize());
            dos.writeUTF(fileMeta.getCode());
            dos.writeBoolean(fileMeta.isTransferFile());
        } else {
            dos.writeBoolean(false);
        }
    }

    @Override
    public FileMeta getFileMeta() {
        return fileMeta;
    }

    public void setFileTransfer(FileMeta fileMeta) {
        this.fileMeta = fileMeta;
    }
}
