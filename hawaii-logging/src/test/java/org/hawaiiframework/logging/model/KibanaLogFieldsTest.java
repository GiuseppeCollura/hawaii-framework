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
package org.hawaiiframework.logging.model;

import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hawaiiframework.logging.model.KibanaLogFieldNames.USER;

public class KibanaLogFieldsTest {
    @Test
    public void testMdcKeysShouldContainAllMdcKeysExceptLogType() {
        for (final KibanaLogFieldNames fieldName : KibanaLogFieldNames.values()) {
            KibanaLogFields.set(fieldName, "test");
        }

        for (final KibanaLogFieldNames mdcKey : KibanaLogFieldNames.values()) {
            if (mdcKey.equals(KibanaLogFieldNames.LOG_TYPE)) {
                assertThat(String.format("Kibana log string should not contain %s", mdcKey.getLogName()), KibanaLogFields.getValuesAsLogString(), not(containsString(mdcKey.getLogName())));
            } else {
                assertThat(String.format("Kibana log string should contain %s", mdcKey.getLogName()), KibanaLogFields.getValuesAsLogString(), containsString(mdcKey.getLogName()));
            }
        }
    }

    @Test
    public void testTagWithList() {
        KibanaLogFields.tag(USER, List.of("a", "b", "c"));
        assertThat("Kibana log field should contain flattened list", KibanaLogFields.get(USER), is("['a', 'b', 'c']"));
    }

    @Test
    public void setWithList() {
        KibanaLogFields.set(USER, List.of("a", "b", "c"));
        assertThat("Kibana log field should contain flattened list", KibanaLogFields.get(USER), is("['a', 'b', 'c']"));
    }

    @After
    public void tearDown() {
        KibanaLogFields.clear();
    }
}
