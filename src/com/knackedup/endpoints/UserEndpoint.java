package com.knackedup.endpoints;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.knackedup.PMF;
import com.knackedup.constant.ErrorMessage;
import com.knackedup.entity.Ids;
import com.knackedup.entity.Member;
import com.knackedup.entity.Quickie;
import com.knackedup.error.BadDataException;
import com.knackedup.error.GenericServiceException;
import com.knackedup.error.InvalidDataException;
import com.knackedup.error.QuickieNotFoundException;
import com.knackedup.error.UserNotFoundException;
import com.knackedup.util.ValidationUtil;

@Api(name = "knackedupapp", version = "v1", clientIds = { Ids.WEB_CLIENT_ID })
public class UserEndpoint
{
	private static final String	DEFAULT_LIMIT	= "100";
	private static final Logger	LOG				= Logger.getLogger(UserEndpoint.class.getName());

	private static PersistenceManager getPersistenceManager()
	{
		return PMF.get().getPersistenceManager();
	}

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP GET method.
	 * 
	 * @return List of all entities persisted.
	 * @throws GenericServiceException
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	@ApiMethod(name = "user.quickie.list", path = "user.quickies")
	public List<Quickie> listUserQuickies(@Nullable @Named("limit") String limit, @Nullable @Named("order") String order, User user) throws OAuthRequestException, IOException, GenericServiceException
	{
		PersistenceManager pm = getPersistenceManager();
		Query query = pm.newQuery(Quickie.class);
		List<Quickie> quickies = null;
		try
		{
			if (user == null)
			{
				throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
			}
			String userId = user.getUserId();
			query.setFilter("sponsorId == userIdParam");
			query.declareParameters("String userIdParam");
			query.setRange(0, new Long(DEFAULT_LIMIT));
			quickies = (List<Quickie>) pm.newQuery(query).execute(userId);
		}
		catch (javax.jdo.JDOObjectNotFoundException ex)
		{
			throw new GenericServiceException();
		}
		finally
		{
			query.closeAll();
			pm.close();
		}
		return quickies;
	}

	@ApiMethod(name = "user.get")
	public Member get(@Named("googleId") String googleId, User user) throws GenericServiceException, QuickieNotFoundException, OAuthRequestException, UserNotFoundException
	{
		// TODO should you be required to be the owner of the quickie?
		if (user == null)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
		}
		PersistenceManager pm = getPersistenceManager();
		Member member = null;
		Query query = pm.newQuery(Member.class);
		try
		{
			query.setFilter("googleId == googleIdParam");
			query.declareParameters("String googleIdParam");
			query.setUnique(true);
			member = (Member) query.execute(googleId);
			if (member == null)
			{
				throw new UserNotFoundException();
			}
		}
		catch (UserNotFoundException ex)
		{
			LOG.severe("User not found");
			throw ex;
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw e;
		}
		finally
		{
			if (!pm.isClosed())
			{
				pm.close();
			}
			if (query != null)
			{
				query.closeAll();
			}
		}
		return member;
	}

	@ApiMethod(name = "user.save")
	public Member save(@Named("accessToken") String accessToken, User user) throws Exception
	{
		Member orgMember = null;
		Member member = null;
		try
		{
			UserGetHelper userGetHelper = new UserGetHelper();
			member = userGetHelper.fetchUserProfile(accessToken);
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw e;
		}
		PersistenceManager pm = getPersistenceManager();
		Query query = pm.newQuery(Member.class);
		try
		{
			String googleId = member.getGoogleId();
			query.setFilter("googleId == googleIdParam");
			query.declareParameters("String googleIdParam");
			query.setUnique(true);
			orgMember = (Member) query.execute(googleId);
			if (orgMember != null && member != null)
			{
				member = Member.Merge(orgMember, member);
			}
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw e;
		}
		pm.currentTransaction().begin();
		try
		{
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			Set<ConstraintViolation<Member>> violations = validator.validate(member, Default.class);
			if (violations.isEmpty() == false)
			{
				ValidationUtil.logExceptions(violations);
				throw new InvalidDataException();
			}
			pm.makePersistent(member);
			pm.flush();
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
			throw e;
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
			if (query != null)
			{
				query.closeAll();
			}
		}
		return member;
	}
}
