package com.knackedup.entity;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.appengine.api.users.User;
import com.knackedup.constant.MaxLength;
import com.knackedup.constant.MinLength;
import com.knackedup.constant.MsgFrags;
import com.knackedup.constant.Resource;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Quickie implements StoreCallback
{
	public static Quickie Merge(Quickie orgQuickie, Quickie newQuickie)
	{
		if (newQuickie.getQuickieName() != null)
		{
			orgQuickie.setQuickieName(newQuickie.getQuickieName());
			orgQuickie.setTermsAccepted(newQuickie.hasAcceptedTerms());
			orgQuickie.setDescription(newQuickie.getDescription());
			orgQuickie.setOrganizationName(newQuickie.getOrganization());
			orgQuickie.setProfileURL(newQuickie.getProfileURL());
		}
		return orgQuickie;
	}

	private Date	createdOn;
	@NotEmpty(message = Resource.DESCRIPTION + MsgFrags.REQUIRED)
	@NotNull(message = Resource.DESCRIPTION + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.DESCRIPTION + MsgFrags.REQUIRED)
	@Length(min = MinLength.PROJECT_DESCRIPTION, max = MaxLength.PROJECT_DESCRIPTION, message = Resource.DESCRIPTION + MsgFrags.EXCEED + MinLength.PROJECT_DESCRIPTION + MsgFrags.CHARS)
	private String	description;
	
	@NotEmpty(message = Resource.ORGANIZATION + MsgFrags.REQUIRED)
	@NotNull(message = Resource.ORGANIZATION + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.ORGANIZATION + MsgFrags.REQUIRED)
	@Length(min = MinLength.ORGANIZATION, max = MaxLength.ORGANIZATION, message = Resource.ORGANIZATION + MsgFrags.ABSOLUTE + MinLength.ORGANIZATION + MsgFrags.CHARS)
	private String	organization;
	
	@NotEmpty(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@NotNull(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@Length(min = MinLength.PROFILE_URL, max = MaxLength.PROFILE_URL, message = Resource.PROFILE_URL + MsgFrags.ABSOLUTE + MinLength.PROFILE_URL + MsgFrags.CHARS)
	private String	profileURL;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long	quickieId;
	
	@NotEmpty(message = Resource.QUICKIE_NAME + MsgFrags.REQUIRED)
	@NotNull(message = Resource.QUICKIE_NAME + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.QUICKIE_NAME + MsgFrags.REQUIRED)
	@Length(min = MinLength.PROJECT_NAME, max = MaxLength.PROJECT_NAME, message = Resource.QUICKIE_NAME + MsgFrags.ABSOLUTE + MinLength.PROJECT_NAME + MsgFrags.CHARS)
	private String	quickieName;

	@NotEmpty(message = Resource.USER_ID + MsgFrags.REQUIRED)
	@NotNull(message = Resource.USER_ID + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.USER_ID + MsgFrags.REQUIRED)
	private String	sponsorId;
	
	@AssertTrue
	private boolean	termsAccepted;


	@SuppressWarnings("unused")
	private Date	updatedOn;
	public String getUserId()
	{
		return sponsorId;
	}

	public void setSponsorId(String value)
	{
		this.sponsorId = value;
	}
	
	public String getDescription()
	{
		return description;
	}

	public String getOrganization()
	{
		return organization;
	}

	public String getProfileURL()
	{
		return profileURL;
	}
	
	public Long getQuickieId()
	{
		return quickieId;
	}
	public String getQuickieName()
	{
		return quickieName;
	}

	public boolean hasAcceptedTerms()
	{
		return termsAccepted;
	}

	@Override
	public void jdoPreStore()
	{
		Date now = new Date();
		updatedOn = now;
		if (createdOn == null)
		{
			createdOn = now;
		}
		if (quickieName != null)
		{
			quickieName = quickieName.trim();
		}
		if (description != null)
		{
			description = description.trim();
		}
		if (profileURL != null)
		{
			profileURL = profileURL.trim();
		}
	}

	public void setDescription(String value)
	{
		this.description = value;
	}

	public void setOrganizationName(String value)
	{
		this.organization = value;
	}

	public void setProfileURL(String value)
	{
		this.profileURL = value;
	}

	public void setQuickieName(String value)
	{
		this.quickieName = value;
	}

	public void setTermsAccepted(boolean value)
	{
		this.termsAccepted = value;
	}
}
