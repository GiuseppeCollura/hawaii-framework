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

package org.hawaiiframework.logging;

import org.hawaiiframework.exception.HawaiiException;
import org.hawaiiframework.logging.model.AutoCloseableKibanaLogField;
import org.hawaiiframework.logging.model.KibanaLogFields;
import org.hawaiiframework.util.Invocable;
import org.hawaiiframework.util.Returnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.hawaiiframework.logging.model.KibanaLogFieldNames.LOG_TYPE;
import static org.hawaiiframework.logging.model.KibanaLogFieldNames.TX_DURATION;
import static org.hawaiiframework.logging.model.KibanaLogFieldNames.TX_TYPE;
import static org.hawaiiframework.logging.model.KibanaLogFields.tagCloseable;
import static org.hawaiiframework.logging.model.KibanaLogTypeNames.CALL_START;
import static org.hawaiiframework.logging.model.KibanaLogTypeNames.END;

/**
 * Helper to start a hawaii transaction, for logging purposes.
 */
public final class KibanaTxWrapper {

    /**
     * The logger to use.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KibanaTxWrapper.class);

    /**
     * Utility constructor.
     */
    private KibanaTxWrapper() {
        // Do nothing.
    }

    /**
     * Wrap the call with a kibana transaction.
     *
     * @param system     The system's name.
     * @param txName     The call's name.
     * @param returnable The actual code to invoke.
     * @param <T>        The return type.
     * @return The value returned by the {@code supplier}.
     */
    @SuppressWarnings({"unused", "try", "PMD.AvoidCatchingThrowable"})
    public static <T> T kibanaTx(final String system, final String txName, final Returnable<T> returnable) {
        final long startTime = System.nanoTime();

        try (KibanaLogTransaction kibanaLogTransaction = new KibanaLogTransaction(getTxType(system, txName))) {
            try {
                logStart();
                return returnable.invoke();
            } catch (RuntimeException rethrown) {
                logError(rethrown);
                throw rethrown;
            } catch (Throwable throwable) {
                logError(throwable);
                throw new HawaiiException(throwable);
            } finally {
                logEnd(startTime);
            }
        }
    }

    /**
     * Wrap the call with a kibana transaction.
     *
     * @param system    The system's name.
     * @param txName    The call's name.
     * @param invocable The actual code to invoke.
     */
    @SuppressWarnings({"unused", "try", "PMD.AvoidCatchingThrowable"})
    public static void kibanaTx(final String system, final String txName, final Invocable invocable) {
        final long startTime = System.nanoTime();

        try (KibanaLogTransaction kibanaLogTransaction = new KibanaLogTransaction(getTxType(system, txName))) {
            try {
                logStart();
                invocable.invoke();
            } catch (RuntimeException rethrown) {
                logError(rethrown);
                throw rethrown;
            } catch (Throwable throwable) {
                logError(throwable);
                throw new HawaiiException(throwable);
            } finally {
                logEnd(startTime);
            }
        }
    }

    @SuppressWarnings({"unused", "try"})
    private static void logStart() {
        try (AutoCloseableKibanaLogField startTag = tagCloseable(LOG_TYPE, CALL_START)) {
            LOGGER.info("Started '{}'.", KibanaLogFields.get(TX_TYPE));
        }
    }

    @SuppressWarnings({"unused", "try"})
    private static void logEnd(final long startTime) {
        final String duration = format("%.2f", (System.nanoTime() - startTime) / 1E6);
        try (AutoCloseableKibanaLogField endTag = tagCloseable(LOG_TYPE, END);
                AutoCloseableKibanaLogField durationTag = tagCloseable(TX_DURATION, duration)) {
            LOGGER.info("Duration '{}' ms.", duration);
        }
    }

    private static String getTxType(final String system, final String txName) {
        return format("%s.%s", system, txName);
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private static void logError(final Throwable throwable) {
        LOGGER.error("Got exception '{}' with message '{}'.", throwable.getClass().getSimpleName(), throwable.getMessage(), throwable);
    }

}
