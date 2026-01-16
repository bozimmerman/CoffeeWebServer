package com.planet_ink.coffee_web.j2ee;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.planet_ink.coffee_web.interfaces.ServletSessionManager;
import com.planet_ink.coffee_web.interfaces.SimpleServletSession;
import com.planet_ink.coffee_web.util.CWConfig;

@SuppressWarnings("deprecation")
public class CWHttpSessionContext implements HttpSessionContext
{
	final ServletSessionManager mgr;
	final CWConfig config;
	final String path;

	public CWHttpSessionContext(final String path, final CWConfig config, final ServletSessionManager mgr)
	{
		this.config=config;
		this.mgr = mgr;
		this.path = path;
	}

	@Override
	public HttpSession getSession(final String sessionId)
	{
		final SimpleServletSession sess = this.mgr.findSession(sessionId);
		if(sess != null)
			return new CWHttpSession(path,config,sess);
		return null;
	}

	@Override
	public Enumeration<String> getIds()
	{
		return mgr.getSessionIds();
	}

}
