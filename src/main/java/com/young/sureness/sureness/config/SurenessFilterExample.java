package com.young.sureness.sureness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usthe.sureness.mgt.SurenessSecurityManager;
import com.usthe.sureness.processor.exception.DisabledAccountException;
import com.usthe.sureness.processor.exception.ExcessiveAttemptsException;
import com.usthe.sureness.processor.exception.ExpiredCredentialsException;
import com.usthe.sureness.processor.exception.IncorrectCredentialsException;
import com.usthe.sureness.processor.exception.NeedDigestInfoException;
import com.usthe.sureness.processor.exception.ProcessorNotFoundException;
import com.usthe.sureness.processor.exception.UnauthorizedException;
import com.usthe.sureness.processor.exception.UnknownAccountException;
import com.usthe.sureness.processor.exception.UnsupportedSubjectException;
import com.usthe.sureness.subject.SubjectSum;
import com.usthe.sureness.util.SurenessContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * sureness
 * {description}
 *
 * @author Young
 * @date 2021-10-21 21:44
 **/
@Order(1)
@WebFilter(filterName = "SurenessFilterExample", urlPatterns = "/", asyncSupported = true)
public class SurenessFilterExample implements Filter {
    /**
     * write response json data
     *
     * @param content  content
     * @param response response
     */
    private static void responseWrite(ResponseEntity content, ServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        ((HttpServletResponse) response).setStatus(content.getStatusCodeValue());
        content.getHeaders().forEach((key, value) ->
                ((HttpServletResponse) response).addHeader(key, value.get(0)));
        try (PrintWriter printWriter = response.getWriter()) {
            if (content.getBody() != null) {
                if (content.getBody() instanceof String) {
                    printWriter.write(content.getBody().toString());
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    printWriter.write(objectMapper.writeValueAsString(content.getBody()));
                }
            } else {
                printWriter.flush();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            SubjectSum subjectSum = SurenessSecurityManager.getInstance().checkIn(servletRequest);
            // ????????????????????????????????????????????????subject ?????????subject????????????????????????????????????holder???????????????
            if (null != subjectSum) {
                SurenessContextHolder.bindSubject(subjectSum);
            }
        } catch (ProcessorNotFoundException | UnknownAccountException | UnsupportedSubjectException e4) {
            // ????????????????????????
            responseWrite(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(e4.getMessage()), servletResponse);
            return;
        } catch (DisabledAccountException | ExcessiveAttemptsException e2) {
            // ????????????????????????
            responseWrite(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body(e2.getMessage()), servletResponse);
            return;
        } catch (IncorrectCredentialsException | ExpiredCredentialsException e3) {
            // ????????????????????????
            responseWrite(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body(e3.getMessage()), servletResponse);
            return;
        } catch (NeedDigestInfoException e5) {
            // digest????????????????????????
            responseWrite(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", e5.getAuthenticate()).build(), servletResponse);
            return;
        } catch (UnauthorizedException e6) {
            // ?????????????????????????????????????????????api
            responseWrite(ResponseEntity
                    .status(HttpStatus.FORBIDDEN).body(e6.getMessage()), servletResponse);
            return;
        } catch (RuntimeException e) {
            // ????????????
            responseWrite(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(),
                    servletResponse);
            return;
        }
        try {
            // ?????????????????? ????????????????????? ????????????????????????
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            SurenessContextHolder.clear();
        }
    }
}
