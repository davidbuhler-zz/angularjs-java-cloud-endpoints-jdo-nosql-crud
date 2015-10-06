package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class QuickieNotFoundException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public QuickieNotFoundException()
	{
		super(400, "Quickie requested could not be found");
	}
}
