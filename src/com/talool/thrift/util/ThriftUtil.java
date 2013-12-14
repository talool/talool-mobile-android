package com.talool.thrift.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.thrift.TBase;
import org.apache.thrift.TBaseHelper;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author clintz
 * 
 */
public final class ThriftUtil
{
	// If the protocol changes from binary - you must change this!
	public static final Factory PROTOCOL_FACTORY = new TBinaryProtocol.Factory();

	/**
	 * Uses default binary protocol factory
	 * 
	 * @param obj
	 * @param protocolFactory
	 * @return
	 * @throws TException
	 */
	@SuppressWarnings("rawtypes")
	public static byte[] serialize(final TBase obj)
	{
		final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
		byte[] bytes = null;
		try {
			bytes = serializer.serialize(obj);
			return bytes;

		} catch (TException e) {
		}
		return bytes;
	}

	@SuppressWarnings("rawtypes")
	public static byte[] serialize(final TBase obj, final TProtocolFactory protocolFactory) throws TException
	{
		final TSerializer serializer = new TSerializer(protocolFactory);
		final byte[] bytes = serializer.serialize(obj);
		return bytes;
	}

	public static void writeToDisk(byte[] bytes, final TProtocolFactory protocolFactory, final String fileName)
			throws TException, IOException
	{
		OutputStream out = null;

		try
		{
			out = new BufferedOutputStream(new FileOutputStream(fileName));
			out.write(bytes);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public static byte[] readFromFile(final File file, final TBase obj, final TProtocolFactory protocolFactory)
			throws IOException, TException
	{
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE)
		{
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		{
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length)
		{
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();

		final TDeserializer deSerializer = new TDeserializer(protocolFactory);
		deSerializer.deserialize(obj, bytes);

		return bytes;
	}

	@SuppressWarnings("rawtypes")
	public static byte[] serializeToDisk(final TBase obj, final TProtocolFactory protocolFactory, final String fileName)
			throws TException, IOException
	{
		final TSerializer serializer = new TSerializer(protocolFactory);
		final byte[] bytes = serializer.serialize(obj);

		OutputStream out = null;

		try
		{
			out = new BufferedOutputStream(new FileOutputStream(fileName));
			out.write(bytes);
			return bytes;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}

	}

	@SuppressWarnings("rawtypes")
	/**
	 * De-serializes using the default binary protocol
	 * @param bytes
	 * @param obj
	 */
	public static void deserialize(final byte[] bytes, final TBase obj)
			throws TException
	{
		final TDeserializer deserializer = new TDeserializer(PROTOCOL_FACTORY);
		deserializer.deserialize(obj, bytes);
	}

	@SuppressWarnings("rawtypes")
	public static void deserialize(final byte[] bytes, final TBase obj, final TProtocolFactory protocolFactory)
			throws TException
	{
		final TDeserializer deserializer = new TDeserializer(protocolFactory);
		deserializer.deserialize(obj, bytes);
	}

	public static byte[] byteBufferToByteArray(final ByteBuffer byteBuffer)
	{
		return TBaseHelper.byteBufferToByteArray(byteBuffer);
	}

	public static int byteBufferToByteArray(final ByteBuffer byteBuffer, final byte[] target, final int offset)
	{
		return TBaseHelper.byteBufferToByteArray(byteBuffer, target, offset);
	}

}
