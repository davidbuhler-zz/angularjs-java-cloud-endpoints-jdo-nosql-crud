package com.knackedup.util;

import java.io.Serializable;
import java.util.Set;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.knackedup.error.InvalidDataException;
public class ValidationUtil implements Serializable
{
	private static final Logger	LOG					= Logger.getLogger(ValidationUtil.class.getName());
	private static final long	serialVersionUID	= 1L;

	public static <T> void logExceptions(Set<ConstraintViolation<T>> violations)
	{
		for (ConstraintViolation<?> cv : violations)
		{
			StringBuilder msg = new StringBuilder();
			msg.append("ValidatationConstraint: " + cv.getConstraintDescriptor().getAnnotation());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getConstraintDescriptor());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getMessageTemplate());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getInvalidValue());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getLeafBean());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getRootBeanClass());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getPropertyPath().toString());
			msg.append("<br/>");
			msg.append("ValidatationConstraint: " + cv.getMessage());
			LOG.info("ConstraintViolation = " + msg.toString());
		}
	}
}
