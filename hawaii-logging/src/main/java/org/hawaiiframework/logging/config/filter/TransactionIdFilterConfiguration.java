/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hawaiiframework.logging.config.filter;

import org.hawaiiframework.logging.web.filter.TransactionIdFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hawaiiframework.logging.config.filter.FilterRegistrationBeanUtil.createFilterRegistrationBean;
import static org.hawaiiframework.logging.config.filter.TransactionIdFilterConfiguration.CONFIG_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configures the {@link TransactionIdFilter}.
 */
@Configuration
@ConditionalOnProperty(prefix = CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public class TransactionIdFilterConfiguration {

    /**
     * The configuration properties' prefix.
     */
    public static final String CONFIG_PREFIX = "hawaii.logging.filters.transaction-id";

    private static final Logger LOGGER = getLogger(TransactionIdFilterConfiguration.class);
    
    @Value("${" + CONFIG_PREFIX + ".http-header:X-Hawaii-Tx-Id}")
    private String headerName;

    @Value("${" + CONFIG_PREFIX + ".order:-500}")
    private int filterOrder;

    /**
     * Create the {@link TransactionIdFilter} bean.
     *
     * @return the {@link TransactionIdFilter} bean
     */
    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
    public TransactionIdFilter transactionIdFilter() {
        LOGGER.trace("Configuration: header '{}', order '{}'.", headerName, filterOrder);
        return new TransactionIdFilter(headerName);
    }

    /**
     * Register the {@link #transactionIdFilter()} bean.
     *
     * @param transactionIdFilter the transaction id filter
     * @return the {@link #transactionIdFilter()} bean, wrapped in a {@link FilterRegistrationBean}
     */
    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
    public FilterRegistrationBean<TransactionIdFilter> transactionIdFilterRegistration(final TransactionIdFilter transactionIdFilter) {
        return createFilterRegistrationBean(transactionIdFilter, filterOrder);
    }

}
