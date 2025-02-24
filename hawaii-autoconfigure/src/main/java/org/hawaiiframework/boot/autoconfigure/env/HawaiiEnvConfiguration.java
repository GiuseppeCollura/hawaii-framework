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

package org.hawaiiframework.boot.autoconfigure.env;

import org.jasypt.encryption.StringEncryptor;
import org.hawaiiframework.crypto.StringEncryptorConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Rutger Lubbers
 * @since 6.0.0
 */
@Configuration
@ConditionalOnClass(StringEncryptor.class)
@ConditionalOnProperty(prefix = "hawaii.crypto", name = "enabled", matchIfMissing = true)
@Import(StringEncryptorConfig.class)
public class HawaiiEnvConfiguration {

}
