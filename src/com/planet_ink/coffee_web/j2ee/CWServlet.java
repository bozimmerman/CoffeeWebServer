package com.planet_ink.coffee_web.j2ee;

import java.util.logging.Level;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;
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
public class CWServlet implements SimpleServlet
{
	private final Servlet servlet;
	private final String path;
	private final CWConfig config;

	public CWServlet(final String path, final CWConfig config, final Servlet servlet)
	{
		this.config=config;
		this.path = path;
		this.servlet = servlet;
	}

	public Servlet getInternalServlet()
	{
		return this.servlet;
	}

	@Override
	public void init()
	{
		try
		{
			servlet.init(new CWServletConfig(config, path, servlet));
		}
		catch (final ServletException e)
		{
			config.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void doGet(final SimpleServletRequest request, final SimpleServletResponse response) throws HTTPException
	{
	}

	@Override
	public void doPost(final SimpleServletRequest request, final SimpleServletResponse response) throws HTTPException
	{
	}

	@Override
	public void service(final HTTPMethod method, final SimpleServletRequest request, final SimpleServletResponse response)
			throws HTTPException
	{
		final ServletRequest i2eeReq = new CWHttpServletRequest(this.path,request);
		final ServletResponse i2eeResp = new CWHttpServletResponse(response);
		try
		{
			servlet.service(i2eeReq, i2eeResp);
		}
		catch(final Exception e)
		{
			throw new HTTPException(e);
		}
	}

}
