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

import io.jmix.core.commons.events.Subscription;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.UiProperties;
import io.jmix.ui.components.ComponentContainer;
import io.jmix.ui.components.UploadField;

import java.util.Set;
import java.util.function.Consumer;

public abstract class WebAbstractUploadComponent<T extends com.vaadin.ui.AbstractComponent>
        extends WebAbstractComponent<T>
        implements UploadField {

    protected static final int BYTES_IN_MEGABYTE = 1048576;

    protected long fileSizeLimit = 0;

    protected Set<String> permittedExtensions;
    protected DropZone dropZone;
    protected ComponentContainer pasteZone;
    protected String dropZonePrompt;

    @Override
    public long getFileSizeLimit() {
        return fileSizeLimit;
    }

    @Override
    public Set<String> getPermittedExtensions() {
        return permittedExtensions;
    }

    @Override
    public void setPermittedExtensions(Set<String> permittedExtensions) {
        this.permittedExtensions = permittedExtensions;
    }

    protected String getFileSizeLimitString() {
        String fileSizeLimitString;
        if (fileSizeLimit > 0) {
            if (fileSizeLimit % BYTES_IN_MEGABYTE == 0) {
                fileSizeLimitString = String.valueOf(fileSizeLimit / BYTES_IN_MEGABYTE);
            } else {
                DatatypeRegistry datatypeRegistry = beanLocator.get(DatatypeRegistry.NAME);
                Datatype<Double> doubleDatatype = datatypeRegistry.getNN(Double.class);
                double fileSizeInMb = fileSizeLimit / ((double) BYTES_IN_MEGABYTE);

                CurrentAuthentication currentAuthentication = beanLocator.get(CurrentAuthentication.NAME);
                fileSizeLimitString = doubleDatatype.format(fileSizeInMb, currentAuthentication.getLocale());
            }
        } else {
            fileSizeLimitString = String.valueOf(beanLocator.get(UiProperties.class).getMaxUploadSizeMb());
        }
        return fileSizeLimitString;
    }

    @Override
    public DropZone getDropZone() {
        return dropZone;
    }

    @Override
    public void setDropZone(DropZone dropZone) {
        this.dropZone = dropZone;
    }

    @Override
    public void setPasteZone(ComponentContainer pasteZone) {
        this.pasteZone = pasteZone;
    }

    @Override
    public ComponentContainer getPasteZone() {
        return pasteZone;
    }

    @Override
    public String getDropZonePrompt() {
        return dropZonePrompt;
    }

    @Override
    public void setDropZonePrompt(String dropZonePrompt) {
        this.dropZonePrompt = dropZonePrompt;
    }

    @Override
    public Subscription addFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        return getEventHub().subscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public void removeFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        unsubscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        return getEventHub().subscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public void removeFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        unsubscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        return getEventHub().subscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void removeFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        unsubscribe(FileUploadErrorEvent.class, listener);
    }
}
