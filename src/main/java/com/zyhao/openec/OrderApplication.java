package com.zyhao.openec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.zyhao.openec.pojo.MachineCode;
import com.zyhao.openec.util.QrcbConstant;

/**
 * The {@link AccountApplication} is a cloud-native Spring Boot application that manages
 * a bounded context for @{link Customer}, @{link Account}, @{link CreditCard}, and @{link Address}
 *
 * @author Kenny Bastani
 * @author Josh Long
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableEurekaClient
@EnableFeignClients
//@EnableResourceServer
//@EnableOAuth2Client
@EnableHystrix
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

//    @LoadBalanced
//    @Bean
//    public RestTemplate loadBalancedRestTemplate() {
//        return new RestTemplate();
//    }

    @LoadBalanced
    @Bean(name = "normalRestTemplate")
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
    
    @Component
    public static class CustomizedRestMvcConfiguration extends RepositoryRestConfigurerAdapter {

        @Override
        public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
            config.setBasePath("/api");
        }
    }

    @Bean
    AlwaysSampler alwaysSampler() {
        return new AlwaysSampler();
    }
    
    @Bean
	MachineCode machineCode(RestTemplate restTemplate) {
		return restTemplate.getForObject("http://unique-code.zyhao.com:8104/nologin/uniqueCode",MachineCode.class);
	}
	@Bean
    public QrcbConstant loadQrcbConstant() {
        return new QrcbConstant();
    }
}
