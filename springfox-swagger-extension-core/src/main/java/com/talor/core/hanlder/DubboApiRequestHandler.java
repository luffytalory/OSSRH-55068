package com.talor.core.hanlder;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static springfox.documentation.spring.web.paths.Paths.splitCamelCase;

/**
 * @author luffy
 * @version 1.0
 * @className DubboApiRequestHandler
 * @description to custom handle and process Request
 */
public class DubboApiRequestHandler implements RequestHandler {

    private final HandlerMethodResolver methodResolver;
    private final TypeResolver typeResolver;
    private final RequestMappingInfo requestMapping;
    private List<ResolvedMethodParameter> parameters;
    private HandlerMethod handlerMethod;

    public DubboApiRequestHandler(HandlerMethodResolver methodResolver, TypeResolver typeResolver,
                                  RequestMappingInfo requestMapping, HandlerMethod handlerMethod) {
        this.methodResolver = methodResolver;
        this.typeResolver = typeResolver;
        this.requestMapping = requestMapping;
        this.handlerMethod = handlerMethod;
    }

    @Override
    public Class<?> declaringClass() {
        return handlerMethod.getBeanType();
    }

    @Override
    public boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
        return null != AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation);
    }

    @Override
    public PatternsRequestCondition getPatternsCondition() {
        return requestMapping.getPatternsCondition();
    }

    @Override
    public String groupName() {
        return splitCamelCase(declaringClass().getSimpleName(), "-").replace("/", "").toLowerCase();
    }

    @Override
    public String getName() {
        Method method = handlerMethod.getMethod();
        return Stream.of(method.getDeclaringClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(method.getName()))
                .count() == 1
                ? method.getName()
                : method.getName() +
                "_(" + Stream.of(method.getParameters()).map(Parameter::getType)
                .map(Class::getSimpleName).collect(joining(", ")) + ")";
    }

    @Override
    public Set<RequestMethod> supportedMethods() {
        return requestMapping.getMethodsCondition().getMethods();
    }

    @Override
    public Set<? extends MediaType> produces() {
        return requestMapping.getProducesCondition().getProducibleMediaTypes();
    }

    @Override
    public Set<? extends MediaType> consumes() {
        return requestMapping.getConsumesCondition().getConsumableMediaTypes();
    }

    @Override
    public Set<NameValueExpression<String>> headers() {
        return Sets.newHashSet();
    }

    @Override
    public Set<NameValueExpression<String>> params() {
        return Sets.newHashSet();
    }

    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotation) {
        return Optional.fromNullable(AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation));
    }

    @Override
    public RequestHandlerKey key() {
        return new RequestHandlerKey(getPatternsCondition().getPatterns(), supportedMethods(), consumes(), produces());
    }

    @Override
    public List<ResolvedMethodParameter> getParameters() {
        if (parameters == null) {
            parameters = methodResolver.methodParameters(handlerMethod);
        }
        return parameters;
    }

    @Override
    public ResolvedType getReturnType() {
        return typeResolver.resolve(handlerMethod.getReturnType().getGenericParameterType());
    }

    @Override
    public <T extends Annotation> Optional<T> findControllerAnnotation(Class<T> annotation) {
        return Optional.fromNullable(AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), annotation));
    }

    @Override
    public RequestMappingInfo getRequestMapping() {
        return requestMapping;
    }

    @Override
    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public RequestHandler combine(RequestHandler other) {
        return this;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("DubboApiRequestHandler{");
        sb.append("key=").append(key());
        sb.append('}');
        return sb.toString();
    }
}
