package com.planet_ink.coffee_web.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.util.Map;

import com.planet_ink.coffee_common.logging.Log;
import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWConfig;

/*
Copyright 2014-2014 Bo Zimmerman

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

public class CGIConverter implements HTTPOutputConverter
{
	private final String executeable;

	public CGIConverter(String executeable)
	{
		this.executeable=executeable;
	}

	private static enum EnvironmentVariables 
	{ 
		AUTH_TYPE,
		CONTENT_LENGTH,
		CONTENT_TYPE,
		GATEWAY_INTERFACE,
		HTTP_,
		PATH_INFO,
		PATH_TRANSLATED,
		QUERY_STRING,
		REMOTE_ADDR,
		REMOTE_HOST,
		REMOTE_IDENT,
		REMOTE_USER,
		REQUEST_METHOD,
		SCRIPT_NAME,
		SERVER_NAME,
		SERVER_PORT,
		SERVER_PROTOCOL,
		SERVER_SOFTWARE, 
	}
	
	
	/**
	 * Standard method for converting an input buffer for writing to 
	 * the client.   The position and limit of the bytebuffer must
	 * already be set for reading the content.
	 * Call generateOutput() to get the new output.
	 * @param config the http configuration (optional, may be null)
	 * @param request the http request bring processed  (optional, may be null)
	 * @param pageFile the file being converted
	 * @param status the status of the request (so far)
	 * @param buffer the input buffer
	 * @throws HTTPException
	 */
	@Override
	public ByteBuffer convertOutput(CWConfig config, HTTPRequest request, File pageFile, HTTPStatus status, ByteBuffer buffer) throws HTTPException
	{
		// http://tools.ietf.org/html/draft-robinson-www-interface-00
		if(request == null)
		{
			Log.errOut("CGIConverter requires a non-null request.");
			return buffer;
		}
		final ProcessBuilder builder = new ProcessBuilder(executeable);
		final Map<String, String> env = builder.environment();
		env.put(EnvironmentVariables.AUTH_TYPE.name(),"");
		env.put(EnvironmentVariables.CONTENT_LENGTH.name(),""+buffer.remaining());
		if(request.getHeader(HTTPHeader.CONTENT_TYPE.toString()) != null)
			env.put(EnvironmentVariables.CONTENT_TYPE.name(),request.getHeader(HTTPHeader.CONTENT_TYPE.toString()));
		else
			env.put(EnvironmentVariables.CONTENT_TYPE.name(),MIMEType.html.getType());
		env.put(EnvironmentVariables.GATEWAY_INTERFACE.name(),"CGI/1.1");
		env.put(EnvironmentVariables.PATH_INFO.name(),request.getUrlPath());
		env.put(EnvironmentVariables.PATH_TRANSLATED.name(),"");
		env.put(EnvironmentVariables.QUERY_STRING.name(),request.getQueryString());
		env.put(EnvironmentVariables.REMOTE_ADDR.name(),request.getClientAddress().toString());
		env.put(EnvironmentVariables.REMOTE_HOST.name(),null);
		env.put(EnvironmentVariables.REMOTE_IDENT.name(),null);
		env.put(EnvironmentVariables.REMOTE_USER.name(),null);
		env.put(EnvironmentVariables.REQUEST_METHOD.name(),request.getMethod().toString());
		env.put(EnvironmentVariables.SCRIPT_NAME.name(),request.getUrlPath());
		env.put(EnvironmentVariables.SERVER_NAME.name(),request.getHost());
		env.put(EnvironmentVariables.SERVER_PORT.name(),""+request.getClientPort());
		env.put(EnvironmentVariables.SERVER_PROTOCOL.name(),"HTTP/"+request.getHttpVer());
		env.put(EnvironmentVariables.SERVER_SOFTWARE.name(),WebServer.NAME);
		for(HTTPHeader header : HTTPHeader.values())
		{
			final String value=request.getHeader(header.name());
			if(value != null)
				env.put("HTTP_"+header.name().replace('-','_'),value);
		}
		try 
		{
			builder.redirectError(Redirect.PIPE);
			final Process process = builder.start();
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final InputStream in = process.getInputStream();
			int b=in.read();
			while(b != -1)
			{
				bout.write(b);
				b=in.read();
			}
			int retCode = process.waitFor();
			if(retCode != 0)
			{
				final InputStream errin = process.getErrorStream();
				StringBuilder errMsg = new StringBuilder("");
				b=errin.read();
				while(b != -1)
				{
					errMsg.append((char)b);
					b=errin.read();
				}
				Log.errOut(errMsg.toString());
			}
			return ByteBuffer.wrap(bout.toByteArray());
		} 
		catch (IOException e) 
		{
			Log.errOut("CGIConverter", e);
		} 
		catch (InterruptedException e) 
		{
			Log.errOut("CGIConverter", e);
		}
		return buffer;
	}
}
