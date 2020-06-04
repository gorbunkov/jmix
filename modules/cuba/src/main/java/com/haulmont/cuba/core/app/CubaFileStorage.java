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
package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.FileStorage;
import io.jmix.core.TimeSource;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.fsfilestorage.FileSystemFileStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component(FileStorageAPI.NAME)
public class CubaFileStorage implements FileStorageAPI {

    private static final Logger log = LoggerFactory.getLogger(CubaFileStorage.class);

    @Autowired
    protected FileSystemFileStorage delegate;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected TimeSource timeSource;

    @Override
    public long saveStream(FileDescriptor fileDescr, InputStream inputStream) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.saveStream(toURI(fileDescr), inputStream);
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void saveFile(FileDescriptor fileDescr, byte[] data) throws FileStorageException {
        checkNotNullArgument(data, "File content is null");
        saveStream(fileDescr, new ByteArrayInputStream(data));
    }

    @Override
    public void removeFile(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            delegate.removeFile(toURI(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public InputStream openStream(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.openStream(toURI(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public byte[] loadFile(FileDescriptor fileDescr) throws FileStorageException {
        InputStream inputStream = openStream(fileDescr);
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, fileDescr.getId().toString(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public boolean fileExists(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.fileExists(toURI(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public URI toURI(FileDescriptor fileDescriptor) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fileDescriptor.getCreateDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        Path datePath = Paths.get(String.valueOf(year),
                StringUtils.leftPad(String.valueOf(month), 2, '0'),
                StringUtils.leftPad(String.valueOf(day), 2, '0'));

        StringBuilder reference = new StringBuilder(datePath.toString())
                .append(";").append(URLEncodeUtils.encodeUtf8(fileDescriptor.getName()));
        try {
            return new URI(reference.toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns an adapter to use this storage as {@link FileStorage}.
     */
    public FileStorage<FileDescriptor, Object> asFileStorage() {
        return new FileStorage<FileDescriptor, Object>() {

            @Override
            public Class<FileDescriptor> getReferenceType() {
                return FileDescriptor.class;
            }

            @Override
            public FileDescriptor createReference(Object fileInfo) {
                String fileName = (String) fileInfo;

                FileDescriptor fileDescriptor = metadata.create(FileDescriptor.class);
                fileDescriptor.setName(fileName);
                fileDescriptor.setExtension(FilenameUtils.getExtension(fileName));
                fileDescriptor.setCreateDate(timeSource.currentTimestamp());

                return fileDescriptor;
            }

            @Override
            public String getFileInfo(FileDescriptor reference) {
                return delegate.getFileInfo(toURI(reference));
            }

            @Override
            public long saveStream(FileDescriptor reference, InputStream inputStream) {
                return delegate.saveStream(toURI(reference), inputStream);
            }

            @Override
            public InputStream openStream(FileDescriptor reference) {
                return delegate.openStream(toURI(reference));
            }

            @Override
            public void removeFile(FileDescriptor reference) {
                delegate.removeFile(toURI(reference));
            }

            @Override
            public boolean fileExists(FileDescriptor reference) {
                return delegate.fileExists(toURI(reference));
            }
        };
    }

    protected void checkFileDescriptor(FileDescriptor fd) {
        if (fd == null || fd.getCreateDate() == null) {
            throw new IllegalArgumentException("A FileDescriptor instance with populated 'createDate' attribute must be provided");
        }
    }

}