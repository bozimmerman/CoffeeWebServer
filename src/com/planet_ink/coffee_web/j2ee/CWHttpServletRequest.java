package com.planet_ink.coffee_web.j2ee;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.planet_ink.coffee_common.collections.EmptyEnumeration;
import com.planet_ink.coffee_common.collections.IteratorEnumeration;
import com.planet_ink.coffee_common.collections.Pair;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.CWThread;

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
public class CWHttpServletRequest implements HttpServletRequest
{

	private final Map<String, Object>	attributes	= new Hashtable<String, Object>();
	private final SimpleServletRequest	req;
	private final CWConfig				config;
	private final String				path;
	private String						charEnc = null;


	public CWHttpServletRequest(final String path, final SimpleServletRequest req)
	{
		this.path=path;
		this.config = ((CWThread)Thread.currentThread()).getConfig();
		this.req = req;
	}

	@Override
	public Object getAttribute(final String name)
	{
		return attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getAttributeNames()
	{
		return EmptyEnumeration.instance;
	}

	@Override
	public String getCharacterEncoding()
	{
		return charEnc;
	}

	@Override
	public void setCharacterEncoding(final String env) throws UnsupportedEncodingException
	{
		charEnc = env;
	}

	@Override
	public int getContentLength()
	{
		try
		{
			return Integer.parseInt(req.getHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName()));
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	@Override
	public long getContentLengthLong()
	{
		try
		{
			return Long.parseLong(req.getHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName()));
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	@Override
	public String getContentType()
	{
		return req.getHeader(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName());
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return new ServletInputStream()
		{
			boolean finished = req.getBody().available()==0;
			boolean ready = req.getBody().available()>0;
			@Override
			public boolean isFinished()
			{
				return finished;
			}

			@Override
			public boolean isReady()
			{
				return ready;
			}

			@Override
			public void setReadListener(final ReadListener readListener)
			{
				throw new java.lang.UnsupportedOperationException();
			}

			@Override
			public int read() throws IOException
			{
				final int c = req.getBody().read();
				if(c < 0)
				{
					ready = false;
					finished = true;
				}
				return c;
			}

		};
	}

	@Override
	public String getParameter(final String name)
	{
		return req.getUrlParameter(name);
	}

	@Override
	public Enumeration<String> getParameterNames()
	{
		return new IteratorEnumeration<String>(getParameterMap().keySet().iterator());
	}

	@Override
	public String[] getParameterValues(final String name)
	{
		return getParameterMap().get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap()
	{
		final Set<String> done = new HashSet<String>();
		final Map<String, String[]> parameters = new HashMap<String, String[]>();
		for(final String s : req.getUrlParameters())
		{
			if(req.isUrlParameter(s+"1") && (!done.contains(s)))
			{
				done.add(s);
				final ArrayList<String> values = new ArrayList<String>();
				values.add(req.getUrlParameter(s));
				for(int i=1;req.isUrlParameter(s+i);i++)
				{
					done.add(s+i);
					values.add(req.getUrlParameter(s+i));
				}
				final String[] vals = values.toArray(new String[values.size()]);
				parameters.put(s, vals);

			}
		}
		for(final String s : req.getUrlParameters())
		{
			if(!done.contains(s))
				parameters.put(s, new String[] {req.getUrlParameter(s)});
		}
		return parameters;
	}

	@Override
	public String getProtocol()
	{
		return "HTTP/"+req.getHttpVer();
	}

	@Override
	public String getScheme()
	{
		final String fullHost = req.getFullHost();
		final int x = fullHost.indexOf(':');
		if(x > 0)
			return fullHost.substring(0,x);
		return "http";
	}

	@Override
	public String getServerName()
	{
		return req.getHost();
	}

	@Override
	public int getServerPort()
	{
		return req.getClientPort();
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return new BufferedReader(new InputStreamReader(req.getBody()));
	}

	@Override
	public String getRemoteAddr()
	{
		return req.getClientAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost()
	{
		return req.getClientAddress().getHostName();
	}

	@Override
	public void setAttribute(final String name, final Object o)
	{
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(final String name)
	{
		attributes.remove(name);
	}

	@Override
	public Locale getLocale()
	{
		final String acceptLang = req.getHeader(HTTPHeader.Common.ACCEPT_LANGUAGE.lowerCaseName());
		if(acceptLang == null || acceptLang.length()==0)
			return Locale.getDefault();
		try
		{
			final List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLang);
			if (ranges.isEmpty())
				return Locale.getDefault();
			final String preferredTag = ranges.get(0).getRange();
			final Locale locale = Locale.forLanguageTag(preferredTag);

			if (locale.equals(Locale.ROOT))
				return Locale.getDefault();
			return locale;

		}
		catch(final IllegalArgumentException e)
		{
			return Locale.getDefault();
		}
	}

	@Override
	public Enumeration<Locale> getLocales()
	{
		final Set<Locale> arr = new HashSet<Locale>();
		final String acceptLang = req.getHeader(HTTPHeader.Common.ACCEPT_LANGUAGE.lowerCaseName());
		if(acceptLang == null || acceptLang.length()==0)
			arr.add(Locale.getDefault());
		try
		{
			final List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLang);
			if (ranges.isEmpty())
				arr.add(Locale.getDefault());
			else
			{
				for(final Locale.LanguageRange r : ranges)
				{
					final String preferredTag = r.getRange();
					final Locale locale = Locale.forLanguageTag(preferredTag);

					if (locale.equals(Locale.ROOT))
						arr.add(Locale.getDefault());
					arr.add(locale);
				}
			}
		}
		catch(final IllegalArgumentException e)
		{
			arr.add(Locale.getDefault());
		}
		return new IteratorEnumeration<Locale>(arr.iterator());
	}

	@Override
	public boolean isSecure()
	{
		return this.getScheme().equalsIgnoreCase("https");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path)
	{
		return null;
	}

	@Override
	public String getRealPath(final String path)
	{
		if (path == null)
			return null;
		final Pair<String,String> mountPath = config.getMount(req.getHost(), req.getClientPort(), path);
		if (mountPath != null)
		{
			String newFullPath = path.substring(mountPath.first.length());
			if (newFullPath.startsWith("/") && mountPath.second.endsWith("/"))
				newFullPath = newFullPath.substring(1);
			final String realPath = mountPath.second + newFullPath;
			final FileManager mgr = config.getFileManager();
			return realPath.replace('/', mgr.getFileSeparator());
		}
		return null;
	}

	@Override
	public int getRemotePort()
	{
		return req.getClientPort();
	}

	@Override
	public String getLocalName()
	{
		return req.getHost();
	}

	@Override
	public String getLocalAddr()
	{
		return "127.0.0.1";
	}

	@Override
	public int getLocalPort()
	{
		return req.getClientPort();
	}

	@Override
	public ServletContext getServletContext()
	{
		return new CWServletContext(config, path);
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException
	{
		throw new IllegalStateException();
	}

	@Override
	public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
			throws IllegalStateException
	{
		throw new IllegalStateException();
	}

	@Override
	public boolean isAsyncStarted()
	{
		return false;
	}

	@Override
	public boolean isAsyncSupported()
	{
		return false;
	}

	@Override
	public AsyncContext getAsyncContext()
	{
		return null;
	}

	@Override
	public DispatcherType getDispatcherType()
	{
		return DispatcherType.REQUEST;
	}

	@Override
	public String getAuthType()
	{
		return null;
	}

	@Override
	public Cookie[] getCookies()
	{
		final ArrayList<CWCookie> cookies = new ArrayList<CWCookie>();
		for(final Enumeration<com.planet_ink.coffee_web.http.Cookie> c = req.getCookies();c.hasMoreElements();)
			cookies.add(new CWCookie(c.nextElement()));
		return cookies.toArray(new CWCookie[cookies.size()]);
	}

	@Override
	public long getDateHeader(final String name)
	{
		final String httpDate = getHeader(name);
		if(httpDate == null)
			return -1;
		try
		{
			final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
			final ZonedDateTime zonedDateTime = ZonedDateTime.parse(httpDate, formatter);
			return zonedDateTime.toInstant().toEpochMilli();
		}
		catch (final DateTimeParseException e)
		{
			return -1;
		}
	}

	@Override
	public String getHeader(final String name)
	{
		return req.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(final String name)
	{
		return req.getHeaders();
	}

	@Override
	public Enumeration<String> getHeaderNames()
	{
		return req.getHeaders();
	}

	@Override
	public int getIntHeader(final String name)
	{
		try
		{
			return Integer.parseInt(getHeader(name));
		}
		catch(final Exception e)
		{
			return -1;
		}
	}

	@Override
	public String getMethod()
	{
		return req.getMethod().name();
	}

	@Override
	public String getPathInfo()
	{
		return null;
	}

	@Override
	public String getPathTranslated()
	{
		return null;
	}

	@Override
	public String getContextPath()
	{
		final String path = req.getUrlPath();
		final int x = path.indexOf('?');
		if(x<0)
			return path;
		return path.substring(0,x);
	}

	@Override
	public String getQueryString()
	{
		return req.getQueryString();
	}

	@Override
	public String getRemoteUser()
	{
		return this.req.getSession().getUser();
	}

	@Override
	public boolean isUserInRole(final String role)
	{
		return false;
	}

	@Override
	public Principal getUserPrincipal()
	{
		return null;
	}

	@Override
	public String getRequestedSessionId()
	{
		return this.req.getSession().getSessionId();
	}

	@Override
	public String getRequestURI()
	{
		String url = req.getFullRequest();
		int x = url.indexOf("://");
		if(x>0)
			url = url.substring(x+3);
		x = url.indexOf('?');
		if(x>0)
			return url.substring(0,x);
		return url;
	}

	@Override
	public StringBuffer getRequestURL()
	{
		final String r = req.getFullRequest();
		final int x = r.indexOf('?');
		if(x < 0)
			return new StringBuffer(r);
		return new StringBuffer(r.substring(0,x));
	}

	@Override
	public String getServletPath()
	{
		if(!path.startsWith("/"))
			return "/"+path;
		return path;
	}

	@Override
	public HttpSession getSession(final boolean create)
	{
		return new CWHttpSession(this.path,this.config,this.req.getSession());
	}

	@Override
	public HttpSession getSession()
	{
		return new CWHttpSession(this.path,this.config,this.req.getSession());
	}

	@Override
	public String changeSessionId()
	{
		return this.req.getSession().getSessionId();
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return false;
	}

	@Override
	public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException
	{
		throw new ServletException("Not supported");
	}

	@Override
	public void login(final String username, final String password) throws ServletException
	{
		throw new ServletException("Not supported");
	}

	@Override
	public void logout() throws ServletException
	{
		throw new ServletException("Not supported");
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException
	{
		final ArrayList<Part> parts = new ArrayList<Part>();
		for(final MultiPartData m : req.getMultiParts())
		{
			parts.add(new Part()
			{
				InputStream i = null;
				final MultiPartData mp = m;
				@Override
				public InputStream getInputStream() throws IOException
				{
					if(i == null)
						i = new ByteArrayInputStream(mp.getData());
					return i;
				}

				@Override
				public String getContentType()
				{
					return mp.getContentType();
				}

				@Override
				public String getName()
				{
					if(mp.getVariables().containsKey("name"))
						return mp.getVariables().get("name");
					if(mp.getVariables().containsKey("NAME"))
						return mp.getVariables().get("NAME");
					return "";
				}

				@Override
				public String getSubmittedFileName()
				{
					if(mp.getVariables().containsKey("filename"))
						return mp.getVariables().get("filename");
					if(mp.getVariables().containsKey("FILENAME"))
						return mp.getVariables().get("FILENAME");
					return "";
				}

				@Override
				public long getSize()
				{
					return mp.getData().length;
				}

				@Override
				public void write(final String fileName) throws IOException
				{
					try (FileOutputStream o = new FileOutputStream(fileName))
					{
						o.write(mp.getData());
					}
				}

				@Override
				public void delete() throws IOException
				{
					req.getMultiParts().remove(mp);
				}

				@Override
				public String getHeader(final String name)
				{
					return mp.getHeaders().get(name);
				}

				@Override
				public Collection<String> getHeaders(final String name)
				{
					final List<String> l = new ArrayList<String>(1);
					final String s = getHeader(name);
					if(s != null)
						l.add(s);
					return l;
				}

				@Override
				public Collection<String> getHeaderNames()
				{
					return mp.getHeaders().keySet();
				}

			});
		}
		return parts;
	}

	@Override
	public Part getPart(final String name) throws IOException, ServletException
	{
		if(req.getMultiParts().size()==0)
			throw new ServletException("No parts");
		for(final Part P : getParts())
			if(name.equals(P.getName()))
				return P;
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException
	{
		throw new ServletException("Not supported");
	}

}
