package com.knackedup.endpoints;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.persistence.EntityExistsException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.AdminDatastoreService.KeyBuilder;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.knackedup.PMF;
import com.knackedup.constant.AdminProperties;
import com.knackedup.constant.ErrorMessage;
import com.knackedup.entity.Ids;
import com.knackedup.entity.Member;
import com.knackedup.entity.Proposition;
import com.knackedup.entity.Quickie;
import com.knackedup.error.BadDataException;
import com.knackedup.error.GenericServiceException;
import com.knackedup.error.InvalidDataException;
import com.knackedup.error.QuickieNotFoundException;
import com.knackedup.util.MailUtil;
import com.knackedup.util.ValidationUtil;

@Api(name = "knackedupapp", version = "v1", clientIds = { Ids.WEB_CLIENT_ID })
public class QuickieEndpoint
{
	private static final Index	INDEX	= getIndex();
	private static final Logger	LOG		= Logger.getLogger(QuickieEndpoint.class.getName());

	private static void addQuickieToSearchIndex(Quickie quickie)
	{
		try
		{
			Document.Builder docBuilder = Document.newBuilder().addField(Field.newBuilder().setName("id").setText(Long.toString(quickie.getQuickieId()))).addField(Field.newBuilder().setName("quickieName").setText(quickie.getQuickieName() != null ? quickie.getQuickieName() : ""))
					.addField(Field.newBuilder().setName("description").setText(quickie.getDescription() != null ? quickie.getDescription() : "")).addField(Field.newBuilder().setName("organization").setText(quickie.getOrganization() != null ? quickie.getOrganization() : ""));
			docBuilder.setId(Long.toString(quickie.getQuickieId()));
			Document doc = docBuilder.build();
			INDEX.put(doc);
		}
		catch (PutException e)
		{
			throw e;
		}
	}

	private static Index getIndex()
	{
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("quickieindex").build();
		return SearchServiceFactory.getSearchService().getIndex(indexSpec);
	}

	private static PersistenceManager getPersistenceManager()
	{
		return PMF.get().getPersistenceManager();
	}

	private boolean containsQuickie(Quickie quickie)
	{
		PersistenceManager pm = getPersistenceManager();
		boolean contains = true;
		try
		{
			pm.getObjectById(Quickie.class, quickie.getQuickieId());
		}
		catch (javax.jdo.JDOObjectNotFoundException ex)
		{
			contains = false;
		}
		finally
		{
			pm.close();
		}
		return contains;
	}

	@ApiMethod(name = "quickie.get")
	public Quickie getQuickie(@Named("quickieId") Long quickieId) throws GenericServiceException, QuickieNotFoundException
	{
		PersistenceManager pm = getPersistenceManager();
		Quickie quickie = null;
		try
		{
			quickie = pm.getObjectById(Quickie.class, quickieId);
		}
		catch (JDOObjectNotFoundException e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new QuickieNotFoundException();
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
		return quickie;
	}

	@ApiMethod(name = "quickie.insert")
	public Quickie insertQuickie(Quickie quickie, User user) throws IOException, GenericServiceException, OAuthRequestException, BadDataException
	{
		if (user == null)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
		}
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		try
		{
			quickie.setSponsorId(user.getUserId());
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			Set<ConstraintViolation<Quickie>> violations = validator.validate(quickie, Default.class);
			if (violations.isEmpty() == false)
			{
				ValidationUtil.logExceptions(violations);
				throw new InvalidDataException();
			}
			pm.makePersistent(quickie);
			pm.flush();
			pm.currentTransaction().commit();
			addQuickieToSearchIndex(quickie);
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
		return quickie;
	}

	private boolean isQuickieOwner(Quickie quickie, User user)
	{
		LOG.info("sponor user id : " + quickie.getUserId());
		LOG.info("google user id : " + user.getUserId());
		return quickie.getUserId().equals(user.getUserId());
	}

	@SuppressWarnings({ "cast", "unchecked" })
	@ApiMethod(name = "quickie.list", path = "quickie")
	public CollectionResponse<Quickie> listQuickies(@Nullable @Named("cursor") String cursorString, @Nullable @Named("limit") Integer limit) throws GenericServiceException
	{
		PersistenceManager pm = null;
		Cursor cursor = null;
		List<Quickie> execute = null;
		try
		{
			pm = getPersistenceManager();
			Query query = pm.newQuery(Quickie.class);
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
			execute = (List<Quickie>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();
			else
				cursorString = "";
			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (@SuppressWarnings("unused")
			Quickie e : execute)
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
		return CollectionResponse.<Quickie> builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	@ApiMethod(name = "quickie.delete")
	public void removeQuickie(@Named("quickieId") Long quickieId, User user) throws GenericServiceException, OAuthRequestException
	{
		if (user == null)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
		}
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		try
		{
			Quickie quickie = pm.getObjectById(Quickie.class, quickieId);
			if (isQuickieOwner(quickie, user) == false)
			{
				throw new OAuthRequestException(ErrorMessage.USER_NOT_OWNER);
			}
			pm.deletePersistent(quickie);
			pm.currentTransaction().commit();
		}
		catch (Exception e)
		{
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
		}
	}

	@ApiMethod(httpMethod = "GET", name = "quickie.search")
	public List<Quickie> searchQuickie(@Named("term") String queryString) throws GenericServiceException
	{
		List<Quickie> quickieList = new ArrayList<Quickie>();
		try
		{
			Results<ScoredDocument> results = INDEX.search(queryString);
			for (ScoredDocument scoredDoc : results)
			{
				Field f = scoredDoc.getOnlyField("id");
				if (f == null || f.getText() == null)
					continue;
				long quickieId = Long.parseLong(f.getText());
				if (quickieId != -1)
				{
					Quickie b = getQuickie(quickieId);
					quickieList.add(b);
				}
			}
		}
		catch (Exception e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new GenericServiceException();
		}
		return quickieList;
	}

	@ApiMethod(name = "quickie.update")
	public Quickie updateQuickie(Quickie quickie, User user) throws IOException, GenericServiceException, OAuthRequestException, QuickieNotFoundException, BadDataException
	{
		if (user == null)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_AUTHENTICATED);
		}
		Quickie orgQuickie = null;
		try
		{
			orgQuickie = getQuickie(quickie.getQuickieId());
		}
		catch (QuickieNotFoundException e)
		{
			LOG.severe(e.getStackTrace().toString());
			throw new QuickieNotFoundException();
		}
		if (isQuickieOwner(orgQuickie, user) == false)
		{
			throw new OAuthRequestException(ErrorMessage.USER_NOT_OWNER);
		}
		PersistenceManager pm = getPersistenceManager();
		pm.currentTransaction().begin();
		try
		{
			quickie = Quickie.Merge(orgQuickie, quickie);
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			Set<ConstraintViolation<Quickie>> violations = validator.validate(quickie, Default.class);
			if (violations.isEmpty() == false)
			{
				ValidationUtil.logExceptions(violations);
				throw new InvalidDataException();
			}
			pm.makePersistent(quickie);
			pm.currentTransaction().commit();
			addQuickieToSearchIndex(quickie);
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
		return quickie;
	}
}
