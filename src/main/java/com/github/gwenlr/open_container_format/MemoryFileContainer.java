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

import kotlin.text.Charsets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.github.gwenlr.open_container_format.StandardProperties.MANIFEST_FULL_PATH;

public class MemoryFileContainer implements FileContainer {

    private final Manifest manifest = new Manifest();
    private final Map<String, ContainerFileEntry> fileMaps = new HashMap<>();

    public MemoryFileContainer() {
    }

    public MemoryFileContainer(byte @NonNull [] content) {
        loadFromBinaryContent(content);
    }

    @Override
    public @Nullable String getManifestProperty(@NonNull String name) {
        return manifest.getProperty(name);
    }

    @Override
    public @NonNull FileContainer setManifestProperty(@NonNull String name, @NonNull String value) {
        manifest.setProperty(name, value);
        return this;
    }

    @Override
    public @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, @NonNull String content, @NonNull String version) {
        var binaryContent = content.getBytes(Charsets.UTF_8);
        addFileEntry(path, mediaType, binaryContent, version);
        return this;
    }

    @Override
    public @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, byte @NonNull [] content, @NonNull String version) {
        var manifestFileEntry = new FileMetadata(path, mediaType, version);
        manifest.addFileEntry(manifestFileEntry);
        fileMaps.put(path, new MemoryContainerFileEntry(path, content));
        return this;
    }

    @Override
    public byte @NonNull [] toBinaryContent() {
        try (var binaryOut = new ByteArrayOutputStream();
             var zipOut = new ZipOutputStream(binaryOut)) {
            writeToZip(zipOut);
            return binaryOut.toByteArray();

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void writeToZip(@NonNull ZipOutputStream zipOut) throws IOException {
        writeEntry(zipOut, manifest);

        for (var entry : fileMaps.values()) {
            writeEntry(zipOut, entry);
        }
    }

    private void writeEntry(ZipOutputStream zipOut, ContainerFileEntry entry) throws IOException {
        var zipEntry = new ZipEntry(entry.fullPath());
        zipOut.putNextEntry(zipEntry);
        zipOut.write(entry.content());
        zipOut.closeEntry();
    }

    @Override
    public void loadFromBinaryContent(byte @NonNull [] binaryContent) {
        this.manifest.clear();
        this.fileMaps.clear();

        try (var inputStream = new ByteArrayInputStream(binaryContent);
             var zipIn = new ZipInputStream(inputStream)) {

            loadFromZip(zipIn);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void loadFromZip(@NonNull ZipInputStream zipIn) throws IOException {
        var zipEntry = zipIn.getNextEntry();

        loadManifest(zipIn, zipEntry);

        while ((zipEntry = zipIn.getNextEntry()) != null) {
            loadFileEntry(zipIn, zipEntry);
        }
    }


    private void loadManifest(@NonNull ZipInputStream zipIn, @Nullable ZipEntry entry) throws IOException {
        if (entry == null) {
            throw new IOException("Manifest entry is missing");
        }

        var fullPath = entry.getName();
        if (!MANIFEST_FULL_PATH.equals(fullPath)) {
            throw new InvalidFileFormatException("First entry is not manifest file: " + fullPath);
        }

        var content = zipIn.readAllBytes();
        manifest.load(content);
    }

    private void loadFileEntry(@NonNull ZipInputStream zipIn, @NonNull ZipEntry zipEntry) throws IOException {
        var entry = readEntry(zipIn, zipEntry);
        fileMaps.put(entry.fullPath(), entry);
    }

    private ContainerFileEntry readEntry(@NonNull ZipInputStream zipIn, @NonNull ZipEntry entry) throws IOException {
        var fullPath = entry.getName();
        var content = zipIn.readAllBytes();
        return new MemoryContainerFileEntry(fullPath, content);
    }

    @Override
    public @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, @NonNull String content) {
        return FileContainer.super.addFileEntry(path, mediaType, content);
    }

    @Override
    public @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, byte @NonNull [] content) {
        return FileContainer.super.addFileEntry(path, mediaType, content);
    }

    @Override
    public boolean containsManifestProperty(@NonNull String name) {
        return manifest.containsProperty(name);
    }

    @Override
    public @NonNull Map<String, String> getManifestProperties() {
        return manifest.exportProperties();
    }

    @Override
    public boolean containsFileEntry(@NonNull String fullPath) {
        return fileMaps.containsKey(fullPath);
    }

    @Override
    public @Nullable String getFileContentAsString(@NonNull String fullPath) {
        var binaryContent = getFileContentAsBinary(fullPath);
        return binaryContent == null ? null : new String(binaryContent, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getFileContentAsBinary(@NonNull String fullPath) {
        var fileEntry = fileMaps.get(fullPath);
        if (fileEntry == null)
            return null;
        return fileEntry.content();
    }

    @Override
    public @Nullable FileMetadata getFileMetadata(@NonNull String fullPath) {
        return manifest.getFileEntry(fullPath);
    }

    @Override
    public @NonNull Set<String> getFilePaths() {
        return fileMaps.keySet();
    }
}
