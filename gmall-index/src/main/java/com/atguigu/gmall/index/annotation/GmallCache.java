package com.atguigu.gmall.index.annotation;

        import org.springframework.core.annotation.AliasFor;

        import java.lang.annotation.*;

@Target(ElementType.METHOD) // 这个注解可以作用在方法上，提供缓存注解使用，所以不需要ElementType.TYPE
@Retention(RetentionPolicy.RUNTIME) // 运行时注解
@Documented
public @interface GmallCache {
    /**
     * 缓存前缀
     * @return
     */
    @AliasFor("value")
    String prefix() default "";

    @AliasFor("prefix")
    String value() default "";

    /**
     * 单位时s
     * @return
     */
    long timeout() default 3001;

    /**
     * 为了防止缓存雪崩，设置的过期时间的随机值
     * @return
     */
    long random() default 3001;
}
