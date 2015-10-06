package com.knackedup.error;

import java.io.Serializable;

import com.google.api.server.spi.ServiceException;
 

public class PropositionNotFoundException extends ServiceException implements Serializable
{
	private static final long	serialVersionUID	= 1L;	
	public PropositionNotFoundException()
	{
		super(400, "Proposition requested could not be found");
	}
}
