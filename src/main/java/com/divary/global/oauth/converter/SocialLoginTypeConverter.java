package com.divary.global.oauth.converter;

import com.divary.common.enums.SocialType;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class SocialLoginTypeConverter implements Converter<String, SocialType> {
    @Override
    public SocialType convert(String s) {
        return SocialType.valueOf(s.toUpperCase());
    }
}
