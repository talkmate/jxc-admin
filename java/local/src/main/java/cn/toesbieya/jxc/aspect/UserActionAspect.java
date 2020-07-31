package cn.toesbieya.jxc.aspect;

import cn.toesbieya.jxc.annoation.UserAction;
import cn.toesbieya.jxc.enumeration.UserActionEnum;
import cn.toesbieya.jxc.model.entity.RecUserAction;
import cn.toesbieya.jxc.service.RecService;
import cn.toesbieya.jxc.model.vo.Result;
import cn.toesbieya.jxc.utils.SpringUtil;
import cn.toesbieya.jxc.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
@Order(Integer.MAX_VALUE - 2)
public class UserActionAspect {
    @Resource
    private RecService recService;

    @Pointcut("@annotation(cn.toesbieya.jxc.annoation.UserAction)&&execution(cn.toesbieya.jxc.model.vo.Result cn.toesbieya.jxc..*.*(..))")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        RecUserAction action = ThreadUtil.getAction();
        if (action == null) {
            return pjp.proceed();
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();
        String[] argNames = signature.getParameterNames();
        UserAction annotation = method.getAnnotation(UserAction.class);

        //优先使用注解表达式
        if (!StringUtils.isEmpty(annotation.value())) {
            String str = (String) SpringUtil.spell(annotation.value(), argNames, args);
            if (!StringUtils.isEmpty(str)) {
                action.setAction(str);
            }
        }

        Result result = (Result) pjp.proceed();

        if (result.isSuccess()) {
            recService.insertUserAction(action, UserActionEnum.SUCCESS);
        }
        else {
            action.setError(result.getMsg());
            recService.insertUserAction(action, UserActionEnum.FAIL);
        }

        return result;
    }
}
