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
public class Proposition implements StoreCallback
{
	private Date	createdOn;
	
	 
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Proposition [createdOn=");
		builder.append(createdOn);
		builder.append(", knack=");
		builder.append(knack);
		builder.append(", pitchId=");
		builder.append(pitchId);
		builder.append(", profileURL=");
		builder.append(profileURL);
		builder.append(", quickieId=");
		builder.append(quickieId);
		builder.append(", termsAccepted=");
		builder.append(termsAccepted);
		builder.append(", title=");
		builder.append(title);
		builder.append(", updatedOn=");
		builder.append(updatedOn);
		builder.append(", suitorId=");
		builder.append(suitorId);
		builder.append("]");
		return builder.toString();
	}

	 

	@NotEmpty(message = Resource.KNACK + MsgFrags.REQUIRED)
	@NotNull(message = Resource.KNACK + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.KNACK + MsgFrags.REQUIRED)
	@Length(min = MinLength.KNACK, max = MaxLength.KNACK, message = Resource.KNACK + MsgFrags.ABSOLUTE + MinLength.KNACK + MsgFrags.CHARS)
	private String	knack;


	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long	pitchId;
	
	@NotEmpty(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@NotNull(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.PROFILE_URL + MsgFrags.REQUIRED)
	@Length(min = MinLength.PROFILE_URL, max = MaxLength.PROFILE_URL, message = Resource.PROFILE_URL + MsgFrags.ABSOLUTE + MinLength.PROFILE_URL + MsgFrags.CHARS)
	private String	profileURL;
	
	private Long	quickieId;
	
	@AssertTrue
	private boolean	termsAccepted;
	
	@NotEmpty(message = Resource.TITLE + MsgFrags.REQUIRED)
	@NotNull(message = Resource.TITLE + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.TITLE + MsgFrags.REQUIRED)
	@Length(min = MinLength.TITLE, max = MaxLength.TITLE, message = Resource.TITLE + MsgFrags.ABSOLUTE + MinLength.TITLE + MsgFrags.CHARS)
	private String	title;
	
	@SuppressWarnings("unused")
	private Date	updatedOn;
	
	@NotEmpty(message = Resource.USER_ID + MsgFrags.REQUIRED)
	@NotNull(message = Resource.USER_ID + MsgFrags.REQUIRED)
	@NotBlank(message = Resource.USER_ID + MsgFrags.REQUIRED)
	private String	suitorId;

	public String getSuitorId()
	{
		return suitorId;
	}

	public void setSuitorId(String value)
	{
		this.suitorId = value;
	}

	public String getKnack()
	{
		return knack;
	}

	public Long getPropositionId()
	{
		return pitchId;
	}

	public Long getQuickieId()
	{
		return quickieId;
	}

	public String getTitle()
	{
		return title;
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
		if (title != null)
		{
			title = title.trim();
		}
		if (knack != null)
		{
			knack = knack.trim();
		}
		if (profileURL != null)
		{
			profileURL = profileURL.trim();
		}
	}

	protected String getProfileURL()
	{
		return profileURL;
	}

	protected void setProfileURL(String profileURL)
	{
		this.profileURL = profileURL;
	}

	protected Date getCreatedOn()
	{
		return createdOn;
	}

	protected Date getUpdatedOn()
	{
		return updatedOn;
	}

	public void setKnack(String value)
	{
		this.knack = value;
	}

	public void setQuickieId(Long value)
	{
		this.quickieId = value;
	}

	public void setTermsAccepted(boolean value)
	{
		this.termsAccepted = value;
	}

	public void setTitle(String value)
	{
		this.title = value;
	}

}
