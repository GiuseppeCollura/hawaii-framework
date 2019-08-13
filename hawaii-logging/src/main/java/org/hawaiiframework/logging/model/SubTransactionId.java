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

import java.util.UUID;

/**
 * Class that holds a subtransaction id in a ThreadLocal.
 *
 * @author Maikel Geuns
 * @since 2.0.0
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class SubTransactionId {

    /**
     * The thread local id.
     */
    private static ThreadLocal<UUID> id = new InheritableThreadLocal<>();

    private SubTransactionId() {
        // private constructor for utility class.
    }

    /**
     * Return the id as string.
     */
    @SuppressWarnings("PMD.LawOfDemeter")
    public static String get() {
        if (id.get() == null) {
            return null;
        }
        return id.get().toString();
    }

    /**
     * Set the transaction id.
     */
    public static void set(final UUID value) {
        id.set(value);
    }

    /**
     * Clear the thread local.
     */
    public static void remove() {
        id.remove();
    }

}
