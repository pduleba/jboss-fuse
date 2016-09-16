package com.pduleba.config;

import static com.pduleba.config.CamelConfig.DATA_FORMAT_BEAN_ID;
import static com.pduleba.config.CamelConfig.DATA_PROVIDER_BEAN_ID;

import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.pduleba.jaxrs.CompanyResource;
import com.pduleba.jaxrs.JaxRsApiApplication;
import com.pduleba.service.JsonService;

@Configuration
@PropertySource("classpath:application.properties")
@Import(CamelConfig.class)
public class ApplicationConfig {

	@Bean 
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public JaxRsApiApplication jaxRsApiApplication() {
		return new JaxRsApiApplication();
	}

	// Change it to import
	@Bean(destroyMethod = "shutdown")
	public SpringBus cxf() {
		return new SpringBus();
	}

	@Bean
	public CompanyResource companyResource() {
		return new CompanyResource();
	}
	
	@Bean(name = CamelConfig.JAXRS_BEAN_ID)
	@DependsOn("cxf")
	public JAXRSServerFactoryBean rsServer(CompanyResource companyResource,
			@Qualifier(DATA_PROVIDER_BEAN_ID) JacksonJsonProvider jacksonProvider,
			JaxRsApiApplication jaxRsApiApplication, SpringBus cxf) {
		JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
		
		factory.setBus(cxf);
		factory.setServiceBeans(Arrays.<Object>asList(companyResource));
		factory.setAddress("http://localhost:9000/api");
		factory.setProviders(Arrays.<Object>asList(jacksonProvider));
		
		return factory;
	}


	@Bean
	public JsonService jsonService(
			@Qualifier(DATA_FORMAT_BEAN_ID) JacksonDataFormat jackson,
			@Qualifier(DATA_PROVIDER_BEAN_ID)  JacksonJsonProvider jacksonProvider,
			CamelContext camelContext) {
		return new JsonService(jackson, jacksonProvider, camelContext);
	}
}