package com.planet_ink.coffee_web.j2ee;

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
public class CWCookie extends javax.servlet.http.Cookie
{
	private static final long serialVersionUID = 15555L;
	com.planet_ink.coffee_web.http.Cookie c;
	public CWCookie(final com.planet_ink.coffee_web.http.Cookie cookie)
	{
		super(cookie.name, cookie.value);
		this.c=cookie;
		this.setDomain(cookie.domain);
		this.setHttpOnly(cookie.httpOnly);
		this.setMaxAge((int)cookie.maxAge);
		this.setPath(cookie.path);
		this.setSecure(cookie.secure);
	}

	@Override
	public void setComment(final String purpose)
	{
		super.setComment(purpose);
	}

	@Override
	public void setDomain(final String domain)
	{
		super.setDomain(domain);
		c.domain = domain;
	}

	@Override
	public void setMaxAge(final int maxAge)
	{
		super.setMaxAge(maxAge);
		c.maxAge = maxAge;
	}

	@Override
	public void setHttpOnly(final boolean only)
	{
		super.setHttpOnly(only);
		c.httpOnly = only;
	}

	@Override
	public void setPath(final String path)
	{
		super.setPath(path);
		c.path = path;

	}

	@Override
	public void setSecure(final boolean secure)
	{
		super.setSecure(secure);
		c.secure = secure;
	}


	@Override
	public void setValue(final String value)
	{
		super.setValue(value);
		c.value = value;
	}
}
