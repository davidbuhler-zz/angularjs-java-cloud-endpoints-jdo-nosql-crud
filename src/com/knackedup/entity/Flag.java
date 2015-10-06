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
public class Flag implements StoreCallback
{
	private Date	createdOn;
	@SuppressWarnings("unused")
	private Date	updatedOn;
	private Long	quickieId;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long	flagId;
	
	public Flag()
	{
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

	public void setQuickieId(Long value)
	{
		quickieId = value;
	}

	public Long getQuickieId()
	{
		return quickieId;
	}
}
