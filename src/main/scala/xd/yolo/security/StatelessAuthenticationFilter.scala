package xd.yolo.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.{FilterChain, ServletRequest, ServletResponse}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class StatelessAuthenticationFilter extends GenericFilterBean {

  @Autowired
  val service: TokenAuthenticationService = null

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    val httpRequest = request.asInstanceOf[HttpServletRequest]
    val authentication = service.getAuthentication(httpRequest)
    SecurityContextHolder.getContext.setAuthentication(authentication)
    chain.doFilter(request, response)
    SecurityContextHolder.getContext.setAuthentication(null)
  }

}
