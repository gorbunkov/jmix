/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.components.impl;

import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.FileDescriptor;
import io.jmix.core.AppBeans;
import io.jmix.core.FileLoader;
import io.jmix.core.FileStorageException;
import io.jmix.ui.components.FileDescriptorResource;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class WebFileDescriptorResource extends WebAbstractStreamSettingsResource
        implements WebResource, FileDescriptorResource {

    protected static final String FILE_STORAGE_EXCEPTION_MESSAGE = "Can't create FileDescriptorResource. " +
            "An error occurred while obtaining a file from the storage";

    protected FileDescriptor fileDescriptor;

    protected String mimeType;

    @Override
    public FileDescriptorResource setFileDescriptor(FileDescriptor fileDescriptor) {
        Preconditions.checkNotNullArgument(fileDescriptor);

        this.fileDescriptor = fileDescriptor;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    @Override
    protected void createResource() {
        resource = new StreamResource(() -> {
            try {
                return AppBeans.get(FileLoader.class).openStream(fileDescriptor);
            } catch (FileStorageException e) {
                throw new RuntimeException(FILE_STORAGE_EXCEPTION_MESSAGE, e);
            }
        }, getResourceName());

        StreamResource streamResource = (StreamResource) this.resource;

        streamResource.setCacheTime(cacheTime);
        streamResource.setBufferSize(bufferSize);
    }

    protected String getResourceName() {
        StringBuilder name = new StringBuilder();

        String fullName = StringUtils.isNotEmpty(fileName)
                ? fileName
                : fileDescriptor.getName();
        String baseName = FilenameUtils.getBaseName(fullName);

        if (StringUtils.isEmpty(baseName)) {
            return UUID.randomUUID().toString();
        }

        String extension;
        if (StringUtils.isEmpty(fileName)
                && StringUtils.isNotEmpty(fileDescriptor.getExtension())) {
            extension = fileDescriptor.getExtension();
        } else {
            extension = FilenameUtils.getExtension(fullName);
        }

        return name.append(baseName)
                .append('-')
                .append(UUID.randomUUID().toString())
                .append('.')
                .append(extension)
                .toString();
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;

        if (resource != null) {
            ((StreamResource) resource).setMIMEType(mimeType);
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}
