package com.github.chrisruffalo.orator.server.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;

@WebFilter(
	asyncSupported = true, 
	urlPatterns = {
		"/*"
	}
)
public class SecurityFilter implements Filter {

	@Inject
	private SecurityManager manager;
	
	private ShiroFilter filter;
	
	@Override
	public void destroy() {
		this.filter.destroy();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		this.filter.doFilter(request, response, filterChain);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		this.filter = new ShiroFilter();
		this.filter.setEnabled(true);
		this.filter.setStaticSecurityManagerEnabled(true);
		if(this.manager instanceof WebSecurityManager) {
			this.filter.setSecurityManager((WebSecurityManager) this.manager);
		}
		this.filter.init(arg0);
	}

}
