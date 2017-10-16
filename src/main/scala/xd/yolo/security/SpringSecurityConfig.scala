package xd.yolo.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.HttpMethod._
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SpringSecurityConfig extends WebSecurityConfigurerAdapter(true) {

  @Value("${jwt.security}")
  private val secret: String = null

  override def configure(http: HttpSecurity): Unit = {
    http.exceptionHandling().and()
      .anonymous().and()
      .servletApi().and()
      .authorizeRequests()
      .antMatchers("/").hasAuthority("Admin")
      .antMatchers(POST, "/topics/{id:[a-z\\d]+}/open").hasAuthority("Admin")
      .antMatchers(POST, "/topics/{id:[a-z\\d]+}/wontfix").hasAuthority("Admin")
      .antMatchers(POST, "/topics/{id:[a-z\\d]+}/close").hasAuthority("Admin")
      .antMatchers(POST, "/groups").hasAuthority("Admin")
      .antMatchers(POST, "/tokens/groups").hasAuthority("Admin")
      .antMatchers(POST, "/tokens/users").hasAuthority("Admin")
      .antMatchers(POST, "/tokens/mails").hasAuthority("Admin")
      .antMatchers(POST, "/topics").hasAnyAuthority("Admin", "Member")
      .antMatchers(POST, "/topics/{id:[a-z\\d]+}/posts").hasAnyAuthority("Admin", "Member")
      .antMatchers(GET, "/topics/{id:[a-z\\d]+}").permitAll()
      .antMatchers(GET, "/topics").permitAll()
      .antMatchers(GET, "/topics/{id:[a-z\\d]+}/posts").permitAll()
      .antMatchers(GET, "/login/credentials").permitAll().and()
      .addFilterBefore(statelessAuthFilter(), classOf[UsernamePasswordAuthenticationFilter])
      .headers()
      .cacheControl()
  }

  override def configure(auth: AuthenticationManagerBuilder): Unit = {
    auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder())
  }

  @Bean
  def statelessAuthFilter(): StatelessAuthenticationFilter = new StatelessAuthenticationFilter()

  @Bean
  def tokenAuthenticationServer(): TokenAuthenticationService = new TokenAuthenticationService(secret)


}
