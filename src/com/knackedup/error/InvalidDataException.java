package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class InvalidDataException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public InvalidDataException()
	{
		super(400, "The data supplied was invalid");
	}
}
