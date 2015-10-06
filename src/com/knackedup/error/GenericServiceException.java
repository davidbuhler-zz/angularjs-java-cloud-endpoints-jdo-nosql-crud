package com.knackedup.error;

import com.google.api.server.spi.ServiceException;

public class GenericServiceException extends ServiceException
{
	private static final long	serialVersionUID	= 1L;

	public GenericServiceException()
	{
		super(400, "An unknown error has taken place");
	}
}
