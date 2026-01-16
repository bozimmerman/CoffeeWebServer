package com.planet_ink.coffee_web.j2ee;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.planet_ink.coffee_web.interfaces.SimpleServletSession;
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
@SuppressWarnings("deprecation")
public class CWHttpSession implements HttpSession
{
	final SimpleServletSession sess;
	final CWConfig config;
	final String path;

	public CWHttpSession(final String path, final CWConfig config, final SimpleServletSession sess)
	{
		this.path=path;
		this.config=config;
		this.sess = sess;
	}
	@Override
	public long getCreationTime()
	{
		return this.sess.getSessionStart().getTime();
	}

	@Override
	public String getId()
	{
		return this.sess.getSessionId();
	}

	@Override
	public long getLastAccessedTime()
	{
		return this.sess.getSessionLastTouchTime();
	}

	@Override
	public ServletContext getServletContext()
	{
		return new CWServletContext(config, path);
	}

	@Override
	public void setMaxInactiveInterval(final int interval)
	{
		sess.setIdleExpirationInterval(interval * 1000L);
	}

	@Override
	public int getMaxInactiveInterval()
	{
		return (int)(sess.getIdleExpirationInterval()/1000);
	}

	@Override
	public HttpSessionContext getSessionContext()
	{
		return new CWHttpSessionContext(path,config,sess.getManager());
	}

	@Override
	public Object getAttribute(final String name)
	{
		return this.sess.getSessionObject(name);
	}

	@Override
	public Object getValue(final String name)
	{
		return this.sess.getSessionObject(name);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return this.sess.getSessionObjects();
	}

	@Override
	public String[] getValueNames()
	{
		final ArrayList<String> names = new ArrayList<String>();
		for(final Enumeration<String> s = this.sess.getSessionObjects(); s.hasMoreElements();)
			names.add(s.nextElement());
		return names.toArray(new String[names.size()]);
	}

	@Override
	public void setAttribute(final String name, final Object value)
	{
		sess.setSessionObject(name, value);
	}

	@Override
	public void putValue(final String name, final Object value)
	{
		sess.setSessionObject(name, value);
	}

	@Override
	public void removeAttribute(final String name)
	{
		sess.setSessionObject(name, null);

	}

	@Override
	public void removeValue(final String name)
	{
		sess.setSessionObject(name, null);
	}

	@Override
	public void invalidate()
	{
		sess.setIdleExpirationInterval(0);
	}

	@Override
	public boolean isNew()
	{
		return sess.getSessionLastTouchTime() - sess.getSessionStart().getTime() < 100;
	}

}
