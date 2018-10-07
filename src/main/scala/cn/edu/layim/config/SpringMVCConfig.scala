package cn.edu.layim.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.{InterceptorRegistry, ViewControllerRegistry, WebMvcConfigurerAdapter}

/**
  * SpringMVC配置
  *
  * @date 2018年9月8日
  * @author 梦境迷离
  */
@Configuration
class SpringMVCConfig extends WebMvcConfigurerAdapter {

    /**
      * 重写addViewControllers方法配置默认主页
      *
      * @param registry
      */
    override def addViewControllers(registry: ViewControllerRegistry): Unit = {
        registry.addViewController("/").setViewName("forward:/index.html")
        registry.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
        super.addViewControllers(registry)
    }

    /**
      * 注册拦截器
      *
      * @param registry
      */
    override def addInterceptors(registry: InterceptorRegistry) = {
        // addPathPatterns 用于添加拦截规则，excludePathPatterns 用户排除拦截
        registry.addInterceptor(new SystemHandlerInterceptor)
          .addPathPatterns("/**")
          .excludePathPatterns("/")
          .excludePathPatterns("/*.html")
          .excludePathPatterns("/user/login")
          .excludePathPatterns("/user/register")
          .excludePathPatterns("/user/existEmail")
        super.addInterceptors(registry);
    }
}