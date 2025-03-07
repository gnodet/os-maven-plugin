/*
 * Copyright 2024 Guillaume Nodet <gnodet@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.maveniverse.maven.os;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.maven.api.spi.PropertyContributor;
import org.codehaus.plexus.logging.Logger;

/**
 * Set Maven session user properties.
 */
@Named
@Typed(PropertyContributor.class)
public class DetectPropertyContributor implements PropertyContributor {

    private final Logger logger;

    @Inject
    DetectPropertyContributor(Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public void contribute(Map<String, String> map) {
        logger.info(
                "The os-detector Maven 4 extension is registered, OS and CPU architecture properties will be provided.");
        DetectExtension.disable();

        final Properties props = new Properties();
        props.putAll(map);

        final Detector detector = new Detector(new SimpleSystemPropertyOperations(map), new SimpleFileOperations()) {
            @Override
            protected void log(String message) {
                logger.debug(message);
            }

            @Override
            protected void logProperty(String name, String value) {}
        };
        detector.detect(props, Collections.emptyList());
    }

    private static class SimpleSystemPropertyOperations implements SystemPropertyOperationProvider {
        final Map<String, String> map;

        private SimpleSystemPropertyOperations(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public String getSystemProperty(String name) {
            return System.getProperty(name);
        }

        @Override
        public String getSystemProperty(String name, String def) {
            return System.getProperty(name, def);
        }

        @Override
        public String setSystemProperty(String name, String value) {
            map.put(name, value);
            return System.setProperty(name, value);
        }
    }

    private static class SimpleFileOperations implements FileOperationProvider {
        @Override
        public InputStream readFile(String fileName) throws IOException {
            return Files.newInputStream(Path.of(fileName));
        }
    }
}
