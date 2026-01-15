package com.planet_ink.coffee_web.j2ee;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.planet_ink.coffee_web.interfaces.ServletSessionManager;
import com.planet_ink.coffee_web.interfaces.SimpleServletSession;

public class CWHttpSessionContext implements HttpSessionContext
{
	final ServletSessionManager mgr;

	public CWHttpSessionContext(final ServletSessionManager mgr)
	{
		this.mgr = mgr;
	}

	@Override
	public HttpSession getSession(final String sessionId)
	{
		final SimpleServletSession sess = this.mgr.findSession(sessionId);
		if(sess != null)
			return new CWHttpSession(sess);
		return null;
	}

	@Override
	public Enumeration<String> getIds()
	{
		return mgr.getSessionIds();
	}

}
