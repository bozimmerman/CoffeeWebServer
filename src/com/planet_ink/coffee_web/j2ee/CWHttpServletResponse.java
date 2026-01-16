package com.planet_ink.coffee_web.j2ee;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;

/*
Copyright 2026-2026 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
public class CWHttpServletResponse implements HttpServletResponse
{
	private final SimpleServletResponse	resp;
	private String						charEnc		= "UTF-8";
	private Locale						locale		= Locale.getDefault();
	private PrintWriter					writer		= null;
	private ServletOutputStream			outputStream = null;
	private int							bufferSize	= 8192;

	public CWHttpServletResponse(final SimpleServletResponse resp)
	{
		this.resp = resp;
	}

	@Override
	public String getCharacterEncoding()
	{
		return charEnc;
	}

	@Override
	public String getContentType()
	{
		return resp.getMimeType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		if (writer != null)
			throw new IllegalStateException("getWriter() has already been called");
		if (outputStream == null)
		{
			outputStream = new ServletOutputStream()
			{
				final OutputStream out = resp.getOutputStream();

				@Override
				public boolean isReady()
				{
					return true;
				}

				@Override
				public void setWriteListener(final javax.servlet.WriteListener writeListener)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(final int b) throws IOException
				{
					out.write(b);
				}

				@Override
				public void write(final byte[] b) throws IOException
				{
					out.write(b);
				}

				@Override
				public void write(final byte[] b, final int off, final int len) throws IOException
				{
					out.write(b, off, len);
				}

				@Override
				public void flush() throws IOException
				{
					out.flush();
				}

				@Override
				public void close() throws IOException
				{
					out.close();
				}
			};
		}
		return outputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException
	{
		if (outputStream != null)
			throw new IllegalStateException("getOutputStream() has already been called");
		if (writer == null)
		{
			try
			{
				writer = new PrintWriter(new java.io.OutputStreamWriter(resp.getOutputStream(), charEnc))
				{
					@Override
					public void write(final int c)
					{
						super.write(c);
					}

					@Override
					public void write(final char[] buf, final int off, final int len)
					{
						super.write(buf, off, len);
					}

					@Override
					public void write(final String s, final int off, final int len)
					{
						super.write(s, off, len);
					}

					@Override
					public void flush()
					{
						super.flush();
					}
				};
			}
			catch (final UnsupportedEncodingException e)
			{
				throw new IOException(e);
			}
		}
		return writer;
	}

	@Override
	public void setCharacterEncoding(final String charset)
	{
		this.charEnc = charset;
	}

	@Override
	public void setContentLength(final int len)
	{
		setHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName(), Integer.toString(len));
	}

	@Override
	public void setContentLengthLong(final long len)
	{
		setHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName(), Long.toString(len));
	}

	@Override
	public void setContentType(final String type)
	{
		resp.setMimeType(type);
		if (type != null && type.contains("charset="))
		{
			final int idx = type.indexOf("charset=") + 8;
			final int end = type.indexOf(';', idx);
			final String charset = (end > 0) ? type.substring(idx, end).trim() : type.substring(idx).trim();
			setCharacterEncoding(charset);
		}
	}

	@Override
	public void setBufferSize(final int size)
	{
		this.bufferSize = size;
	}

	@Override
	public int getBufferSize()
	{
		return bufferSize;
	}

	@Override
	public void flushBuffer() throws IOException
	{
		if (writer != null)
			writer.flush();
		else
		if (outputStream != null)
			outputStream.flush();
	}

	@Override
	public void resetBuffer()
	{
		if (writer != null)
		{
			try
			{
				writer.close();
			}
			catch (final Exception e)
			{
			}
			writer = null;
		}
		if (outputStream != null)
		{
			try
			{
				outputStream.close();
			}
			catch (final Exception e)
			{
			}
			outputStream = null;
		}
	}

	@Override
	public boolean isCommitted()
	{
		return false;
	}

	@Override
	public void reset()
	{
		resetBuffer();
		resp.setStatusCode(HTTPStatus.S200_OK);
		// Clear headers
		for (final Iterator<String> it = resp.getHeaderNames(); it.hasNext();)
			resp.removeHeader(it.next());
	}

	@Override
	public void setLocale(final Locale loc)
	{
		this.locale = loc;
		setHeader(HTTPHeader.Common.CONTENT_LANGUAGE.lowerCaseName(), loc.toLanguageTag());
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	public void addCookie(final Cookie cookie)
	{
		final com.planet_ink.coffee_web.http.Cookie cwCookie = new com.planet_ink.coffee_web.http.Cookie(
				cookie.getName(), cookie.getValue());
		if (cookie.getDomain() != null)
			cwCookie.domain = cookie.getDomain();
		if (cookie.getPath() != null)
			cwCookie.path = cookie.getPath();
		cwCookie.maxAge = cookie.getMaxAge();
		cwCookie.secure = cookie.getSecure();
		cwCookie.httpOnly = cookie.isHttpOnly();
		resp.setCookie(cwCookie);
	}

	@Override
	public boolean containsHeader(final String name)
	{
		return resp.getHeader(name) != null;
	}

	@Override
	public String encodeURL(final String url)
	{
		return url;
	}

	@Override
	public String encodeRedirectURL(final String url)
	{
		return url;
	}

	@Override
	public String encodeUrl(final String url)
	{
		return encodeURL(url);
	}

	@Override
	public String encodeRedirectUrl(final String url)
	{
		return encodeRedirectURL(url);
	}

	@Override
	public void sendError(final int sc, final String msg) throws IOException
	{
		final HTTPStatus status = HTTPStatus.find(sc);
		if (status != null)
			resp.setStatusCode(status);
		else
			resp.setStatusCode(HTTPStatus.S500_INTERNAL_ERROR);
	}

	@Override
	public void sendError(final int sc) throws IOException
	{
		sendError(sc, null);
	}

	@Override
	public void sendRedirect(final String location) throws IOException
	{
		resp.setStatusCode(HTTPStatus.S302_FOUND);
		setHeader(HTTPHeader.Common.LOCATION.lowerCaseName(), location);
	}

	@Override
	public void setDateHeader(final String name, final long date)
	{
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("GMT"));
		final String formattedDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		setHeader(name, formattedDate);
	}

	@Override
	public void addDateHeader(final String name, final long date)
	{
		setDateHeader(name, date);
	}

	@Override
	public void setHeader(final String name, final String value)
	{
		resp.setHeader(name, value);
	}

	@Override
	public void addHeader(final String name, final String value)
	{
		final String existing = resp.getHeader(name);
		if (existing != null)
			resp.setHeader(name, existing + "," + value);
		else
			resp.setHeader(name, value);
	}

	@Override
	public void setIntHeader(final String name, final int value)
	{
		setHeader(name, Integer.toString(value));
	}

	@Override
	public void addIntHeader(final String name, final int value)
	{
		addHeader(name, Integer.toString(value));
	}

	@Override
	public void setStatus(final int sc)
	{
		final HTTPStatus status = HTTPStatus.find(sc);
		if (status != null)
			resp.setStatusCode(status);
		else
			resp.setStatusCode(HTTPStatus.S500_INTERNAL_ERROR);
	}

	@Override
	public void setStatus(final int sc, final String sm)
	{
		setStatus(sc);
	}

	@Override
	public int getStatus()
	{
		final HTTPStatus status = resp.getStatus();
		return (status != null) ? status.getStatusCode() : 200;
	}

	@Override
	public String getHeader(final String name)
	{
		return resp.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(final String name)
	{
		final Collection<String> headers = new ArrayList<String>();
		final String header = resp.getHeader(name);
		if (header != null)
		{
			for (final String value : header.split(","))
				headers.add(value.trim());
		}
		return headers;
	}

	@Override
	public Collection<String> getHeaderNames()
	{
		final Collection<String> names = new ArrayList<String>();
		for (final Iterator<String> it = resp.getHeaderNames(); it.hasNext();)
			names.add(it.next());
		return names;
	}
}
