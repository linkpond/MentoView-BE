package ce2team1.mentoview.config;

import ce2team1.mentoview.security.filter.*;
import ce2team1.mentoview.security.handler.MvLogoutHandler;
import ce2team1.mentoview.security.handler.MvOAuth2FormFailureHandler;
import ce2team1.mentoview.security.handler.MvOAuth2FormSuccessHandler;
import ce2team1.mentoview.security.service.JwtTokenProvider;
import ce2team1.mentoview.security.service.MvAuthenticationProvider;
import ce2team1.mentoview.security.service.MvOAuth2UserService;
import ce2team1.mentoview.security.service.RefreshTokenService;
import ce2team1.mentoview.security.social.MvHttpCookieOAuth2AuthorizationRequestRepository;
import ce2team1.mentoview.security.social.MvOAuth2ClientRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.*;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    private final MvAuthenticationProvider mvAuthenticationProvider;
    private final MvOAuth2UserService mvOAuth2UserService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MvRequestFilter mvRequestFilter;
    private final MvLogoutHandler mvLogoutHandler;
    private final LambdaRequestFilter lambdaFilter;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final WebhookFilter webhookFilter;


    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        log.info("✅ [SecurityConfig] clientRegistrationRepository 생성 완료");
        return new MvHttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(MvOAuth2ClientRegistration mvOAuth2ClientRegistration) {
        return new InMemoryClientRegistrationRepository(mvOAuth2ClientRegistration.googleClientRegistration());
    }


    private void configureCommon(HttpSecurity security) throws Exception {
        security.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())

                .headers(headers -> headers
                        .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin-allow-popups")) // ✅ 직접 헤더 추가
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts.disable()))//AWS
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return web -> web.ignoring()
                .requestMatchers("/api/management/**","/api/targets","/api/config","/api/actuator","/api/actuator/**")
                .requestMatchers("/img/**")
                .requestMatchers("/api/management/**")
                .requestMatchers("/api/test")
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**", "/api/swagger-resources/**") //swagger
                .requestMatchers("/", "/favicon.ico", "/static", "/about", "/contactus")
                .requestMatchers("/error", "/error/**");
    }

    @Bean
    public SecurityFilterChain monitoringSecurityFilterChain(HttpSecurity security) throws Exception {
        configureCommon(security);
        security.securityMatcher("/api/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll() // 관리자가 로그인할 수 있도록 허용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // ✅ 관리자만 접근 가능
                        .anyRequest().authenticated()
                );
        return security.build();
    }

    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity security,
                                                         AuthenticationManager authenticationManager,
                                                         DeletedUserFilter deletedUserFilter) throws Exception {
        configureCommon(security);
        security.securityMatcher("/api/oauth2/authorization/google","/login/oauth2/code/google","/api/authorization/google/**","/api/login/oauth2/code/**");// 이 URL만 처리함
        security .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/google", "/api/authorization/google/**", "/api/oauth2/authorization/google").permitAll()
                        .requestMatchers("/api/login/oauth2/code/google").permitAll()
                        .requestMatchers("/api/login/oauth2/code/**").permitAll()
                        .requestMatchers("/login/oauth2/code/google").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated()

                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authEndpoint -> authEndpoint
                                .authorizationRequestRepository(authorizationRequestRepository())
                                .baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(redirectionEndpointConfig -> redirectionEndpointConfig
                                .baseUri("/api/login/oauth2/code/google"))
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(mvOAuth2UserService))
                        .successHandler(mvOAuth2FormSuccessHandler())
                        .failureHandler(mvOAuth2FormFailureHandler())
                );
        security.addFilterBefore(deletedUserFilter, UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }

    @Bean
    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity security,
                                                            AuthenticationManager authenticationManager,
                                                            MvLoginFormFilter mvLoginFormFilter,
                                                            AuthenticationConfiguration authenticationConfiguration,
                                                            DeletedUserFilter deletedUserFilter) throws Exception {
        configureCommon(security);
        security.securityMatcher("/api/login") // 폼 로그인 요청만 매칭
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(mvLoginFormFilter, UsernamePasswordAuthenticationFilter.class);
        security.addFilterBefore(deletedUserFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }
    @Bean
    public SecurityFilterChain commonSecurityFilterChain(HttpSecurity security,
                                                         DeletedUserFilter deletedUserFilter) throws Exception {
        configureCommon(security);
        security.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/api/interview/response/transcription").permitAll()
                        .requestMatchers("/api/signup/form").permitAll()
                        .requestMatchers("/api/webhook/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/token/access").authenticated()
                        .requestMatchers("/api/**").authenticated());

        security.addFilterBefore(lambdaFilter, UsernamePasswordAuthenticationFilter.class);
        security.addFilterBefore(webhookFilter, UsernamePasswordAuthenticationFilter.class);
        security.addFilterBefore(mvRequestFilter, UsernamePasswordAuthenticationFilter.class);

        security.addFilterBefore(deletedUserFilter, UsernamePasswordAuthenticationFilter.class);
        security.logout(logout -> logout
                .addLogoutHandler(mvLogoutHandler)
                .logoutUrl("/api/logout"));
        return security.build();
    }


    @Bean
    public AuthenticationSuccessHandler mvOAuth2FormSuccessHandler() {
        return new MvOAuth2FormSuccessHandler(jwtTokenProvider, refreshTokenService);
    }

    @Bean
    public AuthenticationFailureHandler mvOAuth2FormFailureHandler() {
        return new MvOAuth2FormFailureHandler();
    }

    @Bean
    public MvLoginFormFilter mvLoginFormFilter(AuthenticationManager authenticationManager) {
        MvLoginFormFilter filter = new MvLoginFormFilter(authenticationManager, mvOAuth2FormSuccessHandler(), mvOAuth2FormFailureHandler());
        filter.setFilterProcessesUrl("/api/login");
        return filter;
    }
}
