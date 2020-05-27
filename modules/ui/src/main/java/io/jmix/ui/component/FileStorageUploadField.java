/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.ui.upload.TemporaryStorage;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Component for uploading files to file storage.
 *
 * @param <T> file reference type
 */
public interface FileStorageUploadField<T> extends SingleFileUploadField, Field<T> {
    String NAME = "fileStorageUpload";

    /**
     * Defines when file will be placed into FileStorage.
     */
    enum FileStoragePutMode {
        /**
         * User have to put file into FileStorage manually.
         */
        MANUAL,
        /**
         * File will be placed into FileStorage right after upload.
         */
        IMMEDIATE
    }

    /**
     * Get id for uploaded file in {@link TemporaryStorage}.
     *
     * @return File Id.
     */
    UUID getFileId();

    String getFileName();

    /**
     * Returns reference instance of uploaded file. Can be null.
     *
     * @return file reference instance or null
     */
    @Nullable
    T getReference();

    /**
     * Set mode which determines when file will be put into FileStorage.
     */
    void setMode(FileStoragePutMode mode);

    /**
     * @return mode which determines when file will be put into FileStorage.
     */
    FileStoragePutMode getMode();

    void setFileStorageName(String fileStorageName);

    String getFileStorageName();
}
