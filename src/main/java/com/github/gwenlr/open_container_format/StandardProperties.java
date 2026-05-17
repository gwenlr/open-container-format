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

//TODO find a better name

/**
 * Names of standard properties
 */
public final class StandardProperties {
    private StandardProperties() {
    }

    public static final String MANIFEST_VERSION = "manifest-version";
    public static final String VERSION = "version";
    public static final String CREATION = "creation-date";
    public static final String LAST_MODIFIED = "last-modified-date";
    public static final String STORAGE_FORMAT_VERSION = "storage-format-version";
    public static final String FILE_ENTRIES = "file-entries";

    public static final String FILE_ENTRY_FULL_PATH = "full-path";
    public static final String FILE_ENTRY_MEDIA_TYPE = "media-type";
    public static final String FILE_ENTRY_VERSION = "version";

    public static final String MANIFEST_FULL_PATH = "/META-INF/manifest";

}
