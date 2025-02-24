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
package org.hawaiiframework.boot.autoconfigure.cache;

import org.hawaiiframework.cache.redis.config.RedisConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;


/**
 * Cache auto configuration.
 *
 * @author Richard Kohlen
 * @since 3.0.0
 */
@Configuration
@ConditionalOnClass({RedisConfiguration.class, RedisConnectionFactory.class})
@Import(RedisConfiguration.class)
public class HawaiiRedisAutoConfiguration {

}
