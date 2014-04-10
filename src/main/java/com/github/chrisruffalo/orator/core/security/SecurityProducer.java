package com.github.chrisruffalo.orator.core.security;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;

@Startup
@Singleton
public class SecurityProducer {
	
	@Inject
	@Logging
	private Logger logger;
	
	private WebEnvironment webEnvironment;
	
	private SecurityManager securityManager;

	@PostConstruct
	public void init() {
		final String iniFile = "classpath:default-shiro.ini";
		this.logger.info("Initializing Shiro INI SecurityManager using " + iniFile);
		
		// load ini
		Ini ini = new Ini();
		ini.loadFromPath(iniFile);
		
		// create web environment (and get security manager)
		IniWebEnvironment web = new IniWebEnvironment();
		web.setIni(ini);
		
		LifecycleUtils.init(web);
		this.securityManager = web.getSecurityManager();
		SecurityUtils.setSecurityManager(this.securityManager);
		this.webEnvironment = web;
	}
	
	@Produces
	public SecurityManager getSecurityManager() {
		return this.securityManager;
	}

	@Produces
	public Subject getSubject() {
		// get subject
		return SecurityUtils.getSubject();
	}
		
	@Produces
	public WebEnvironment getEnvironment() {
		return this.webEnvironment;
	}
}
