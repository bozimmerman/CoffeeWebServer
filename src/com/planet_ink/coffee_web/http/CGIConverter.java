package com.planet_ink.coffee_web.http;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.coffee_web.interfaces.CGIConverterManager;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.RequestStats;

/*
Copyright 2012-2014 Bo Zimmerman

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

/**
 * Manages a relatively static set of converter classes 
 * and the root contexts needed to access them.
 * 
 * @author Bo Zimmerman
 *
 */
public class CGIConverter implements CGIConverterManager
{
	private final Map<String,HTTPOutputConverter>		converters;   // map of registered converters by context
	private final Map<HTTPOutputConverter, RequestStats>requestStats; // stats about each converter
	
	/**
	 * Construct a cgi config manager, loading the converters from the config given
	 * @param config
	 */
	public CGIConverter(CWConfig config)
	{
		converters = new Hashtable<String,HTTPOutputConverter>();
		requestStats = new Hashtable<HTTPOutputConverter, RequestStats>();
		for(final String extension : config.getCGIs().keySet())
		{
			final String fileName=config.getCGIs().get(extension);
			if(new File(fileName).exists())
			{
				registerConverter(extension.toLowerCase().trim(), new com.planet_ink.coffee_web.converters.CGIConverter(fileName));
			}
			else
			{
				config.getLogger().severe("CGI Manager can't find "+fileName);
			}
		}
	}
	
	
	/**
	 * Internal method to register a converters existence, and its context.
	 * @param extension the extension the cgi converter converts
	 * @param converterClass the class of the converter
	 */
	@Override
	public void registerConverter(String extension, HTTPOutputConverter converterObject)
	{
		converters.put(extension, converterObject);
		requestStats.put(converterObject, new RequestStats());
	}
	
	/**
	 * For anyone externally interested, will return the list of converter objects
	 * that are registered
	 * @return the list of converter classes
	 */
	@Override
	public Collection<HTTPOutputConverter> getConverters()
	{
		return converters.values();
	}

	/**
	 * Returns a converter (if any) that handles the given extension.
	 * if none is found, NULL is returned.
	 * @param extension the extension
	 * @return the converter object, if any, or null
	 */
	@Override
	public HTTPOutputConverter findConverter(String extension)
	{
		return converters.get(extension);
	}

	/**
	 * Returns a statistics object for the given converter object
	 * or null if none exists
	 * @param converterObject the converter object managed by this web server
	 * @return the converter stats object
	 */
	@Override
	public RequestStats getConverterStats(Class<? extends HTTPOutputConverter> converterObject)
	{
		return requestStats.get(converterObject);
	}
}
