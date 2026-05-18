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
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManifestTest {

    private static final Instant instant = Instant.now();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Manifest manifest;

    @BeforeEach
    void setUp() {
        manifest = new Manifest();
    }

    @Test
    void fullPath() {
        assertThat(manifest.fullPath())
                .isEqualTo(StandardProperties.MANIFEST_FULL_PATH);
    }

    @Test
    void getStorageFormatVersion() {
        assertThat(manifest.getStorageFormatVersion())
                .isEqualTo("1");
    }

    @Test
    @DisplayName("getVersion shall return the default value when not set")
    void getVersion_defaultValue() {
        assertThat(manifest.getVersion())
                .isEmpty();
    }

    @Test
    void setVersion() {
        manifest.setVersion("3");

        assertThat(manifest.getVersion())
                .isEqualTo("3");
    }

    @Test
    @DisplayName("getCreationInstant shall be null by default")
    void getCreationInstant() {
        assertThat(manifest.getCreationInstant())
                .isNull();
    }

    @Test
    @DisplayName("setCreationInstant shall accept non null value")
    void setCreationInstant() {
        manifest.setCreationInstant(instant);

        assertThat(manifest.getCreationInstant())
                .isEqualTo(instant);
    }

    @Test
    @DisplayName("setCreationInstant shall accept null value")
    void setCreationInstant_nullValue() {
        manifest.setCreationInstant(instant);

        manifest.setCreationInstant(null);

        assertThat(manifest.getCreationInstant())
                .isNull();
    }

    @Test
    @DisplayName("getLastModifiedInstant shall return null by default")
    void getLastModifiedInstant() {
        assertThat(manifest.getLastModifiedInstant())
                .isNull();
    }

    @Test
    @DisplayName("setLastModifiedInstant shall accept non null values")
    void setLastModifiedInstant() {
        manifest.setLastModifiedInstant(instant);

        assertThat(manifest.getLastModifiedInstant())
                .isEqualTo(instant);
    }

    @Test
    @DisplayName("setLastModifiedInstant shall accept null value")
    void setLastModifiedInstant_nullValue() {
        manifest.setLastModifiedInstant(instant);
        manifest.setLastModifiedInstant(null);

        assertThat(manifest.getLastModifiedInstant())
                .isNull();
    }

    @Test
    @DisplayName("getPropertyNames shall return only default values")
    void getPropertyNames() {
        assertThat(manifest.getPropertyNames())
                .containsOnly(StandardProperties.MANIFEST_VERSION, StandardProperties.STORAGE_FORMAT_VERSION, StandardProperties.VERSION);
    }

    @Test
    @DisplayName("getPropertyValue(String) shall return null when property does not exist")
    void getProperty_String() {
        assertThat(manifest.getProperty("xxx"))
                .isNull();
    }

    @Test
    @DisplayName("getPropertyValue_StringString shall return null when property does not exist and default value is null")
    void getProperty_StringString_default_null() {
        assertThat(manifest.getProperty("xxx"))
                .isNull();
    }

    @Test
    @DisplayName("getPropertyValue_StringString shall return the default when property does not exist")
    void getProperty_StringString_default_not_null() {
        assertThat(manifest.getProperty("xxx", "abc"))
                .isEqualTo("abc");
    }

    @Test
    @DisplayName("setProperty shall failed when readonly property name")
    void setProperty_readOnlyProperty() {
        assertThatThrownBy(
                () -> manifest.setProperty(StandardProperties.MANIFEST_VERSION, "3.0")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("setProperty shall not accept blank name")
    void setProperty_blankName() {
        assertThatThrownBy(
                () -> manifest.setProperty("   ", null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("setProperty shall accept null value")
    void setProperty_nullValue() {
        manifest.setProperty("xxx", null);

        assertThat(manifest.getProperty("xxx"))
                .isNull();
    }

    @Test
    @DisplayName("setProperty shall accept non null value")
    void setProperty_nonNullValue() {
        manifest.setProperty("xxx", "abc");

        assertThat(manifest.getProperty("xxx"))
                .isEqualTo("abc");
    }

    @Test
    @DisplayName("contains shall return true when property exists")
    void contains_property_exists() {
        manifest.setProperty("xxx", "abc");

        assertThat(
                manifest.contains("xxx")
        ).isTrue();
    }

    @Test
    @DisplayName("contains shall return false when property does not exist")
    void contains_property_not_exists() {
        assertThat(
                manifest.contains("xxx")
        ).isFalse();
    }

    @Test
    @DisplayName("contains shall return true when file entry exists")
    void contains_file_exists() {
        manifest.addFileEntry(new FileMetadata(
                "x/y/z",
                "text/plain",
                "0"
        ));

        assertThat(
                manifest.contains("x/y/z")
        ).isTrue();
    }

    @Test
    @DisplayName("contains shall return true when file entry does not exist")
    void contains_file_not_exists() {
        assertThat(
                manifest.contains("x/y/z")
        ).isFalse();
    }

    @Test
    @DisplayName("containsProperty shall return true when property exists")
    void containsProperty_exists() {
        manifest.setProperty("xxx", "abc");

        assertThat(
                manifest.containsProperty("xxx")
        ).isTrue();
    }

    @Test
    @DisplayName("containsProperty shall return false when property does not exist")
    void containsProperty_not_exist() {
        assertThat(
                manifest.containsProperty("xxx")
        ).isFalse();
    }

    @Test
    @DisplayName("containsFile shall return true when file exists")
    void containsFile_exist() {
        manifest.addFileEntry(new FileMetadata(
                "x/y/z",
                "text/plain",
                "0"
        ));

        assertThat(
                manifest.containsFile("x/y/z")
        ).isTrue();
    }

    @Test
    @DisplayName("containsFile shall return false when file does not exists")
    void containsFile_not_exist() {

        assertThat(
                manifest.containsFile("x/y/z")
        ).isFalse();
    }


    @Test
    @DisplayName("getFileEntry shall return the entry when it exists")
    void getFileEntry_exist() {
        manifest.addFileEntry(new FileMetadata(
                "x/y/z",
                "text/plain",
                "0"
        ));

        assertThat(manifest.getFileEntry("x/y/z"))
                .extracting("fullPath", "mediaType", "version")
                .contains("x/y/z",
                        "text/plain",
                        "0");
    }

    @Test
    @DisplayName("getFileEntry shall return null when the entry does not exist")
    void getFileEntry_not_exist() {
        assertThat(manifest.getFileEntry("x/y/z"))
                .isNull();
    }

    @Test
    void addFileEntry() {
        manifest.addFileEntry(new FileMetadata(
                "x/y/z",
                "text/plain",
                "0"
        ));

        assertThat(manifest.containsFileEntry("x/y/z"))
                .isTrue();
    }


    @Test
    @DisplayName("getFileEntries shall return empty set when no registered files")
    void getFileEntries_empty() {
        assertThat(manifest.getFileEntries())
                .isEmpty();
    }

    @Test
    @DisplayName("getFileEntries shall return registered files")
    void getFileEntries_filled() {
        manifest.addFileEntry(new FileMetadata(
                "x/y.txt",
                "text/plain",
                "0"
        ));
        manifest.addFileEntry(new FileMetadata(
                "x/z.json",
                "application/json",
                "1"
        ));


        assertThat(manifest.getFileEntries())
                .extracting("fullPath", "mediaType", "version")
                .contains(
                        Tuple.tuple("x/y.txt",
                                "text/plain",
                                "0"),
                        Tuple.tuple("x/z.json",
                                "application/json",
                                "1")
                );
    }

    @Test
    @DisplayName("getAsText shall be able to return a value when manifest is empty")
    void getAsText() {
        String text = manifest.getAsText();
        var node = objectMapper.readTree(text);
        assertThat(node.isObject()).isTrue();
    }

    @Test
    void content_empty() {
        var content = manifest.content();

        assertThat(content).hasSizeGreaterThan(0);
    }

    @Test
    void content_filled() {
        manifest.addFileEntry(new FileMetadata(
                "x/z.json",
                "application/json",
                "1"
        ));
        manifest.setProperty("xxx", "abc");
        var content = manifest.content();

        assertThat(content).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("clear() shall do nothing when manifest is empty")
    void clear_empty() {
        manifest.addFileEntry(new FileMetadata(
                "x/z.json",
                "application/json",
                "1"
        ));
        manifest.setProperty("xxx", "abc");
        manifest.setProperty(StandardProperties.VERSION, "3");

        manifest.clear();

        assertThat(manifest.getPropertyNames())
                .containsOnly(StandardProperties.MANIFEST_VERSION, StandardProperties.STORAGE_FORMAT_VERSION, StandardProperties.VERSION);

        assertThat(manifest.getProperty(StandardProperties.VERSION))
                .isEmpty();

        assertThat(manifest.getFileEntries())
                .isEmpty();
    }

    @Test
    @DisplayName("clear() shall reset values  when manifest is filled")
    void clear_filled() {
        manifest.clear();

        assertThat(manifest.getPropertyNames())
                .containsOnly(StandardProperties.MANIFEST_VERSION, StandardProperties.STORAGE_FORMAT_VERSION, StandardProperties.VERSION);

        assertThat(manifest.getFileEntries())
                .isEmpty();
    }

    @Test
    void load_empty() {
        var manifest2 = new Manifest();
        var content = manifest2.content();

        manifest.load(content);

        assertThat(manifest.getPropertyNames())
                .containsOnly(StandardProperties.MANIFEST_VERSION, StandardProperties.STORAGE_FORMAT_VERSION, StandardProperties.VERSION);

        assertThat(manifest.getProperty(StandardProperties.VERSION))
                .isEmpty();

        assertThat(manifest.getFileEntries())
                .isEmpty();
    }

    @Test
    void load_filled() {
        var manifest2 = new Manifest();
        manifest2.addFileEntry(new FileMetadata(
                "x/y.txt",
                "text/plain",
                "0"
        ));
        manifest2.addFileEntry(new FileMetadata(
                "x/z.json",
                "application/json",
                "1"
        ));
        manifest2.setProperty("xxx", "abc");
        manifest2.setProperty(StandardProperties.VERSION, "3");

        var content = manifest2.content();

        System.out.println( new String(content, StandardCharsets.UTF_8));

        manifest.load(content);

        assertThat(manifest.getPropertyNames())
                .containsOnly(StandardProperties.MANIFEST_VERSION, StandardProperties.STORAGE_FORMAT_VERSION, StandardProperties.VERSION, "xxx");

        assertThat(manifest.getProperty(StandardProperties.VERSION))
                .isEqualTo("3");
        assertThat(manifest.getProperty("xxx"))
                .isEqualTo("abc");

        assertThat(manifest.getFileEntries())
                .extracting("fullPath", "mediaType", "version")
                .contains(
                        Tuple.tuple("x/y.txt",
                                "text/plain",
                                "0"),
                        Tuple.tuple("x/z.json",
                                "application/json",
                                "1")
                );
    }
}