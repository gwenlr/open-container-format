/*
 * Copyright (c) 2026, the original authors
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

package com.github.gwenlr.open_container_format;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryFileContainerTest {

    private MemoryFileContainer container;

    @BeforeEach
    void setUp() {
        container = new MemoryFileContainer();
    }


    @Test
    void setManifestProperty() {
        container.setManifestProperty("xxx", "abc");
        assertThat(container.getManifestProperty("xxx"))
                .isEqualTo("abc");
    }

    @Test
    void addFileEntry_noVersion_text() {
        container.addFileEntry("/toto.txt", "text/plain", "content");
    }

    @Test
    void addFileEntry_version_text() {
        container.addFileEntry("/toto.txt", "text/plain", "content", "4");
    }

    @Test
    void addFileEntry_noVersion_binary() {
        byte[] content = "content".getBytes();
        container.addFileEntry("/toto.bin", "application/octet-stream", content);
    }

    @Test
    void addFileEntry_version_binary() {
        byte[] content = "content".getBytes();
        container.addFileEntry("/toto.bin", "application/octet-stream", content, "5");

    }

    @Test
    void toBinaryContent_empty() {
        var content = container.toBinaryContent();
        assertThat(content).isNotEmpty();
    }

    @Test
    void toBinaryContent_filled() {
        container.addFileEntry("/toto.txt", "text/plain", "toto");
        container.addFileEntry("/tata.txt", "text/plain", "tata");
        container.setManifestProperty("xxx", "abc");
        container.setManifestProperty("yyy", "def");

        var content = container.toBinaryContent();
        assertThat(content).isNotEmpty();
    }

    @Test
    void loadFromBinaryContent_empty() {
        var content = new MemoryFileContainer().toBinaryContent();

        container.loadFromBinaryContent(content);

        assertThat(
                container.getManifestProperties().entrySet()
        ).extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsOnly(
                        Tuple.tuple(StandardProperties.VERSION, "")
                );

        assertThat(container.getFilePaths())
                .isEmpty();
    }

    @Test
    void loadFromBinaryContent_filled_with_properties() {
        var container2 = new MemoryFileContainer();
        container2.setManifestProperty("xxx", "abc");
        container2.setManifestProperty("yyy", "def");


        var content = container2.toBinaryContent();

        container.loadFromBinaryContent(content);

        assertThat(container.getManifestProperties().entrySet())
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsOnly(
                        Tuple.tuple(StandardProperties.VERSION, ""),
                        Tuple.tuple("xxx", "abc"),
                        Tuple.tuple("yyy", "def")
                );
    }

    @Test
    void loadFromBinaryContent_filled_with_files() {
        var container2 = new MemoryFileContainer();
        container2.addFileEntry("/a/b/c", "text/plain", "content", "0");
        container2.addFileEntry("/toto.txt", "text/html", "toto", "1");
        container2.addFileEntry("/tata.txt", "text/csv", "tata", "2");

        var content = container2.toBinaryContent();

        container.loadFromBinaryContent(content);

        assertThat(container2.getFilePaths())
                .containsOnly("/a/b/c", "/toto.txt", "/tata.txt");

        assertContainsFile("/a/b/c", "text/plain", "content", "0");
        assertContainsFile("/toto.txt", "text/html", "toto", "1");
        assertContainsFile("/tata.txt", "text/csv", "tata", "2");
    }

    private void assertContainsFile(String path, String mediaType, String content, String version) {
        assertThat(container.getFilePaths())
                .contains(path);
        assertThat(container.getFileMetadata(path))
                .isNotNull()
                .extracting("fullPath", "mediaType", "version")
                .containsExactly(path, mediaType, version);

        assertThat(container.getFileContentAsString(path))
                .isEqualTo(content);
        assertThat(container.getFileContentAsBinary(path))
                .isEqualTo(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("containsManifestProperty shall return false when property not exist")
    void containsManifestProperty_not_exist() {
        assertThat(
                container.containsManifestProperty("xxx")
        ).isFalse();
    }

    @Test
    @DisplayName("containsManifestProperty shall return true when property exists")
    void containsManifestProperty_exists() {
        container.setManifestProperty("xxx", "abc");

        assertThat(
                container.containsManifestProperty("xxx")
        ).isTrue();
    }

    @Test
    @DisplayName("getManifestProperties shall return only version when container is empty")
    void getManifestProperties_empty() {
        Map<String, String> manifestProperties = container.getManifestProperties();
        assertThat(
                manifestProperties.entrySet()
        ).extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsOnly(
                        Tuple.tuple(StandardProperties.VERSION, "")
                );
    }

    @Test
    @DisplayName("getManifestProperties shall return all non internal properties")
    void getManifestProperties_filled() {
        container.setManifestProperty("xxx", "abc");
        container.setManifestProperty("yyy", "def");

        Map<String, String> manifestProperties = container.getManifestProperties();
        assertThat(
                manifestProperties.entrySet()
        ).extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsOnly(
                        Tuple.tuple(StandardProperties.VERSION, ""),
                        Tuple.tuple("xxx", "abc"),
                        Tuple.tuple("yyy", "def")
                );
    }

    @Test
    @DisplayName("containsFileEntry shall return false when file not registered")
    void containsFileEntry_not_found() {
        assertThat(container.containsFileEntry("/toto.txt"))
                .isFalse();
    }

    @Test
    @DisplayName("containsFileEntry shall return true when file registered")
    void containsFileEntry_found() {
        container.addFileEntry("/a/b/c", "text/plain", "content", "0");

        assertThat(container.containsFileEntry("/a/b/c"))
                .isTrue();
    }

    @Test
    @DisplayName("getFileContentAsString shall return null when file not registered")
    void getFileContentAsString_not_found() {
        assertThat(container.getFileContentAsString("/a/b"))
                .isNull();
    }

    @Test
    @DisplayName("getFileContentAsString shall return file content when file registered")
    void getFileContentAsString_found() {
        String content = "content";
        container.addFileEntry("/a/b/c", "text/plain", content, "0");

        assertThat(container.getFileContentAsString("/a/b/c"))
                .isEqualTo(content);
    }

    @Test
    @DisplayName("getFileContentAsBinary shall return null when file not registered")
    void getFileContentAsBinary_not_found() {
        assertThat(container.getFileContentAsBinary("/a/b"))
                .isNull();
    }

    @Test
    @DisplayName("getFileContentAsBinary shall return file content when file registered")
    void getFileContentAsBinary_found() {
        byte[] content = "content".getBytes();
        container.addFileEntry("/a/b/c", "application/octet-stream", content, "0");

        assertThat(container.getFileContentAsBinary("/a/b/c"))
                .isEqualTo(content);
    }

    @Test
    @DisplayName("getFileMetadata shall return null when file not registered")
    void getFileMetadata_not_found() {
        assertThat(container.getFileMetadata("/a/b"))
                .isNull();
    }

    @Test
    @DisplayName("getFileMetadata shall return metadata when file registered")
    void getFileMetadata_found() {
        container.addFileEntry("/a/b/c", "text/plain", "content", "0");

        assertThat(container.getFileMetadata("/a/b/c"))
                .isNotNull()
                .extracting("fullPath", "mediaType", "version")
                .containsOnly("/a/b/c", "text/plain", "0");
    }


    @Test
    @DisplayName("getFilePaths shall return empty set when no registered files")
    void getFilePaths_empty() {
        assertThat(container.getFilePaths())
                .isEmpty();
    }

    @Test
    @DisplayName("getFilePaths shall return empty set when no registered files")
    void getFilePaths_filled() {
        container.addFileEntry("/a/b/c", "text/plain", "content", "0");
        container.addFileEntry("/toto.txt", "text/plain", "content", "0");
        container.addFileEntry("/tata.txt", "text/plain", "content", "0");

        assertThat(container.getFilePaths())
                .containsOnly("/a/b/c", "/toto.txt", "/tata.txt");
    }
}