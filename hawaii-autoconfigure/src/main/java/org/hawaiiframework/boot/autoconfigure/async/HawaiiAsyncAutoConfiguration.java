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

package org.hawaiiframework.boot.autoconfigure.async;

import org.hawaiiframework.async.AsyncExecutorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Async auto configuration.
 *
 * @author Paul Klos
 * @since 2.0.0
 */
@Configuration
@ConditionalOnClass(AsyncExecutorConfiguration.class)
@ConditionalOnResource(resources = "${hawaii.async.configuration}")
@Import(AsyncExecutorConfiguration.class)
public class HawaiiAsyncAutoConfiguration {

}
