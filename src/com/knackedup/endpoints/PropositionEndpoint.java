package com.knackedup.endpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.knackedup.PMF;
import com.knackedup.constant.ErrorMessage;
import com.knackedup.entity.Ids;
import com.knackedup.entity.Member;
import com.knackedup.entity.Proposition;
import com.knackedup.error.BadDataException;
import com.knackedup.error.GenericServiceException;
import com.knackedup.error.InvalidDataException;
import com.knackedup.error.NotEntityOwnerException;
import com.knackedup.error.PropositionNotFoundException;
import com.knackedup.util.ValidationUtil;

@Api(name = "knackedupapp", version = "v1", clientIds = { Ids.WEB_CLIENT_ID })
public class PropositionEndpoint
{
	private static final Logger	LOG	= Logger.getLogger(PropositionEndpoint.class.getName());

	@SuppressWarnings({ "cast", "unchecked" })
	@ApiMethod(name = "proposition.list", path = "quickie/{quickieId}/propositions")
	public CollectionResponse<Proposition> list(@Named("quickieId") Long quickieId, @Nullable @Named("cursor") String cursorString, @Nullable @Named("limit") Integer limit) throws GenericServiceException
	{
		PersistenceManager pm = null;
		Cursor cursor = null;
		List<Proposition> execute = null;
		try
		{
			pm = getPersistenceManager();
			Query query = pm.newQuery(Proposition.class, "quickieId == " + quickieId);
			if (cursorString != null && cursorString != "")
			{
				cursor = Cursor.fromWebSafeString(cursorString);
				Map<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}
			if (limit != null)
			{
				query.setRange(0, limit);
			}
			execute = (List<Proposition>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();
			else
				cursorString = "";
			// Tight loop for fetching all entities from datastore and accomodate for lazy fetch.
			for (@SuppressWarnings("unused")
			Proposition e : execute)
				;
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new GenericServiceException();
		}
		finally
		{
			pm.close();
		}
		return CollectionResponse.<Proposition> builder().setItems(execute).setNextPageToken(cursorString).build();
	}
	
	@ApiMethod(name = "proposition.get", path = "quickie/{quickieId}/proposition/{id}")
	public Proposition get(@Named("propositionId") Long propositionId) throws GenericServiceException, OAuthRequestException, PropositionNotFoundException
	{
		PersistenceManager pm = getPersistenceManager();
		Proposition proposition = null;
		try
		{
			proposition = pm.getObjectById(Proposition.class, propositionId);
		}
		catch (JDOObjectNotFoundException e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new PropositionNotFoundException();
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new GenericServiceException();
		}
		finally
		{
			pm.close();
		}
		return proposition;
	}

	@ApiMethod(name = "proposition.insert", path = "quickie/{quickieId}/proposition")
	public Proposition insert(Proposition proposition, User user) throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		try
		{
			if (user == null)
			{
				throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
			}
			proposition.setSuitorId(user.getUserId());
			LOG.info(proposition.toString());
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			Set<ConstraintViolation<Proposition>> violations = validator.validate(proposition, Default.class);
			if (violations.isEmpty() == false)
			{
				ValidationUtil.logExceptions(violations);
				throw new InvalidDataException();
			}
			pm.makePersistent(proposition);
			pm.currentTransaction().commit();
		}
		catch (InvalidDataException e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new BadDataException();
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
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
		return proposition;
	}

	@ApiMethod(name = "proposition.delete", path = "quickie/{quickieId}/proposition/{propositionId}")
	public Proposition remove(@Named("quickieId") Long quickieId, @Named("propositionId") Long id, User user) throws OAuthRequestException, NotEntityOwnerException, GenericServiceException
	{
		if (user == null)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
		}
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		Proposition proposition = null;
		try
		{
			proposition = pm.getObjectById(Proposition.class, id);
			if (proposition.getSuitorId().equals(user.getUserId()) == false)
			{
				throw new NotEntityOwnerException();
			}
			pm.deletePersistent(proposition);
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
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
		return proposition;
	}

	private static PersistenceManager getPersistenceManager()
	{
		return PMF.get().getPersistenceManager();
	}
}
