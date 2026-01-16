package com.planet_ink.coffee_web.j2ee;

import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.planet_ink.coffee_common.collections.IteratorEnumeration;
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
public class CWServletConfig implements ServletConfig
{
	final Servlet servlet;
	final CWConfig config;
	final String path;

	public CWServletConfig(final CWConfig config, final String path, final Servlet servlet)
	{
		this.config=config;
		this.servlet=servlet;
		this.path = path;
	}

	@Override
	public String getServletName()
	{
		return servlet.getClass().getCanonicalName();
	}

	@Override
	public ServletContext getServletContext()
	{
		return new CWServletContext(config, path);
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

}
