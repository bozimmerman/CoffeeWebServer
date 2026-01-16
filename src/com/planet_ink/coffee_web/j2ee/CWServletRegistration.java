package com.planet_ink.coffee_web.j2ee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRegistration;

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
public class CWServletRegistration implements ServletRegistration
{

	private final CWConfig config;
	private final String path;

	public CWServletRegistration(final CWConfig config, final String path)
	{
		this.config=config;
		this.path=path;
	}
	@Override
	public String getName()
	{
		return path;
	}

	@Override
	public String getClassName()
	{
		return config.getServletMan().findServlet(path).getClass().getCanonicalName();
	}

	@Override
	public boolean setInitParameter(final String name, final String value)
	{
		if(config.getServletMan().getServletInitVariables(path).containsKey(name))
			return false;
		config.getServletMan().getServletInitVariables(path).put(name, value);
		return true;
	}

	@Override
	public String getInitParameter(final String name)
	{
		return config.getServletMan().getServletInitVariables(path).get(name);
	}

	@Override
	public Set<String> setInitParameters(final Map<String, String> initParameters)
	{
		final Set<String> res = new HashSet<String>();
		for(final String key : initParameters.keySet())
			if(!setInitParameter(key, initParameters.get(key)))
				res.add(key);
		return res;
	}

	@Override
	public Map<String, String> getInitParameters()
	{
		return config.getServletMan().getServletInitVariables(path);
	}

	@Override
	public Set<String> addMapping(final String... urlPatterns)
	{
		final Set<String> collisions = new HashSet<String>();
		if(urlPatterns == null)
			return collisions;
		for(final String patt : urlPatterns)
		{
			if(config.getServletMan().findServlet(patt) != null)
				collisions.add(patt);
			else
				config.getServletMan().registerServlet(patt, config.getServletMan().findServlet(path));
		}
		return collisions;
	}

	@Override
	public Collection<String> getMappings()
	{
		final ArrayList<String> paths = new ArrayList<String>(1);
		paths.add(path);
		return paths;
	}

	@Override
	public String getRunAsRole()
	{
		return null;
	}

}
