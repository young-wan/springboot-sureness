package com.young.sureness.sureness.config;

import com.usthe.sureness.DefaultSurenessConfig;
import org.springframework.context.annotation.Bean;

/**
 * sureness
 * {description}
 *
 * @author Young
 * @date 2021-10-21 21:41
 **/
public class SurenessConfiguration {
    /**
     * sureness default config bean
     * @return default config bean
     */
    @Bean
    public DefaultSurenessConfig surenessConfig() {
        return new DefaultSurenessConfig();
    }
}
