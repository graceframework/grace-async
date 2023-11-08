/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.events;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import grails.events.bus.EventBus;
import org.grails.events.bus.spring.EventBusFactoryBean;
import org.grails.events.gorm.GormDispatcherRegistrar;
import org.grails.events.spring.SpringEventTranslator;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} for {@link EventBus}
 *
 * @author Michael Yan
 * @since 5.5
 */
@AutoConfiguration
public class EventBusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EventBusFactoryBean grailsEventBus() {
        return new EventBusFactoryBean();
    }

    @Bean
    @ConditionalOnMissingBean
    public GormDispatcherRegistrar gormDispatchEventRegistrar(EventBus grailsEventBus) {
        return new GormDispatcherRegistrar(grailsEventBus);
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("deprecation")
    public reactor.bus.EventBus eventBus(EventBus grailsEventBus) {
        return new reactor.bus.EventBus(grailsEventBus);
    }

    @Bean
    @ConditionalOnProperty(name = "grails.events.spring", havingValue = "true")
    public SpringEventTranslator springEventTranslator(EventBus grailsEventBus) {
        return new SpringEventTranslator(grailsEventBus);
    }

}
