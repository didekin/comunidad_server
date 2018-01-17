package com.didekin;

import com.didekin.userservice.repository.UsuarioAuthService;
import com.didekin.userservice.repository.UsuarioManagerIf;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

import static com.didekin.ThreadPoolConstants.IDLE_TIMEOUT_FRONT;
import static com.didekinlib.http.CommonServConstant.ERROR;
import static com.didekinlib.http.ComunidadServConstant.COMUNIDAD_PATH;
import static com.didekinlib.http.ComunidadServConstant.COMUNIDAD_READ;
import static com.didekinlib.http.IncidServConstant.INCID_PATH;
import static com.didekinlib.http.IncidServConstant.INCID_READ;
import static com.didekinlib.http.IncidServConstant.INCID_WRITE;
import static com.didekinlib.http.UsuarioComunidadServConstant.COMUNIDAD_WRITE;
import static com.didekinlib.http.UsuarioServConstant.OPEN_AREA;
import static com.didekinlib.http.UsuarioServConstant.USER_PATH;
import static com.didekinlib.http.UsuarioServConstant.USER_READ;
import static com.didekinlib.http.UsuarioServConstant.USER_WRITE;
import static com.didekinlib.http.oauth2.OauthClient.CL_ADMON;
import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.ADMON_AUTH;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthConstant.READ_WRITE_SCOPE;
import static com.didekinlib.http.oauth2.OauthConstant.REFRESH_TOKEN_GRANT;
import static com.didekinlib.http.oauth2.OauthConstant.USERS_RSRC_ID;
import static com.didekinlib.http.oauth2.OauthConstant.USER_AUTH;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * User: pedro
 * Date: 10/03/15
 * Time: 17:18
 */

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    // Access tokens parameters.
    private static final int ACCESSTK_VALIDITY_SECONDS = 12 * 60 * 60;   // 12 horas, 43200 segundos.
    public static final int REFRESHTK_VALIDITY_SECONDS = 60 * 24 * 60 * 60; // 60 días.

    public static void main(String[] args)
    {
        logger.debug("Before calling run()");
        SpringApplication app = new SpringApplication(Application.class);
        app.setShowBanner(false);
        app.run(args);
    }

    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory()
    {
        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        factory.addServerCustomizers((Server server) -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
//            threadPool.setMinThreads(MIN_THREADS_FRONT);
//            threadPool.setMaxThreads(MAX_THREADS_FRONT);
            threadPool.setIdleTimeout(IDLE_TIMEOUT_FRONT);
            logger.debug(String.format("Max threads = %d min threads = %d idleTimeout = %d %n",
                    threadPool.getMaxThreads(), threadPool.getMinThreads(), threadPool.getIdleTimeout()));
        });
        return factory;
    }

    @Configuration
    @EnableWebSecurity
    public static class WebServerConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception
        {
            http
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers(ERROR, OPEN_AREA).permitAll()
                    .and()
                    .authorizeRequests().anyRequest()
                    .hasAnyAuthority(USER_AUTH, ADMON_AUTH)
                    .and()
                    .formLogin().disable().httpBasic()
            ;
        }
    }

    /*
    /oauth/authorize (the authorization endpoint)
    /oauth/token (the token endpoint)
    /oauth/error (used to render errors in the authorization server)
    /oauth/check_token (used by Resource Servers to decode access tokens)
    /oauth/token_key (exposes public key for token verification if using JWT tokens)
    /oauth/confirm_access (user posts approval for grants here)
    */
    @Configuration
    @EnableAuthorizationServer
    @EnableWebSecurity
    protected static class AuthorizationServerConfiguration extends
            AuthorizationServerConfigurerAdapter {

        @SuppressWarnings("SpringJavaAutowiringInspection")
        @Autowired
        private AuthenticationManager authenticationManager;

        @SuppressWarnings("unused")
        @Autowired
        private DataSource dataSource;

        @Autowired
        private JdbcTokenStore authTokenStore;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception
        {
            clients.inMemory()
                    .withClient(CL_USER.getId())
                    .resourceIds(USERS_RSRC_ID)
                    .authorizedGrantTypes(PASSWORD_GRANT, REFRESH_TOKEN_GRANT)
                    .authorities(USER_AUTH)
                    .scopes(READ_WRITE_SCOPE)
                    .accessTokenValiditySeconds(Application.ACCESSTK_VALIDITY_SECONDS)
                    .refreshTokenValiditySeconds(Application.REFRESHTK_VALIDITY_SECONDS)
                    .and()
                    .withClient(CL_ADMON.getId())
                    .resourceIds(USERS_RSRC_ID)
                    .authorizedGrantTypes(PASSWORD_GRANT, REFRESH_TOKEN_GRANT)
                    .authorities(ADMON_AUTH)
                    .scopes(READ_WRITE_SCOPE)
                    .accessTokenValiditySeconds(Application.ACCESSTK_VALIDITY_SECONDS)
                    .refreshTokenValiditySeconds(Application.REFRESHTK_VALIDITY_SECONDS)
            ;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
        {
            endpoints.authenticationManager(authenticationManager)
                    .tokenStore(authTokenStore)
                    .approvalStoreDisabled()
                    .reuseRefreshTokens(false);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security)
        {
//            security.sslOnly();
        }
    }

    @Configuration
    @EnableResourceServer
    public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private JdbcTokenStore resourceTokenStore;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
        {
            resources.stateless(true).resourceId(USERS_RSRC_ID).tokenStore(resourceTokenStore);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception
        {
            http
                    .requestMatchers().antMatchers(
                    USER_PATH + "/**",
                    COMUNIDAD_PATH + "/**",
                    INCID_PATH + "/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers(GET, USER_READ + "/**", COMUNIDAD_READ + "/**", INCID_READ + "/**")
                    .access("#oauth2.hasScope('readwrite') and hasAnyAuthority('user','admon')")
                    .antMatchers(POST, USER_WRITE + "/**", COMUNIDAD_WRITE + "/**", INCID_WRITE + "/**")
                    .access("#oauth2.hasScope('readwrite') and hasAnyAuthority('user','admon')")
                    .antMatchers(PUT, USER_WRITE + "/**", COMUNIDAD_WRITE + "/**", INCID_WRITE + "/**")
                    .access("#oauth2.hasScope('readwrite') and hasAnyAuthority('user','admon')")
                    .antMatchers(DELETE, USER_WRITE + "/**", COMUNIDAD_WRITE + "/**", INCID_WRITE + "/**")
                    .access("#oauth2.hasScope('readwrite') and hasAnyAuthority('user','admon')");
        }
    }

    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    protected static class AuthenticationManagerConfiguration extends
            GlobalAuthenticationConfigurerAdapter {

        @Autowired
        private UsuarioManagerIf sujetosService;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception
        {
            auth.userDetailsService(new UsuarioAuthService(sujetosService)).passwordEncoder(new BCryptPasswordEncoder());
        }
    }
}