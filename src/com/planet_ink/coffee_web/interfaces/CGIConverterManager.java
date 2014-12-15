package com.planet_ink.coffee_web.interfaces;

import java.util.Collection;

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
 * Interface for an http response converter manager, based on the extension 
 * of the file returned.
 * 
 * @author Bo Zimmerman
 *
 */
public interface CGIConverterManager
{
	/**
	 * Internal method to register a converter existence, and its mime type.
	 * @param extension the extension the cgi converter responds to
	 * @param converterObject the converter
	 */
	public void registerConverter(String extension, HTTPOutputConverter converterObject);
	
	/**
	 * For anyone externally interested, will return the list of converter objects
	 * that are registered
	 * @return the list of converter objects
	 */
	public Collection<HTTPOutputConverter> getConverters();

	/**
	 * Returns a converter (if any) that handles the given extension.
	 * if none is found, NULL is returned.
	 * @param extension the extension
	 * @return the converter object, if any, or null
	 */
	public HTTPOutputConverter findConverter(String extension);

	/**
	 * Returns a statistics object for the given converter class
	 * or null if none exists
	 * @param converterObject the converter object managed by this web server
	 * @return the converter stats object
	 */
	public RequestStats getConverterStats(Class<? extends HTTPOutputConverter> converterObject);
}
