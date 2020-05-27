/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package io.jmix.ui.export;

import io.jmix.core.AppBeans;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;

import java.io.InputStream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Data provider for FileDescriptor
 */
public class FileDataProvider<R> implements ExportDataProvider {

    protected R fileReference;
    protected FileStorage<R, ?> fileStorage;

    public FileDataProvider(R fileReference) {
        this(fileReference, AppBeans.get(FileStorageLocator.class).getDefault());
    }

    public FileDataProvider(R fileReference, FileStorage<R, ?> fileStorage) {
        checkNotNullArgument(fileReference, "Null file reference");
        this.fileReference = fileReference;
        this.fileStorage = fileStorage;
    }

    @Override
    public InputStream provide() {
        return fileStorage.openStream(fileReference);
    }
}