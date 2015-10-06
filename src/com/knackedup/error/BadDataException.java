package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class BadDataException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public BadDataException()
	{
		super(400, "The data was invalid");
	}
}
