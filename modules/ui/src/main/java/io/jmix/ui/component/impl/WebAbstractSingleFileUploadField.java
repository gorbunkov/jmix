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

package io.jmix.ui.component.impl;

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.component.Window;
import io.jmix.ui.components.impl.JmixFileUploadField;
import io.jmix.ui.export.ExportDisplay;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;
import static io.jmix.ui.upload.FileUploadTypesHelper.convertToMIME;

public abstract class WebAbstractSingleFileUploadField<R> extends WebV8AbstractField<JmixFileUploadField, String, R>
        implements SingleFileUploadField, InitializingBean {

    protected static final int BYTES_IN_MEGABYTE = 1048576;

    protected ExportDisplay exportDisplay;
    protected Messages messages;
    protected Supplier<InputStream> contentProvider;

    protected long fileSizeLimit = 0;
    protected Set<String> permittedExtensions;
    protected String accept;
    protected String fileName;

    protected DropZone dropZone;
    protected ComponentContainer pasteZone;
    protected String dropZonePrompt;

    public WebAbstractSingleFileUploadField() {
        component = createComponent();
    }

    protected JmixFileUploadField createComponent() {
        return new JmixSingleFileUploadField();
    }

    @Autowired
    public void setExportDisplay(ExportDisplay exportDisplay) {
        this.exportDisplay = exportDisplay;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
        attachValueChangeListener(component);
    }

    protected void initComponent() {
        component.setClearButtonCaption(messages.getMessage("FileUploadField.clearButtonCaption"));
        component.setFileNotSelectedMessage(messages.getMessage("FileUploadField.fileNotSelected"));

        component.setProgressWindowCaption(messages.getMessage("upload.uploadingProgressTitle"));
        component.setUnableToUploadFileMessage(messages.getMessage("upload.unableToUploadFile"));
        component.setCancelButtonCaption(messages.getMessage("upload.cancel"));
        component.setUploadButtonCaption(messages.getMessage("upload.submit"));
        component.setDropZonePrompt(messages.getMessage("upload.singleDropZonePrompt"));
        component.setUploadButtonDescription(null);

        component.setFileSizeLimit(getActualFileSizeLimit());

        component.setUploadReceiver(this::receiveUpload);

        component.addUploadStartedListener(this::onUploadStarted);

        component.addUploadFinishedListener(this::onUploadFinished);

        component.addUploadSucceededListener(this::onUploadSucceeded);

        component.addUploadFailedListener(this::onUploadFailed);

        component.addFileSizeLimitExceededListener(this::onFileSizeLimitExceeded);

        component.addFileExtensionNotAllowedListener(this::onFileExtensionNotAllowed);

        component.addFileNameClickListener(this::onFileNameClick);
        component.setClearButtonListener(this::clearButtonClicked);
        component.setRequiredError(null);
    }

    protected abstract OutputStream receiveUpload(String fileName, String MIMEType);

    protected void onUploadStarted(JmixFileUpload.StartedEvent event) {
        fireFileUploadStart(event.getFileName(), event.getContentLength());
    }

    protected void onUploadFinished(JmixFileUpload.FinishedEvent event) {
        fireFileUploadFinish(event.getFileName(), event.getContentLength());
    }

    protected void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        fireFileUploadSucceed(event.getFileName(), event.getContentLength());
    }

    protected void onUploadFailed(JmixFileUpload.FailedEvent event) {
        fireFileUploadError(event.getFileName(), event.getContentLength(), event.getReason());
    }

    protected void onFileSizeLimitExceeded(JmixFileUpload.FileSizeLimitExceededEvent e) {
        Notifications notifications = getScreenContext(this).getNotifications();

        notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(messages.formatMessage("upload.fileTooBig.message", e.getFileName(),
                        getFileSizeLimitString()))
                .show();
    }

    protected void onFileExtensionNotAllowed(JmixFileUpload.FileExtensionNotAllowedEvent e) {
        Notifications notifications = getScreenContext(this).getNotifications();

        notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(messages.formatMessage("upload.fileIncorrectExtension.message", e.getFileName()))
                .show();
    }

    protected void onFileNameClick(Button.ClickEvent e) {
    }

    public String getFileName() {
        if (fileName == null) {
            return null;
        }

        String[] strings = fileName.split("[/\\\\]");
        return strings[strings.length - 1];
    }

    @Override
    public Subscription addFileUploadSucceedListener(Consumer<SingleFileUploadField.FileUploadSucceedEvent> listener) {
        return getEventHub().subscribe(SingleFileUploadField.FileUploadSucceedEvent.class, listener);
    }

    @Override
    public void removeFileUploadSucceedListener(Consumer<SingleFileUploadField.FileUploadSucceedEvent> listener) {
        unsubscribe(SingleFileUploadField.FileUploadSucceedEvent.class, listener);
    }

    @Override
    public String getAccept() {
        return accept;
    }

    @Override
    public void setAccept(String accept) {
        if (!Objects.equals(accept, getAccept())) {
            this.accept = accept;
            component.setAccept(convertToMIME(accept));
        }
    }

    @Override
    public void setDropZone(DropZone dropZone) {
        this.dropZone = dropZone;

        if (dropZone == null) {
            component.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();
            if (target instanceof Window.Wrapper) {
                target = ((Window.Wrapper) target).getWrappedWindow();
            }

            Component vComponent = target.unwrapComposition(Component.class);
            component.setDropZone(vComponent);
        }
    }

    @Override
    public void setPasteZone(ComponentContainer pasteZone) {
        this.pasteZone = pasteZone;

        component.setPasteZone(pasteZone != null ? pasteZone.unwrapComposition(Component.class) : null);
    }

    @Override
    public void setDropZonePrompt(String dropZonePrompt) {
        this.dropZonePrompt = dropZonePrompt;

        component.setDropZonePrompt(dropZonePrompt);
    }

    @Override
    public long getFileSizeLimit() {
        return fileSizeLimit;
    }

    protected long getActualFileSizeLimit() {
        if (fileSizeLimit > 0) {
            return fileSizeLimit;
        } else {
            int maxUploadSizeMb = beanLocator.get(UiProperties.class).getMaxUploadSizeMb();

            return maxUploadSizeMb * BYTES_IN_MEGABYTE;
        }
    }

    @Override
    public Set<String> getPermittedExtensions() {
        return permittedExtensions;
    }

    protected String getFileSizeLimitString() {
        String fileSizeLimitString;
        if (fileSizeLimit > 0) {
            if (fileSizeLimit % BYTES_IN_MEGABYTE == 0) {
                fileSizeLimitString = String.valueOf(fileSizeLimit / BYTES_IN_MEGABYTE);
            } else {
                DatatypeRegistry datatypeRegistry = beanLocator.get(DatatypeRegistry.NAME);
                Datatype<Double> doubleDatatype = datatypeRegistry.get(Double.class);
                double fileSizeInMb = fileSizeLimit / ((double) BYTES_IN_MEGABYTE);

                CurrentAuthentication currentAuthentication = beanLocator.get(CurrentAuthentication.NAME);
                fileSizeLimitString = doubleDatatype.format(fileSizeInMb, currentAuthentication.getLocale());
            }
        } else {
            fileSizeLimitString = String.valueOf(beanLocator.get(UiProperties.class).getMaxUploadSizeMb());
        }
        return fileSizeLimitString;
    }

    protected void internalValueChanged(Object newValue) {
    }

    @Override
    public DropZone getDropZone() {
        return dropZone;
    }

    @Override
    public ComponentContainer getPasteZone() {
        return pasteZone;
    }

    @Override
    public String getDropZonePrompt() {
        return dropZonePrompt;
    }

    protected void clearButtonClicked(@SuppressWarnings("unused") Button.ClickEvent clickEvent) {
        BeforeValueClearEvent beforeValueClearEvent = new BeforeValueClearEvent(this);
        publish(BeforeValueClearEvent.class, beforeValueClearEvent);

        if (!beforeValueClearEvent.isClearPrevented()) {
            setValue(null);
            fileName = null;
        }

        AfterValueClearEvent afterValueClearEvent = new AfterValueClearEvent(this,
                !beforeValueClearEvent.isClearPrevented());
        publish(AfterValueClearEvent.class, afterValueClearEvent);
    }

    protected void fireFileUploadStart(String fileName, long contentLength) {
        publish(FileUploadStartEvent.class, new FileUploadStartEvent(this, fileName, contentLength));
    }

    protected void fireFileUploadFinish(String fileName, long contentLength) {
        publish(FileUploadFinishEvent.class, new FileUploadFinishEvent(this, fileName, contentLength));
    }

    protected void fireFileUploadError(String fileName, long contentLength, Exception cause) {
        publish(FileUploadErrorEvent.class, new FileUploadErrorEvent(this, fileName, contentLength, cause));
    }

    protected void fireFileUploadSucceed(String fileName, long contentLength) {
        publish(FileUploadSucceedEvent.class, new FileUploadSucceedEvent(this, fileName, contentLength));
    }

    @Override
    public void setContentProvider(Supplier<InputStream> contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    public Supplier<InputStream> getContentProvider() {
        return contentProvider;
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

    @Override
    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;

        component.setFileSizeLimit(fileSizeLimit);
    }

    @Override
    public boolean isShowFileName() {
        return component.isShowFileName();
    }

    @Override
    public void setShowFileName(boolean showFileName) {
        component.setShowFileName(showFileName);
        if (showFileName) {
            component.setFileNameButtonCaption(fileName);
        }
    }

    @Override
    public void setPermittedExtensions(Set<String> permittedExtensions) {
        if (permittedExtensions != null) {
            this.permittedExtensions = permittedExtensions.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } else {
            this.permittedExtensions = null;
        }
        component.setPermittedExtensions(this.permittedExtensions);
    }

    @Override
    public void setShowClearButton(boolean showClearButton) {
        component.setShowClearButton(showClearButton);
    }

    @Override
    public boolean isShowClearButton() {
        return component.isShowClearButton();
    }

    @Override
    public void setClearButtonCaption(String caption) {
        component.setClearButtonCaption(caption);
    }

    @Override
    public String getClearButtonCaption() {
        return component.getClearButtonCaption();
    }

    @Override
    public void setClearButtonIcon(String icon) {
        if (icon != null) {
            IconResolver iconResolver = beanLocator.get(IconResolver.NAME);
            Resource iconResource = iconResolver.getIconResource(icon);
            component.setClearButtonIcon(iconResource);
        } else {
            component.setClearButtonIcon(null);
        }
    }

    @Override
    public String getClearButtonIcon() {
        return component.getClearButtonIcon();
    }

    @Override
    public void setClearButtonDescription(String description) {
        component.setClearButtonDescription(description);
    }

    @Override
    public String getClearButtonDescription() {
        return component.getClearButtonDescription();
    }

    @Override
    public Subscription addBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener) {
        return getEventHub().subscribe(BeforeValueClearEvent.class, listener);
    }

    @Override
    public void removeBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener) {
        unsubscribe(BeforeValueClearEvent.class, listener);
    }

    @Override
    public Subscription addAfterValueClearListener(Consumer<AfterValueClearEvent> listener) {
        return getEventHub().subscribe(AfterValueClearEvent.class, listener);
    }

    @Override
    public void removeAfterValueClearListener(Consumer<AfterValueClearEvent> listener) {
        unsubscribe(AfterValueClearEvent.class, listener);
    }

    @Override
    public void setUploadButtonCaption(String caption) {
        component.setUploadButtonCaption(caption);
    }

    @Override
    public String getUploadButtonCaption() {
        return component.getUploadButtonCaption();
    }

    @Override
    public void setUploadButtonIcon(String icon) {
        if (!StringUtils.isEmpty(icon)) {
            IconResolver iconResolver = beanLocator.get(IconResolver.class);
            component.setUploadButtonIcon(iconResolver.getIconResource(icon));
        } else {
            component.setUploadButtonIcon(null);
        }
    }

    @Override
    public String getUploadButtonIcon() {
        return component.getUploadButtonIcon();
    }

    @Override
    public void setUploadButtonDescription(String description) {
        component.setUploadButtonDescription(description);
    }

    @Override
    public String getUploadButtonDescription() {
        return component.getUploadButtonDescription();
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    protected class JmixSingleFileUploadField extends JmixFileUploadField {
        @Override
        protected void onSetInternalValue(Object newValue) {
            internalValueChanged(newValue);
        }
    }
}
