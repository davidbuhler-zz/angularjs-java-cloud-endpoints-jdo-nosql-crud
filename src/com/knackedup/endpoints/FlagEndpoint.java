package com.knackedup.endpoints;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.knackedup.PMF;
import com.knackedup.constant.AdminProperties;
import com.knackedup.constant.ErrorMessage;
import com.knackedup.entity.Flag;
import com.knackedup.entity.Ids;
import com.knackedup.error.GenericServiceException;
import com.knackedup.util.MailUtil;

@Api(name = "knackedupapp", version = "v1", clientIds = { Ids.WEB_CLIENT_ID })
public class FlagEndpoint
{
	private static PersistenceManager getPersistenceManager()
	{
		return PMF.get().getPersistenceManager();
	}

	private static final Logger	LOG		= Logger.getLogger(FlagEndpoint.class.getName());
	
	@ApiMethod(name = "flag.insert", path = "quickie/{quickieId}/details")
	public Flag insert(Flag flag) throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		try
		{
			MailUtil.doSimpleMail(AdminProperties.EMAIL, "Quickie Flagged",  "A quickie has been flagged as inappropriate.", false);
			pm.makePersistent(flag);
			pm.flush();
			pm.currentTransaction().commit();
		}
		catch (Exception e)
		{
			LOG.severe(e.getMessage());
			throw new GenericServiceException();
		}
		finally
		{
			if (pm.currentTransaction().isActive())
			{
				pm.currentTransaction().rollback();
			}
			if (!pm.isClosed())
			{
				pm.close();
			}
		}
		return flag;
	}
}
