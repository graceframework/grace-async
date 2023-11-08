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
package org.grails.plugins.web.async;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import org.grails.plugins.web.async.mvc.AsyncActionResultTransformer;
import org.grails.plugins.web.async.spring.PromiseFactoryBean;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} for Grails Async
 *
 * @author Michael Yan
 * @since 5.5
 */
@AutoConfiguration
public class ControllersAsyncAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AsyncActionResultTransformer asyncPromiseResponseActionResultTransformer() {
        return new AsyncActionResultTransformer();
    }

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public PromiseFactoryBean grailsPromiseFactory() {
        return new PromiseFactoryBean();
    }

}
