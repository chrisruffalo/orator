package com.github.chrisruffalo.orator.server.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

public class RememberingFilter extends FormAuthenticationFilter {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isRememberMe(ServletRequest request) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        return subject.isAuthenticated() || subject.isRemembered();
    }	
	
}
