package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class UserNotFoundException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public UserNotFoundException()
	{
		super(400, "User requested could not be found");
	}
}
