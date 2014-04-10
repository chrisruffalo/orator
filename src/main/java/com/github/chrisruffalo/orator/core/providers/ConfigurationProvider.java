package com.github.chrisruffalo.orator.core.providers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;

import com.github.chrisruffalo.eeconfig.annotations.Bootstrap;
import com.github.chrisruffalo.eeconfig.annotations.DefaultProperty;
import com.github.chrisruffalo.eeconfig.annotations.Resolver;
import com.github.chrisruffalo.eeconfig.annotations.Source;

@ApplicationScoped
public class ConfigurationProvider {
	
	// properties and defaults
	public static final String KEY_HOME_DIR = "orator.home.dir";
	public static final String DEFAULT_HOME_DIR = "/opt/orator";
	
	@Inject
	@com.github.chrisruffalo.eeconfig.annotations.Configuration(
		sources = {
			@Source(
				value = "${orator.home.dir}/configuration.xml",
				resolve = true
			),
			@Source(
				value = "${jboss.server.config.dir}/orator/bootstrap.properties",
				resolve = true
			)			
		},
		merge = true,
		resolver = @Resolver(
			bootstrap = @Bootstrap(
				sources = {
					@Source(
						value = "${jboss.server.config.dir}/orator/bootstrap.properties",
						resolve = true
					)	
				}
			),
			properties = {
				@DefaultProperty(key = ConfigurationProvider.KEY_HOME_DIR, value = ConfigurationProvider.DEFAULT_HOME_DIR)
			}			
		)
	)	
	private Configuration configuration;

	@Produces
	public Configuration configuration() {
		return this.configuration;
	}
	
}
