package com.taobao.timetunnel.client.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-9
 * 
 */
public class ZlibCompressAlgo implements CompresssAlgo {
	private static final Logger log = Logger.getLogger(ZlibCompressAlgo.class);
	private final String algoName;

	public ZlibCompressAlgo() {
		algoName = "ZLIB";
	}

	@Override
	public String algoName() {
		return algoName;
	}

	@Override
	public byte[] compress(byte[] input) {
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		compressor.setInput(input);
		compressor.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		try {
			// free native memory
			compressor.end();
			bos.close();
		} catch (IOException e) {
		}
		byte[] compressedData = bos.toByteArray();
		return compressedData;
	}

	@Override
	public byte[] decompress(byte[] input) {
		Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		byte[] buf = new byte[1024];
		try {
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			}
		} catch (DataFormatException e) {
			log.error("Error during decompression", e);
			return input;// throw new RuntimeException(e);
		} finally {
			// free native memory
			decompressor.end();
			try {
				bos.close();
			} catch (IOException e) {
				log.error("Error close stream", e);
				throw new RuntimeException(e);
			}
		}

		byte[] decompressedData = bos.toByteArray();
		return decompressedData;
	}
}
