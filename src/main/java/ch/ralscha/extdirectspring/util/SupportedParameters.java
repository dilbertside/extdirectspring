/**
 * Copyright 2010-2012 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring.util;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Enum of all supported parameter types
 * 
 * @author mansari
 * @author Ralph Schaer
 */
public enum SupportedParameters {

	SERVLET_REQUEST(ServletRequest.class), SERVLET_RESPONSE(ServletResponse.class), SESSION(HttpSession.class), LOCALE(
			Locale.class), PRINCIPAL(Principal.class);

	private final Class<?> clazz;

	private SupportedParameters(final Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * @return the enclosing Class
	 */
	public Class<?> getSupportedClass() {
		return clazz;
	}

	/**
	 * Checks if the class is a supported parameter type
	 * 
	 * @param clazz
	 * @return true if is supported, else false
	 */
	public static boolean isSupported(final Class<?> clazz) {
		if (clazz != null) {
			for (SupportedParameters supportedParameter : SupportedParameters.values()) {
				if (supportedParameter.clazz.isAssignableFrom(clazz)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Object resolveParameter(final Class<?> parameterType, final HttpServletRequest request,
			final HttpServletResponse response, final Locale locale) {

		if (SERVLET_REQUEST.getSupportedClass().isAssignableFrom(parameterType)) {
			return request;
		} else if (SERVLET_RESPONSE.getSupportedClass().isAssignableFrom(parameterType)) {
			return response;
		} else if (SESSION.getSupportedClass().isAssignableFrom(parameterType)) {
			return request.getSession();
		} else if (PRINCIPAL.getSupportedClass().isAssignableFrom(parameterType)) {
			return request.getUserPrincipal();
		} else if (LOCALE.getSupportedClass().equals(parameterType)) {
			return locale;
		}

		return null;

	}

}
