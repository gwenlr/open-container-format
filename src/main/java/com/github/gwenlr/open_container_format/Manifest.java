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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static com.github.gwenlr.open_container_format.StandardProperties.*;

/**
 * A manifest contains metadata about the container and the content of the container
 */
class Manifest implements ContainerFileEntry {

    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, FileMetadata> fileEntries = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> READ_ONLY_PROPERTY_NAMES = Set.of(MANIFEST_VERSION, STORAGE_FORMAT_VERSION);
    private static final Set<String> NOT_EXPORTABLE_PROPERTY_NAMES = Set.of(MANIFEST_VERSION, STORAGE_FORMAT_VERSION);

    public Manifest() {
        resetContent();
    }

    private void resetContent() {
        properties.clear();
        fileEntries.clear();
        properties.put(MANIFEST_VERSION, "1.0");
        properties.put(STORAGE_FORMAT_VERSION, "1");
        properties.put(VERSION, "");
    }

    @Override
    public @NonNull String fullPath() {
        return MANIFEST_FULL_PATH;
    }

    public @NonNull String getStorageFormatVersion() {
        return properties.get(STORAGE_FORMAT_VERSION);
    }

    public @NonNull String getVersion() {
        return properties.get(VERSION);
    }

    public void setVersion(@NonNull String version) {
        properties.put(VERSION, version);
    }

    public @Nullable Instant getCreationInstant() {
        String rawValue = properties.get(CREATION);
        return rawValue == null ? null : Instant.parse(rawValue);
    }

    public void setCreationInstant(@Nullable Instant creationInstant) {
        if (creationInstant == null) {
            properties.remove(CREATION);
        } else {
            properties.put(CREATION, creationInstant.toString());
        }
    }

    public @Nullable Instant getLastModifiedInstant() {
        String rawValue = properties.get(LAST_MODIFIED);
        return rawValue == null ? null : Instant.parse(rawValue);
    }

    public void setLastModifiedInstant(@Nullable Instant lastModifiedInstant) {
        if (lastModifiedInstant == null) {
            properties.remove(LAST_MODIFIED);
        } else {
            properties.put(LAST_MODIFIED, lastModifiedInstant.toString());
        }
    }

    public @Nullable Set<String> getPropertyNames() {
        return properties.keySet();
    }

    public @Nullable String getProperty(@NonNull String name) {
        return properties.get(name);
    }

    public @Nullable String getProperty(@NonNull String name, @Nullable String defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    public boolean contains(@NonNull String name) {
        return properties.containsKey(name) || fileEntries.containsKey(name);
    }

    public boolean containsProperty(@NonNull String name) {
        return properties.containsKey(name);
    }

    public boolean containsFile(@NonNull String fullPath) {
        return fileEntries.containsKey(fullPath);
    }

    public void setProperty(@NonNull String name, @Nullable String value) {
        checkPropertyName(name);
        assertNotReadOnlyProperty(name);
        properties.put(name, value);
    }

    private void checkPropertyName(@NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    private void assertNotReadOnlyProperty(String name) {
        if (READ_ONLY_PROPERTY_NAMES.contains(name)) {
            throw new IllegalArgumentException("property " + name + " is read-only");
        }
    }

    public @Nullable FileMetadata getFileEntry(@NonNull String fullPath) {
        return fileEntries.get(fullPath);
    }

    public void addFileEntry(@NonNull FileMetadata fileEntry) {
        checkFileEntryFullPath(fileEntry.fullPath());
        fileEntries.put(fileEntry.fullPath(), fileEntry);
    }

    private void checkFileEntryFullPath(@NonNull String fullPath) {
        if (fullPath.isBlank()) {
            throw new IllegalArgumentException("fullPath must not be blank");
        }
    }

    public boolean containsFileEntry(@NonNull String fullPath) {
        return fileEntries.containsKey(fullPath);
    }

    public @NonNull Set<FileMetadata> getFileEntries() {
        return new HashSet<>(fileEntries.values());
    }

    public @NonNull String getAsText() {
        var json = toJson();
        return mapper.writeValueAsString(json);
    }

    @Override
    public byte @NonNull [] content() {
        var textContent = getAsText();
        return textContent.getBytes(StandardCharsets.UTF_8);
    }

    private @NonNull ObjectNode toJson() {
        var nodeFactory = mapper.getNodeFactory();
        var rootNode = nodeFactory.objectNode();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            rootNode.put(entry.getKey(), entry.getValue());
        }

        var fileEntryArrayNode = rootNode.putArray("file-entries");
        for (Map.Entry<String, FileMetadata> entry : fileEntries.entrySet()) {
            appendFileEntry(entry.getValue(), fileEntryArrayNode);
        }

        return rootNode;
    }

    private void appendFileEntry(@NonNull FileMetadata entry, @NonNull ArrayNode rootNode) {
        var fileEntryNode = rootNode.objectNode();
        fileEntryNode.put(FILE_ENTRY_FULL_PATH, entry.fullPath());
        fileEntryNode.put(FILE_ENTRY_MEDIA_TYPE, entry.mediaType());
        fileEntryNode.put(FILE_ENTRY_VERSION, entry.version());
        rootNode.add(fileEntryNode);
    }

    public void clear() {
        resetContent();
    }

    public void load(byte[] content) {
        resetContent();
        var rootNode = mapper.readTree(content);
        if (!rootNode.isObject()) {
            throw new InvalidFileFormatException("Root node shall be an object");
        }

        for (Map.Entry<String, JsonNode> entry : rootNode.properties()) {
            if (entry.getKey().equals(FILE_ENTRIES)) {
                loadFileEntries(entry.getValue().asArray());
            } else {
                loadPropertyEntry(entry.getKey(), entry.getValue().asString());
            }
        }
    }

    private void loadFileEntries(@NonNull ArrayNode fileEntriesArrayNode) {
        for (JsonNode node : fileEntriesArrayNode) {
            var manifestFileEntry = toManifestFileEntry(node.asObject());
            addFileEntry(manifestFileEntry);
        }
    }

    private @NonNull FileMetadata toManifestFileEntry(@NonNull ObjectNode fileEntryNode) {
        var fullPath = fileEntryNode.get(FILE_ENTRY_FULL_PATH).asString();
        var mediaType = fileEntryNode.get(FILE_ENTRY_MEDIA_TYPE).asString();
        var version = fileEntryNode.get(FILE_ENTRY_VERSION).asString();
        return new FileMetadata(fullPath, mediaType, version);
    }

    private void loadPropertyEntry(@NonNull String name, @NonNull String value) {
        checkPropertyName(name);
        properties.put(name, value);
    }

    @NonNull Map<String,String> exportProperties() {
        var exportProperties = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if( exportableProperty(entry.getKey())) {
                exportProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return exportProperties;
    }

    private boolean exportableProperty(@NonNull String name) {
        return  ! NOT_EXPORTABLE_PROPERTY_NAMES.contains(name);
    }
}