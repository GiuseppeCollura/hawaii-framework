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
package org.hawaiiframework.sql;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for {@link ResourceSqlQueryResolver}.
 *
 * @author Paul Klos
 * @since 2.0.0
 */
public class ResourceSqlQueryResolverTests {

    private ResourceSqlQueryResolver queryResolver;
    private String sqlFileName;
    private File sqlFile;
    private String prefix;
    private String suffix;

    @Before
    public void setup() throws Exception {
        queryResolver = new ResourceSqlQueryResolver(new FileSystemResourceLoader());
        suffix = ".sql";
        sqlFile = File.createTempFile("junit-test", suffix);
        // We're using a path outside the VM working dir, so prefix starts with file://
        prefix = "file:" + sqlFile.getParent() + '/';
        sqlFileName = FilenameUtils.getBaseName(sqlFile.getName());

        queryResolver.setPrefix(prefix);
        queryResolver.setSuffix(suffix);
        // Enable TRACE level for test debugging
        // Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        // logger.setLevel(Level.TRACE);
    }

    @After
    public void tearDown() {
        sqlFile.delete();
    }

    @Test
    public void testFileIsReadOk() throws Exception {
        FileUtils.writeStringToFile(sqlFile, "QUERY1", "UTF-8");
        String query = queryResolver.resolveSqlQuery(sqlFileName);
        assertThat(query, is(equalTo("QUERY1")));
    }

    @Test
    public void testNonExistentFile() {
        String query = queryResolver.resolveSqlQuery(sqlFileName + "idontexist");
        assertThat(query, is(emptyOrNullString()));
    }

    @Test
    public void testOldContentsAreReturnedWithinCacheTime() throws Exception {
        queryResolver.setCacheSeconds(20);
        FileUtils.writeStringToFile(sqlFile, "QUERY1", "UTF-8");
        String query = queryResolver.resolveSqlQuery(sqlFileName);
        assertThat(query, is(equalTo("QUERY1")));
        FileUtils.writeStringToFile(sqlFile, "QUERY2", "UTF-8");
        query = queryResolver.resolveSqlQuery(sqlFileName);
        assertThat(query, is(equalTo("QUERY1")));
    }

    @Test
    public void testNewContentsAreReturnedAfterCacheTime() throws Exception {
        queryResolver.setCacheSeconds(1);
        FileUtils.writeStringToFile(sqlFile, "QUERY1", "UTF-8");
        String query = queryResolver.resolveSqlQuery(sqlFileName);
        assertThat(query, is(equalTo("QUERY1")));
        // make sure the file timestamp will be at least a second higher
        TimeUnit.SECONDS.sleep(1);
        FileUtils.writeStringToFile(sqlFile, "QUERY2", "UTF-8");
        query = queryResolver.resolveSqlQuery(sqlFileName);
        assertThat(query, is(equalTo("QUERY2")));
    }

    @Test
    public void testOldContentsAreReturnedDuringConcurrentUpdate() throws Exception {
        // Custom resolver for this test
        queryResolver = new LongUpdatingQueryResolver(100);
        queryResolver.setPrefix(prefix);
        queryResolver.setSuffix(suffix);
        // Disable cache seconds, i.e. the loader will always check if the query file has changed
        queryResolver.setCacheSeconds(0);

        // Setup initial query
        FileUtils.writeStringToFile(sqlFile, "QUERY1", "UTF-8");
        assertThat(queryResolver.resolveSqlQuery(sqlFileName), is(equalTo("QUERY1")));

        // make sure the file timestamp will be at least a second higher
        TimeUnit.SECONDS.sleep(1);
        FileUtils.writeStringToFile(sqlFile, "QUERY2", "UTF-8");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<String> queryReader = () -> queryResolver.resolveSqlQuery(sqlFileName);

        Future<String> query1 = executor.submit(queryReader);
        Future<String> query2 = executor.submit(queryReader);

        /*
         * We don't know which thread will have returned the
         * updated query and which got the original one,
         * so just sort the result array and match it to the
         * array of expected values.
         */
        String[] returnedQueries = {query1.get(), query2.get()};
        Arrays.sort(returnedQueries);
        String[] expectedQueries = {"QUERY1", "QUERY2"};
        assertArrayEquals(expectedQueries, returnedQueries);
    }

    /*
     * Subclass ResourceSqlQueryResolver to make doRefreshQueryHolder
     * wait a while, so we can test if contention between threads is
     * handled correctly.
     */
    private static class LongUpdatingQueryResolver extends ResourceSqlQueryResolver {

        private final int waitMillis;

        private LongUpdatingQueryResolver(int waitMillis) {
            super(new FileSystemResourceLoader());
            this.waitMillis = waitMillis;
        }

        @Override
        protected void doRefreshQueryHolder(String sqlQueryName, QueryHolder queryHolder) {
            try {
                // First wait the specified amount
                TimeUnit.MILLISECONDS.sleep(this.waitMillis);
                // Now actually read the file
                super.doRefreshQueryHolder(sqlQueryName, queryHolder);
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }
    }
}
