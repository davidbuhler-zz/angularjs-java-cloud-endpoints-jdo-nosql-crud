package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class NotEntityOwnerException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public NotEntityOwnerException()
	{
		super(400, "Error");
	}
}
