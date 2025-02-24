/*
 * Copyright 2015-2018 the original author or authors.
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

package org.hawaiiframework.async.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hawaiiframework.async.AbortableTaskDecorator;
import org.hawaiiframework.async.DelegatingExecutor;
import org.hawaiiframework.async.model.ExecutorConfigurationProperties;
import org.hawaiiframework.async.model.ExecutorProperties;
import org.hawaiiframework.async.task_listener.TaskListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.hawaiiframework.async.AsyncExecutorConfiguration.ASYNC_TIMEOUT_EXECUTOR;


/**
 * Utility to initialize executors for the asynchronous execution of methods using
 * the @{@link org.springframework.scheduling.annotation.Async} annotation.
 *
 * @author Rutger Lubbers
 * @author Paul Klos
 * @since 3.0.0
 */
public class AsyncExecutorInitializer {

    /**
     * The logger to use.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncExecutorInitializer.class);

    /**
     * The default executor.
     * <p>
     * This is determined from the configuration properties.
     *
     * @see AsyncConfigurer
     */
    private TaskExecutor defaultExecutor;

    /**
     * Spring's bean factory.
     */
    private final ConfigurableListableBeanFactory beanFactory;

    /**
     * The executor configuration.
     */
    private final ExecutorConfigurationProperties configuration;

    /**
     * The constructor.
     *
     * @param beanFactory   Spring's bean factory.
     * @param configuration The executor configuration.
     */
    public AsyncExecutorInitializer(final ConfigurableListableBeanFactory beanFactory,
            final ExecutorConfigurationProperties configuration) {
        this.beanFactory = beanFactory;
        this.configuration = configuration;
    }

    /**
     * Initialize all configured executors in the bean factory and determine the default executor.
     */
    @SuppressWarnings("PMD.CloseResource")
    public void initializeExecutors() {
        final ScheduledThreadPoolExecutor asyncTimeoutExecutor =
                (ScheduledThreadPoolExecutor) beanFactory.getBean(ASYNC_TIMEOUT_EXECUTOR);
        asyncTimeoutExecutor.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("async-timeout-%d").daemon(true).build());
        beanFactory.initializeBean(asyncTimeoutExecutor, ASYNC_TIMEOUT_EXECUTOR);

        for (final ExecutorProperties executorConfiguration : configuration.getExecutors()) {
            final ThreadPoolTaskExecutor executor = initializeExecutor(executorConfiguration, asyncTimeoutExecutor);
            if (isDefaultExecutor(executorConfiguration)) {
                registerDefaultExecutor(executor);
            }
        }
    }

    private void registerDefaultExecutor(final ThreadPoolTaskExecutor executor) {
        final Map<String, TaskListenerFactory> beansOfType = beanFactory.getBeansOfType(TaskListenerFactory.class);
        defaultExecutor = new DelegatingExecutor(executor, configuration, beansOfType.values(), configuration.getDefaultExecutor());
    }

    /**
     * Get the executor to configure.
     *
     * @return The task executor.
     */
    @SuppressWarnings("PMD.CommentRequired")
    public TaskExecutor getDefaultExecutor() {
        return defaultExecutor;
    }


    /**
     * Configure a task executor from its configuration properties.
     *
     * @param executorConfiguration the executor's configuration.
     * @param timeoutExecutor       the timeout executor.
     */
    private ThreadPoolTaskExecutor initializeExecutor(final ExecutorProperties executorConfiguration,
            final ScheduledThreadPoolExecutor timeoutExecutor) {
        LOGGER.info("Creating executor '{}'.", executorConfiguration);
        final ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) beanFactory.getBean(executorConfiguration.getName());
        taskExecutor.setThreadFactory(null);
        taskExecutor.setThreadNamePrefix(executorConfiguration.getName() + "-");
        taskExecutor.setCorePoolSize(executorConfiguration.getCorePoolSize());
        taskExecutor.setMaxPoolSize(executorConfiguration.getMaxPoolSize());
        taskExecutor.setQueueCapacity(executorConfiguration.getMaxPendingRequests());
        taskExecutor.setKeepAliveSeconds(executorConfiguration.getKeepAliveTime());

        taskExecutor.setTaskDecorator(new AbortableTaskDecorator(taskExecutor, timeoutExecutor));

        taskExecutor.initialize();
        return taskExecutor;
    }

    /**
     * Match the name from the executor properties to the global default executor name.
     *
     * @param executorProperties the executor properties to check
     * @return true if the names match
     * @see #isDefaultExecutor(String)
     */
    private boolean isDefaultExecutor(final ExecutorProperties executorProperties) {
        return isDefaultExecutor(executorProperties.getName());
    }

    /**
     * Match the executor name to the global default executor name.
     *
     * @param executorName the executor name
     * @return true if the names match
     */
    private boolean isDefaultExecutor(final String executorName) {
        if (StringUtils.isBlank(executorName)) {
            return false;
        }
        return executorName.equals(configuration.getDefaultExecutor());
    }
}
