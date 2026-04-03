package com.nxboot.framework.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类：静态方法获取国际化消息
 */
@Component
public class I18nHelper {

    private static MessageSource messageSource;

    public I18nHelper(MessageSource messageSource) {
        I18nHelper.messageSource = messageSource;
    }

    /**
     * 获取国际化消息
     *
     * @param key  消息键
     * @param args 占位符参数
     * @return 国际化后的消息文本，找不到时返回 key 本身
     */
    public static String get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }
}
