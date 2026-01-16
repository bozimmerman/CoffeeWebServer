package com.planet_ink.coffee_web.j2ee;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import com.planet_ink.coffee_common.collections.IteratorEnumeration;
import com.planet_ink.coffee_common.collections.Pair;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.util.CWConfig;

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
public class CWServletContext implements ServletContext
{
	private final CWConfig				config;
	private final String				path;
	private final Map<String, Object>	attributes;
	private volatile String				reqCharEnc = null;
	private volatile String				respCharEnc = null;

	public CWServletContext(final CWConfig config, final String path)
	{
		this.config=config;
		this.path = path;
		this.attributes = config.getServletMan().getServletContextVariables(path);
	}

	@Override
	public String getContextPath()
	{
		return path;
	}

	@Override
	public ServletContext getContext(final String uripath)
	{
		return new CWServletContext(config, uripath);
	}

	@Override
	public int getMajorVersion()
	{
		String version = config.getCoffeeWebServer().getVersion();
		final int x = version.indexOf('.');
		if(x>0)
			version = version.substring(0,x);
		try
		{
			return Integer.parseInt(version.trim());
		}
		catch(final Exception e)
		{
			return 0;
		}
	}

	@Override
	public int getMinorVersion()
	{
		String version = config.getCoffeeWebServer().getVersion();
		int x = version.indexOf('.');
		if(x>0)
			version = version.substring(0,x).trim();
		else
			return 0;
		x = version.indexOf('.');
		if(x>0)
			version = version.substring(0,x);
		try
		{
			return Integer.parseInt(version.trim());
		}
		catch(final Exception e)
		{
			return 0;
		}
	}

	@Override
	public int getEffectiveMajorVersion()
	{
		return getMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion()
	{
		return getMinorVersion();
	}

	@Override
	public String getMimeType(final String file)
	{
		if (file == null)
			return null;
		return MIMEType.All.getMIMEType(file).getType();
	}

	@Override
	public Set<String> getResourcePaths(final String path)
	{
		if (path == null)
			return null;
		final Pair<String,String> mountPath = config.getMount("", -1, path);
		if (mountPath != null)
		{
			String newFullPath = path.substring(mountPath.first.length());
			if (newFullPath.startsWith("/") && mountPath.second.endsWith("/"))
				newFullPath = newFullPath.substring(1);
			final String realPath = mountPath.second + newFullPath;
			final FileManager mgr = config.getFileManager();
			final File dir = mgr.createFileFromPath(realPath.replace('/', mgr.getFileSeparator()));
			if (dir == null || !dir.isDirectory())
				return null;
			final File[] files = dir.listFiles();
			if (files == null)
				return null;
			final Set<String> paths = new java.util.HashSet<String>();
			for (final File file : files)
			{
				String filePath = path;
				if (!filePath.endsWith("/"))
					filePath += "/";
				filePath += file.getName();
				if (file.isDirectory())
					filePath += "/";
				paths.add(filePath);
			}
			return paths;
		}
		return null;
	}

	@Override
	public URL getResource(final String path) throws MalformedURLException
	{
		if (path == null)
			return null;
		final Pair<String,String> mountPath = config.getMount("", -1, path);
		if (mountPath != null)
		{
			String newFullPath = path.substring(mountPath.first.length());
			if (newFullPath.startsWith("/") && mountPath.second.endsWith("/"))
				newFullPath = newFullPath.substring(1);
			final String realPath = mountPath.second + newFullPath;
			final FileManager mgr = config.getFileManager();
			final File file = mgr.createFileFromPath(realPath.replace('/', mgr.getFileSeparator()));
			if (file != null && file.exists())
				return file.toURI().toURL();
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final String path)
	{
		try
		{
			return config.getFileManager().getFileStream(config.getFileManager().createFileFromPath(path));
		}
		catch (final Exception e)
		{
			return null;
		}
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path)
	{
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(final String name)
	{
		return null;
	}

	@Override
	public Servlet getServlet(final String name) throws ServletException
	{
		final SimpleServlet servlet = config.getServletMan().findServlet(name);
		if (servlet == null)
			return null;
		if (servlet instanceof CWServlet)
			return ((CWServlet)servlet).getInternalServlet();
		return null;
	}

	@Override
	public Enumeration<Servlet> getServlets()
	{
		final java.util.List<Servlet> servlets = new java.util.ArrayList<Servlet>();
		for (final SimpleServlet s : config.getServletMan().getServlets())
		{
			if (s instanceof CWServlet)
			{
				final Servlet servlet = ((CWServlet)s).getInternalServlet();
				if (servlet != null)
					servlets.add(servlet);
			}
		}
		return new IteratorEnumeration<Servlet>(servlets.iterator());
	}

	@Override
	public Enumeration<String> getServletNames()
	{
		final java.util.List<String> names = new java.util.ArrayList<String>();
		for (final SimpleServlet s : config.getServletMan().getServlets())
		{
			if (s instanceof CWServlet)
			{
				final Servlet servlet = ((CWServlet)s).getInternalServlet();
				if (servlet != null)
				{
					final String name = servlet.getServletConfig().getServletName();
					if (name != null)
						names.add(name);
				}
			}
		}
		return new IteratorEnumeration<String>(names.iterator());

	}

	@Override
	public void log(final String msg)
	{
		config.getLogger().log(Level.INFO, msg);
	}

	@Override
	public void log(final Exception exception, final String msg)
	{
		config.getLogger().log(Level.SEVERE, msg, exception);
	}

	@Override
	public void log(final String message, final Throwable throwable)
	{
		config.getLogger().log(Level.SEVERE, message, throwable);
	}

	@Override
	public String getRealPath(final String path)
	{
		if (path == null)
			return null;
		final Pair<String,String> mountPath = config.getMount("", -1, path);
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
	public String getServerInfo()
	{
		return "CoffeeWebServer/" + config.getCoffeeWebServer().getVersion();
	}

	@Override
	public String getInitParameter(final String name)
	{
		return config.getServletMan().getServletInitVariables(path).get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames()
	{
		return new IteratorEnumeration<String>(config.getServletMan().getServletInitVariables(path).keySet().iterator());
	}

	@Override
	public boolean setInitParameter(final String name, final String value)
	{
		config.getINIFile().put(name, value);
		return true;
	}

	@Override
	public Object getAttribute(final String name)
	{
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return new IteratorEnumeration<String>(attributes.keySet().iterator());
	}

	@Override
	public void setAttribute(final String name, final Object object)
	{
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(final String name)
	{
		attributes.remove(name);
	}

	@Override
	public String getServletContextName()
	{
		if (path != null && path.length() > 0)
		{
			if (path.startsWith("/"))
				return path.substring(1);
			return path;
		}
		return "CoffeeWebServer";
	}

	@Override
	public Dynamic addServlet(final String servletName, final String className)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(final String servletName, final Servlet servlet)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(final String servletName, final Class<? extends Servlet> servletClass)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Dynamic addJspFile(final String servletName, final String jspFile)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <T extends Servlet> T createServlet(final Class<T> clazz) throws ServletException
	{
		try
		{
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			throw new ServletException("Unable to create servlet instance", e);
		}
	}

	@Override
	public ServletRegistration getServletRegistration(final String servletName)
	{
		if(config.getServletMan().findServlet(servletName) != null)
			return new CWServletRegistration(config, servletName);
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations()
	{
		final Map<String, ServletRegistration> regs = new Hashtable<String, ServletRegistration>();
		for(final String name : config.getServletMan().getServletPaths())
			regs.put(name, new CWServletRegistration(config, name));
		return regs;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(final String filterName, final String className)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(final String filterName, final Filter filter)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(final String filterName, final Class<? extends Filter> filterClass)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <T extends Filter> T createFilter(final Class<T> clazz) throws ServletException
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public FilterRegistration getFilterRegistration(final String filterName)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations()
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig()
	{
		return null;
	}

	@Override
	public void setSessionTrackingModes(final Set<SessionTrackingMode> sessionTrackingModes)
	{
		// dp what now?
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
	{
		final Set<SessionTrackingMode> modes = new java.util.HashSet<SessionTrackingMode>();
		modes.add(SessionTrackingMode.COOKIE);
		return modes;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
	{
		return getDefaultSessionTrackingModes();
	}

	@Override
	public void addListener(final String className)
	{
	}

	@Override
	public <T extends EventListener> void addListener(final T t)
	{
	}

	@Override
	public void addListener(final Class<? extends EventListener> listenerClass)
	{
	}

	@Override
	public <T extends EventListener> T createListener(final Class<T> clazz) throws ServletException
	{
		try
		{
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			throw new ServletException("Unable to create listener instance", e);
		}
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor()
	{
		return null;
	}

	@Override
	public ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public void declareRoles(final String... roleNames)
	{
	}

	@Override
	public String getVirtualServerName()
	{
		return null;
	}

	@Override
	public int getSessionTimeout()
	{
		return (int)(config.getMaxThreadIdleMs()/1000L/60L);
	}

	@Override
	public void setSessionTimeout(final int sessionTimeout)
	{
	}

	@Override
	public String getRequestCharacterEncoding()
	{
		return reqCharEnc;
	}

	@Override
	public void setRequestCharacterEncoding(final String encoding)
	{
		reqCharEnc = encoding;
	}

	@Override
	public String getResponseCharacterEncoding()
	{
		return respCharEnc;
	}

	@Override
	public void setResponseCharacterEncoding(final String encoding)
	{
		respCharEnc = encoding;
	}
}
