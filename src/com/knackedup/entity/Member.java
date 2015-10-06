package com.knackedup.entity;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;
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
public class Member implements StoreCallback
{
	private Date	createdOn;
	
	@NotBlank(message = Resource.EMAIL + MsgFrags.REQUIRED)
	@NotEmpty(message = Resource.EMAIL + MsgFrags.REQUIRED)
	@NotNull(message = Resource.EMAIL + MsgFrags.REQUIRED)
	private String	email;
	
	@NotBlank(message = Resource.LAST_NAME + MsgFrags.REQUIRED)
	@NotEmpty(message = Resource.LAST_NAME + MsgFrags.REQUIRED)
	@NotNull(message = Resource.LAST_NAME + MsgFrags.REQUIRED)
	@Length(min = MinLength.LAST_NAME, max = MaxLength.LAST_NAME, message = Resource.LAST_NAME + MsgFrags.INVALID_LENGTH)
	private String	familyName;
	
	@NotBlank(message = com.knackedup.constant.Resource.FIRST_NAME + MsgFrags.REQUIRED)
	@NotEmpty(message = Resource.FIRST_NAME + MsgFrags.REQUIRED)
	@NotNull(message = Resource.FIRST_NAME + MsgFrags.REQUIRED)
	@Length(min = MinLength.FIRST_NAME, max = MaxLength.FIRST_NAME, message = Resource.FIRST_NAME + MsgFrags.INVALID_LENGTH)
	private String	givenName;
	
	@NotBlank(message = Resource.GOOGLE_ID + MsgFrags.REQUIRED)
	@NotEmpty(message = Resource.GOOGLE_ID + MsgFrags.REQUIRED)
	@NotNull(message = Resource.GOOGLE_ID + MsgFrags.REQUIRED)
	private String	googleId;
	private String	hd;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long	memberId;
	private String	name;
	private String	locale;
	private String	pictureURL;
	
	@SuppressWarnings("unused")
	private Date	updatedOn;
	private Boolean	verifiedEmail;

	public static Member Merge(Member orgMember, Member newMember)
	{
		orgMember.setEmail(newMember.getEmail());
		orgMember.setFamilyName(newMember.getFamilyName());
		orgMember.setGivenName(newMember.getGivenName());
		orgMember.setHd(newMember.getHd());
		orgMember.setName(newMember.getName());
		orgMember.setLocale(newMember.getLocale());
		orgMember.setPictureURL(newMember.getPictureURL());
		orgMember.setVerifiedEmail(newMember.getVerifiedEmail());
		return orgMember;
	}

	public String getEmail()
	{
		return email;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public String getGivenName()
	{
		return givenName;
	}

	public String getGoogleId()
	{
		return googleId;
	}

	public String getLocale()
	{
		return locale;
	}

	public String getHd()
	{
		return hd;
	}

	public Long getMemberId()
	{
		return memberId;
	}

	public String getName()
	{
		return name;
	}

	public String getPictureURL()
	{
		return pictureURL;
	}

	public Boolean getVerifiedEmail()
	{
		return verifiedEmail;
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
	}

	public void setLocale(String value)
	{
		this.locale = value;
	}

	public void setEmail(String value)
	{
		this.email = value;
	}

	public void setFamilyName(String value)
	{
		this.familyName = value;
	}

	public void setGivenName(String value)
	{
		this.givenName = value;
	}

	public void setGoogleId(String value)
	{
		this.googleId = value;
	}

	public void setHd(String value)
	{
		this.hd = value;
	}

	public void setName(String value)
	{
		this.name = value;
	}

	public void setPictureURL(String value)
	{
		this.pictureURL = value;
	}

	public void setVerifiedEmail(Boolean value)
	{
		this.verifiedEmail = value;
	}
}
