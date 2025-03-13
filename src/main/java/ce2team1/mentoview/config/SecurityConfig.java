package ce2team1.mentoview.config;

import ce2team1.mentoview.security.*;
import ce2team1.mentoview.security.service.MvAuthenticationProvider;
import ce2team1.mentoview.security.service.MvOAuth2UserService;
import ce2team1.mentoview.security.service.RefreshTokenService;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
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
    private final AuthenticationConfiguration authenticationConfiguration;


    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
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

        return web -> web.ignoring().requestMatchers("/img/**")
                .requestMatchers("/api/test")
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**", "api/swagger-resources/**") //swagger
                .requestMatchers("/", "/favicon.ico", "/static", "/about", "/contactus")
                .requestMatchers("/error", "/error/**");

    }

    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity security,
                                                         AuthenticationManager authenticationManager) throws Exception {
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
        return security.build();
    }

    @Bean
    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity security,
                                                            AuthenticationManager authenticationManager,
                                                            MvLoginFormFilter mvLoginFormFilter, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        configureCommon(security);
        security.securityMatcher("/api/login") // 폼 로그인 요청만 매칭
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(mvLoginFormFilter, UsernamePasswordAuthenticationFilter.class);
        security.addFilterAfter(mvRequestFilter, SecurityContextHolderFilter.class);

        return security.build();

    }

    @Bean
    public SecurityFilterChain commonSecurityFilterChain(HttpSecurity security) throws Exception {
        configureCommon(security);
        security.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/api/signup/**").permitAll()
                        .requestMatchers("/api/auth/me").hasRole("USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN"));

        security.addFilterBefore(mvRequestFilter, UsernamePasswordAuthenticationFilter.class);
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




