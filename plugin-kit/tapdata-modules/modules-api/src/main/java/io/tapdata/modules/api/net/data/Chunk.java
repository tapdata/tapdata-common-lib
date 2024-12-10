package io.tapdata.modules.api.net.data;

import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Chunk extends Data {
	public static final byte TYPE = 61;
	private String id;
	private Integer originalType;
	private byte[] content;
	private Integer offset;
	private Integer totalChunks;
	private Integer chunkNum;

	public Chunk() {
		super(TYPE);
	}

	public Chunk(byte[] data, byte encode) {
		super(TYPE);

		setData(data);
		setEncode(encode);
		resurrect();
	}

	@Override
	public void from(InputStream inputStream) throws IOException {
		super.from(inputStream);
		DataInputStreamEx dis = dataInputStream(inputStream);
		id = dis.readUTF();
		originalType = dis.readInt();
		content = dis.readBytes();
		offset = dis.readInt();
		totalChunks = dis.readInt();
		chunkNum = dis.readInt();
	}

	@Override
	public void to(OutputStream outputStream) throws IOException {
		super.to(outputStream);
		DataOutputStreamEx dos = dataOutputStream(outputStream);
		dos.writeUTF(id);
		dos.writeInt(originalType);
		dos.writeBytes(content);
		dos.writeInt(offset);
		dos.writeInt(totalChunks);
		dos.writeInt(chunkNum);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getOriginalType() {
		return originalType;
	}

	public void setOriginalType(Integer originalType) {
		this.originalType = originalType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getTotalChunks() {
		return totalChunks;
	}

	public void setTotalChunks(Integer totalChunks) {
		this.totalChunks = totalChunks;
	}

	public Integer getChunkNum() {
		return chunkNum;
	}

	public void setChunkNum(Integer chunkNum) {
		this.chunkNum = chunkNum;
	}
}