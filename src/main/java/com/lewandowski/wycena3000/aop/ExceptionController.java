package com.lewandowski.wycena3000.aop;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest request, Exception e)   {
        log.error( "Request: {} raised {}", request.getRequestURL(), e);
        return new ModelAndView("error/error");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleError404(HttpServletRequest request, Exception e)   {
        log.error( "Request: {} raised {}", request.getRequestURL(), e);
        return new ModelAndView("error/404");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleError403(HttpServletRequest request, Exception e) {
        log.error( "Request: {} raised {}", request.getRequestURL(), e);
        return new ModelAndView("error/403");
    }
}
