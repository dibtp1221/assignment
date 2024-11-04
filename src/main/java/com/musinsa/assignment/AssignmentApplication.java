package com.musinsa.assignment;

import com.musinsa.assignment.initdata.TestDataInit;
import com.musinsa.assignment.repository.BrandJpaRepository;
import com.musinsa.assignment.repository.CategoryJpaRepository;
import com.musinsa.assignment.repository.ItemRepository;
import com.musinsa.assignment.service.ResponseCachingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing
@SpringBootApplication
public class AssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of("ADMIN"); // 인증이 있다면 세션에서 로그인 정보 이용
	}

	@Bean
	@Profile("default") // 운영, qa, 개발, 로컬 따로 처리 가능함.
	public TestDataInit realDataInit(ItemRepository itemRepository,
                                     BrandJpaRepository brandRepository,
                                     CategoryJpaRepository categoryRepository,
                                     ResponseCachingService responseCachingService
	) {
		return new TestDataInit(itemRepository, brandRepository,
				categoryRepository, responseCachingService);
	}

}
