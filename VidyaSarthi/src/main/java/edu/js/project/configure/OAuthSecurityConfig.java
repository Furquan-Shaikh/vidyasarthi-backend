package edu.js.project.configure;
/*

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import edu.js.project.service.impl.BlacklistToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class OAuthSecurityConfig {

    private final RSAPrivateKey PRIVATE_KEY;
    private final RSAPublicKey PUBLIC_KEY;
    private BlacklistToken blacklistToken;

    public OAuthSecurityConfig(@Value("${jwt.private-key.location}")Resource PRIVATE_KEY, @Value("${jwt.public-key.location}") Resource PUBLIC_KEY, BlacklistToken blacklistToken) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.PRIVATE_KEY = loadPrivateKey(PRIVATE_KEY.getInputStream());
        this.PUBLIC_KEY = loadPublicKey(PUBLIC_KEY.getInputStream());
        this.blacklistToken = blacklistToken;
    }

    public RSAPrivateKey loadPrivateKey(InputStream is) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----(.*)-----","")
                .replaceAll("-----(.*)-----","")
                .replaceAll("\\s","");
        byte[] decode = Base64.getDecoder().decode(pem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decode);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);

    }

    public RSAPublicKey loadPublicKey(InputStream is) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        String pem = new String(is.readAllBytes(),StandardCharsets.UTF_8)
                .replaceAll("-----(.*)-----","")
                .replaceAll("-----(.*)-----","")
                .replaceAll("\\s","");
        byte[] decode = Base64.getDecoder().decode(pem);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decode);

        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);

    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public InMemoryUserDetailsManager userDetailsManager(){
//
//        UserDetails user = User.withUsername("admin")
//                .password("pass")
//                .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.sessionManagement( sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( req ->
                        req.requestMatchers(HttpMethod.POST, "/VidyaSarthi/loginAcc", "/VidyaSarthi/signUpAcc").permitAll()
                                .requestMatchers( "/VidyaSarthi/updateFaculty","/VidyaSarthi/addFaculty"
                                ,"/VidyaSarthi/updateStudent","/VidyaSarthi/searchTeacherByFilter","/VidyaSarthi/searchStudentByFilter"
                                ,"/VidyaSarthi/addStudent","/VidyaSarthi/facultyList","/VidyaSarthi/studentList"
                                ,"VidyaSarthi/searchByStudentId/","VidyaSarthi/deleteFaculty/","VidyaSarthi/deleteStudent/"
                                        ,"VidyaSarthi/searchByFacultyId/","/VidyaSarthi/addRegulation/","/VidyaSarthi/addRegulation/addUnit"
                                ,"VidyaSarthi/logoutAcc").hasAnyAuthority("Admin")
                                .requestMatchers("/VidyaSarthi/faculty/**","VidyaSarthi/logoutAcc").hasAnyAuthority("Faculty")
                                .requestMatchers("/VidyaSarthi/student/**","VidyaSarthi/logoutAcc").hasAnyAuthority("Student")

                                .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return httpSecurity.build();

    }

    @Bean
    public JwtEncoder jwtEncoder(){

        RSAKey key = new RSAKey.Builder(PUBLIC_KEY)
                .privateKey(PRIVATE_KEY)
                .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(key));

        return new NimbusJwtEncoder(jwkSource);

    }

    @Bean
    public JwtDecoder jwtDecoder(){

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(PUBLIC_KEY)
                .build();

        OAuth2TokenValidator<Jwt> expiry =JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> blackListToken = token ->
                blacklistToken.isBlacklisted(token.getTokenValue())
                        ?  OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid token ","User logout",null))
                        : OAuth2TokenValidatorResult.success();
        System.out.println(blackListToken);
        decoder.setJwtValidator( new DelegatingOAuth2TokenValidator<>(expiry,blackListToken));
        return decoder;

    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;

    }
}
*/

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import edu.js.project.service.impl.BlacklistToken;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class OAuthSecurityConfig {

    // These keys are now injected directly by Spring from the RsaKeyGenerator configuration.
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final BlacklistToken blacklistToken;

    /**
     * The constructor now uses dependency injection for the RSA keys.
     * This resolves the startup error by ensuring the keys are available
     * before this configuration is initialized.
     */
    public OAuthSecurityConfig(RSAPrivateKey privateKey, RSAPublicKey publicKey, BlacklistToken blacklistToken) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.blacklistToken = blacklistToken;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                // It's good practice to have a general API prefix like /api/v1
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.POST, "/VidyaSarthi/loginAcc", "/VidyaSarthi/signUpAcc").permitAll()
                        .requestMatchers("/VidyaSarthi/updateFaculty", "/VidyaSarthi/addFaculty",
                                "/VidyaSarthi/updateStudent", "/VidyaSarthi/searchTeacherByFilter", "/VidyaSarthi/searchStudentByFilter",
                                "/VidyaSarthi/addStudent", "/VidyaSarthi/facultyList", "/VidyaSarthi/studentList",
                                "/VidyaSarthi/searchByStudentId/**", "/VidyaSarthi/deleteFaculty/**", "/VidyaSarthi/deleteStudent/**",
                                "/VidyaSarthi/searchByFacultyId/**", "/VidyaSarthi/addRegulation/**",
                                "/VidyaSarthi/logoutAcc").hasAuthority("Admin")
                        .requestMatchers("/VidyaSarthi/faculty/**", "/VidyaSarthi/logoutAcc").hasAuthority("Faculty")
                        .requestMatchers("/VidyaSarthi/student/**", "/VidyaSarthi/logoutAcc").hasAuthority("Student")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        // Now uses the injected key beans
        RSAKey key = new RSAKey.Builder(this.publicKey)
                .privateKey(this.privateKey)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(key));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Now uses the injected publicKey bean
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(this.publicKey).build();

        // Combines standard validation with our custom blacklist check
        OAuth2TokenValidator<Jwt> withDefaultValidators = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> withBlacklistValidator = token ->
                blacklistToken.isBlacklisted(token.getTokenValue())
                        ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "This token has been revoked.", null))
                        : OAuth2TokenValidatorResult.success();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withDefaultValidators, withBlacklistValidator));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Telling the converter to look for authorities in the "roles" claim of the JWT
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        // Removing the default "SCOPE_" prefix
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
