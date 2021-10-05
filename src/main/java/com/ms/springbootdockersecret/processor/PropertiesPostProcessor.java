package com.ms.springbootdockersecret.processor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.LinkedHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.FileCopyUtils;

public class PropertiesPostProcessor implements EnvironmentPostProcessor {
    @Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		
		String bindPathPpty = environment.getProperty("docker-secret.bind-path");

        Map<String,Object> dockerSecrets = new LinkedHashMap<>();
		if (bindPathPpty!=null) {
			File dockerFile = new File(bindPathPpty);
			
			if( dockerFile.exists()) {
				Optional<File[]> files = Optional.ofNullable(dockerFile.listFiles());
				
				files.ifPresent(item ->  {
					List<File> itemArray = Arrays.asList(item);
					itemArray.forEach(secretFile -> {
						String key = "docker-secret-" + secretFile.getName();
						byte[] content = null;
						try {
							content = FileCopyUtils.copyToByteArray(secretFile);
							dockerSecrets.put(key, new String(content));
						} catch (IOException e) {
							System.err.println(e.getMessage());
						}
					});
					
				});
			}
				
			MapPropertySource pptySource = new MapPropertySource("docker-secrets",dockerSecrets);
			
			environment.getPropertySources().addLast(pptySource);
				
		}				
	}
}
