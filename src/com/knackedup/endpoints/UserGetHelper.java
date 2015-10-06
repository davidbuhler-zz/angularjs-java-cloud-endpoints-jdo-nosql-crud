package com.knackedup.endpoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.google.appengine.api.users.User;
import com.knackedup.entity.Member;
import com.knackedup.error.GenericServiceException;

public class UserGetHelper
{
	public Member fetchUserProfile(String token) throws Exception
	{
		String url = null;
		StringBuffer stringBuffer = null;
		Member member = new Member();
		try
		{
			url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + token;
			stringBuffer = new StringBuffer();
			final URL u = new URL(url);
			final URLConnection uc = u.openConnection();
			final int end = 1000;
			InputStreamReader isr = null;
			BufferedReader br = null;
			try
			{
				isr = new InputStreamReader(uc.getInputStream());
				br = new BufferedReader(isr);
				final int chk = 0;
				while ((url = br.readLine()) != null)
				{
					if ((chk >= 0) && ((chk < end)))
					{
						stringBuffer.append(url).append('\n');
					}
				}
			}
			catch (final java.net.ConnectException cex)
			{
				throw new GenericServiceException();
			}
			catch (final Exception ex)
			{
				throw new GenericServiceException();
			}
			finally
			{
				try
				{
					br.close();
				}
				catch (final Exception ex)
				{
					throw new GenericServiceException();
				}
			}
		}
		catch (final Exception ex)
		{
			throw ex;
		}
		try
		{
			final JsonFactory f = new JsonFactory();
			JsonParser jp;
			jp = f.createJsonParser(stringBuffer.toString());
			jp.nextToken();
			while (jp.nextToken() != JsonToken.END_OBJECT)
			{
				final String fieldname = jp.getCurrentName();
				jp.nextToken();
				if ("picture".equals(fieldname))
				{
					member.setPictureURL(jp.getText());
				}
				else if ("name".equals(fieldname))
				{
					member.setName(jp.getText());
				}
				else if ("email".equals(fieldname))
				{
					member.setEmail(jp.getText());
				}
				else if ("verified_email".equals(fieldname))
				{
					member.setVerifiedEmail(Boolean.valueOf(jp.getText()));
				}
				else if ("given_name".equals(fieldname))
				{
					member.setGivenName(jp.getText());
				}
				else if ("family_name".equals(fieldname))
				{
					member.setFamilyName(jp.getText());
				}
				else if ("locale".equals(fieldname))
				{
					member.setLocale(jp.getText());
				}
				else if ("id".equals(fieldname))
				{
					member.setGoogleId(jp.getText());
				}
				else if ("hd".equals(fieldname))
				{
					member.setHd(jp.getText());
				}
			}
		}
		catch (final JsonParseException ex)
		{
			throw (ex);
		}
		catch (final IOException ex)
		{
			throw (ex);
		}
		catch (final Exception ex)
		{
			throw (ex);
		}
		return member;
	}
}
