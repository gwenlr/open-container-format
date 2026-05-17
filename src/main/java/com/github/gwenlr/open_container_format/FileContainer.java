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

import java.util.Map;
import java.util.Set;

/**
 * A container of file
 */
public interface FileContainer {

    @Nullable String getManifestProperty(@NonNull String name);

    @NonNull FileContainer setManifestProperty(@NonNull String name, @NonNull String value);

    @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, @NonNull String content, @NonNull String version);

    default @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, @NonNull String content) {
        return addFileEntry(path, content, mediaType, "0");
    }

    @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, byte @NonNull [] content, @NonNull String version);

    default @NonNull FileContainer addFileEntry(@NonNull String path, @NonNull String mediaType, byte @NonNull [] content) {
        return addFileEntry(path, mediaType, content, "0");
    }

    @Nullable FileMetadata getFileMetadata(@NonNull String fullPath);

    boolean containsManifestProperty(@NonNull String name);

    @NonNull Map<String,String> getManifestProperties();

    boolean containsFileEntry(@NonNull String fullPath);

    @Nullable String getFileContentAsString(@NonNull String fullPath);

    byte[] getFileContentAsBinary(@NonNull String fullPath);

    void loadFromBinaryContent(byte @NonNull [] binaryContent);

    byte @NonNull [] toBinaryContent();

    @NonNull Set<String> getFilePaths();
}
