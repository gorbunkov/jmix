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
import io.jmix.core.CoreProperties;
import io.jmix.core.UuidProvider;
import io.jmix.ui.UiProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

public class ByteArrayDataProvider implements ExportDataProvider {

    private static Logger log = LoggerFactory.getLogger(ByteArrayDataProvider.class);

    private Supplier<InputStream> supplier;

    public ByteArrayDataProvider(byte[] data) {
        int saveExportedByteArrayDataThresholdBytes =
                AppBeans.get(UiProperties.class).getSaveExportedByteArrayDataThresholdBytes();

        if (data.length >= saveExportedByteArrayDataThresholdBytes) {
            // save to temp
            File file = saveToTempStorage(data);
            this.supplier = () -> readFromTempStorage(file);
        } else {
            this.supplier = () -> new ByteArrayInputStream(data);
        }
    }

    protected File saveToTempStorage(byte[] data) {
        UUID uuid = UuidProvider.createUuid();

        String tempDir = AppBeans.get(CoreProperties.class).getTempDir();

        File dir = new File(tempDir);
        File file = new File(dir, uuid.toString());

        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write byte data to temp file", e);
        }

        log.debug("Stored {} bytes of data to temporary file {}", data.length, file.getAbsolutePath());

        return file;
    }

    @Nullable
    protected InputStream readFromTempStorage(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.warn("Unable to read temp file " + file.getAbsolutePath());
            return null;
        }
    }

    @Override
    public InputStream provide() {
        return supplier.get();
    }
}