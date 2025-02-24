/*
 * Copyright 2015-2022 the original author or authors.
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
package org.hawaiiframework.logging.web.sse;

import org.hawaiiframework.logging.model.KibanaLogContext;
import org.hawaiiframework.logging.model.KibanaLogFields;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggingSseEmitter extends SseEmitter {

    private static final Logger LOGGER = getLogger(LoggingSseEmitter.class);

    private final KibanaLogContext context;
    private final List<HttpMessageConverter<?>> converters;

    public LoggingSseEmitter(final List<HttpMessageConverter<?>> converters, final KibanaLogContext context) {
        this.converters = converters;
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void send(final SseEventBuilder builder) throws IOException {
        final LoggingSseHttpOutputMessage output = new LoggingSseHttpOutputMessage();
        for (final DataWithMediaType dataWithMediaType : builder.build()) {
            final Object data = dataWithMediaType.getData();
            final MediaType mediaType = dataWithMediaType.getMediaType();
            for (HttpMessageConverter<?> converter : converters) {
                if (converter.canWrite(data.getClass(), mediaType)) {
                    ((HttpMessageConverter<Object>) converter).write(data, mediaType, output);
                    break;
                }
            }
        }

        super.send(builder);
        final KibanaLogContext currentCtx = KibanaLogFields.getContext();
        try {
            KibanaLogFields.populateFromContext(context);
            LOGGER.info(output.getContents());
        } finally {
            KibanaLogFields.clear();
            KibanaLogFields.populateFromContext(currentCtx);
        }
    }


    // AvoidSynchronizedAtMethodLevel -> since interface has this.
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    @Override
    public synchronized void onError(final Consumer<Throwable> callback) {
        super.onError(throwable -> {
            final KibanaLogContext currentCtx = KibanaLogFields.getContext();
            try {
                KibanaLogFields.populateFromContext(context);
                callback.accept(throwable);
                LOGGER.trace("End of stream.");
            } finally {
                KibanaLogFields.clear();
                KibanaLogFields.populateFromContext(currentCtx);
            }
        });
    }
}
