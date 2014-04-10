package com.github.chrisruffalo.orator.server.filter;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.MutableWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;

@WebListener
public class SecurityEnvironmentListener implements ServletContextListener {

	@Inject
	private WebEnvironment environment;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
		if(this.environment instanceof MutableWebEnvironment && this.environment.getServletContext() == null) {
			MutableWebEnvironment mutable = (MutableWebEnvironment)this.environment;
			mutable.setServletContext(context);
		}
		//LifecycleUtils.init(this.environment);
		context.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, this.environment);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
        try {
            Object environment = context.getAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);
            LifecycleUtils.destroy(environment);
        } finally {
        	context.removeAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);
        }
	}	
}
