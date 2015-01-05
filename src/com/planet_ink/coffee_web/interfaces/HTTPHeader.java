package com.planet_ink.coffee_web.interfaces;

public interface HTTPHeader 
{
	/**
	 * Return the default value for this header, if one is defined, or ""
	 * @return the default value of this header
	 */
	public String getDefaultValue();

	/**
	 * Return a lowercase form of this headers name as used in normalized map lookups
	 * @return lowercase name of this header
	 */
	public String lowerCaseName();
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(String value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(String value);
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(int value);
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(long value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(int value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(long value);
}
