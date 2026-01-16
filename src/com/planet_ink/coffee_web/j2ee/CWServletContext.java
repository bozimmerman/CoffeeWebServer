package com.planet_ink.coffee_web.j2ee;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
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
	private final CWConfig config;
	private final String path;

	public CWServletContext(final CWConfig config, final String path)
	{
		this.config=config;
		this.path = path;
	}

	@Override
	public String getContextPath()
	{
		return path;
	}

	@Override
	public ServletContext getContext(final String uripath)
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getResourcePaths(final String path)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(final String path) throws MalformedURLException
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(final String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Servlet getServlet(final String name) throws ServletException
	{
		final SimpleServlet servlet = config.getServletMan().findServlet(name);
		if(servlet == null)
			return null;
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Servlet> getServlets()
	{
		//config.getServletMan().getServlets().iterator();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getServletNames()
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(final String name)
	{
		return config.getMiscProp(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames()
	{
		final Vector<String> v = new Vector<String>();
		for(final Enumeration<Object> e = config.getINIFile().keys();e.hasMoreElements();)
			v.add(e.toString());
		return v.elements();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(final String name, final Object object)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(final String name)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getServletContextName()
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(final String servletName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations()
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionTrackingModes(final Set<SessionTrackingMode> sessionTrackingModes)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(final String className)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> void addListener(final T t)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(final Class<? extends EventListener> listenerClass)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> T createListener(final Class<T> clazz) throws ServletException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor()
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getVirtualServerName()
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getRequestCharacterEncoding()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRequestCharacterEncoding(final String encoding)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getResponseCharacterEncoding()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResponseCharacterEncoding(final String encoding)
	{
		// TODO Auto-generated method stub

	}
}
