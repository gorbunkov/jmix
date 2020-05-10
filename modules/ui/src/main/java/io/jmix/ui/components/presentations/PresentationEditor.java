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
package io.jmix.ui.components.presentations;

import com.vaadin.ui.*;
import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.commons.util.Dom4j;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.Presentation;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.Security;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.components.HasPresentations;
import io.jmix.ui.presentations.Presentations;
import io.jmix.ui.sys.PersistenceHelper;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widgets.CubaButton;
import io.jmix.ui.widgets.CubaWindow;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PresentationEditor extends CubaWindow {

    private static final Logger log = LoggerFactory.getLogger(PresentationEditor.class);

    protected Presentation presentation;

    protected HasPresentations component;
    protected TextField nameField;
    protected CheckBox autoSaveField;
    protected CheckBox defaultField;

    protected CheckBox globalField;

    protected boolean isNew;
    protected boolean allowGlobalPresentations;

    protected Messages messages;
    protected CurrentAuthentication currentAuthentication;
    protected Security security;

    public PresentationEditor(Presentation presentation, HasPresentations component) {
        this.presentation = presentation;
        this.component = component;

        messages = AppBeans.get(Messages.NAME);
        currentAuthentication = AppBeans.get(CurrentAuthentication.NAME);
        security = AppBeans.get(Security.NAME);

        isNew = PersistenceHelper.isNew(presentation);
        allowGlobalPresentations = security.isSpecificPermitted("cuba.gui.presentations.global");

        initLayout();

        setWidthUndefined();

        String titleMessageKey = isNew ? "PresentationsEditor.new" : "PresentationsEditor.edit";
        setCaption(getMessage(titleMessageKey));

        setModal(true);
        setResizable(false);
    }

    protected void initLayout() {
        ThemeConstants theme = App.getInstance().getThemeConstants();

        VerticalLayout root = new VerticalLayout();
        root.setWidthUndefined();
        root.setSpacing(true);
        root.setMargin(false);
        setContent(root);

        messages = AppBeans.get(Messages.class);

        nameField = new TextField(messages.getMessage("PresentationsEditor.name"));
        nameField.setWidth(theme.get("cuba.web.PresentationEditor.name.width"));
        nameField.setValue(getPresentationCaption());
        root.addComponent(nameField);

        autoSaveField = new CheckBox();
        autoSaveField.setCaption(messages.getMessage("PresentationsEditor.autoSave"));
        autoSaveField.setValue(BooleanUtils.isTrue(presentation.getAutoSave()));
        root.addComponent(autoSaveField);

        defaultField = new CheckBox();
        defaultField.setCaption(messages.getMessage("PresentationsEditor.default"));
        defaultField.setValue(EntityValues.<UUID>getId(presentation).equals(component.getDefaultPresentationId()));
        root.addComponent(defaultField);

        if (allowGlobalPresentations) {
            globalField = new CheckBox();
            globalField.setCaption(messages.getMessage("PresentationsEditor.global"));
            globalField.setValue(!isNew && presentation.getUser() == null);
            root.addComponent(globalField);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setMargin(false);
        buttons.setSpacing(true);
        buttons.setWidthUndefined();
        root.addComponent(buttons);
        root.setComponentAlignment(buttons, Alignment.MIDDLE_LEFT);

        Button commitButton = new CubaButton(messages.getMessage("PresentationsEditor.save"));
        commitButton.addClickListener(event -> {
            if (validate()) {
                commit();
                forceClose();
            }
        });
        buttons.addComponent(commitButton);

        Button closeButton = new CubaButton(messages.getMessage("PresentationsEditor.close"));
        closeButton.addClickListener(event ->
                forceClose()
        );
        buttons.addComponent(closeButton);

        nameField.focus();
    }

    protected boolean validate() {
        Presentations presentations = component.getPresentations();

        //check that name is empty
        if (StringUtils.isEmpty(nameField.getValue())) {
            AppUI.getCurrent().getNotifications()
                    .create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("PresentationsEditor.error"))
                    .withDescription(messages.getMessage("PresentationsEditor.error.nameRequired"))
                    .show();
            return false;
        }

        //check that name is unique
        final Presentation pres = presentations.getPresentationByName(nameField.getValue());
        if (pres != null && !pres.equals(presentation)) {
            AppUI.getCurrent().getNotifications()
                    .create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("PresentationsEditor.error"))
                    .withDescription(messages.getMessage("PresentationsEditor.error.nameAlreadyExists"))
                    .show();
            return false;
        }
        return true;
    }

    protected void commit() {
        Presentations presentations = component.getPresentations();

        Document doc = DocumentHelper.createDocument();
        doc.setRootElement(doc.addElement("presentation"));

        component.saveSettings(doc.getRootElement());

        String xml = Dom4j.writeDocument(doc, false);
        presentation.setXml(xml);

        presentation.setName(nameField.getValue());
        presentation.setAutoSave(autoSaveField.getValue());
        presentation.setDefault(defaultField.getValue());

        // todo user substitution
        BaseUser user = currentAuthentication.getUser();

        boolean userOnly = !allowGlobalPresentations || !BooleanUtils.isTrue(globalField.getValue());
        presentation.setUser(userOnly ? user : null);

        if (log.isTraceEnabled()) {
            log.trace(String.format("XML: %s", Dom4j.writeDocument(doc, true)));
        }

        if (isNew) {
            presentations.add(presentation);
        } else {
            presentations.modify(presentation);
        }
        presentations.commit();

        addCloseListener(e -> {
            if (isNew) {
                component.applyPresentation(EntityValues.<UUID>getId(presentation));
            }
        });
    }

    protected String getPresentationCaption() {
        return presentation.getName() == null ? "" : presentation.getName();
    }

    protected String getMessage(String key) {
        return messages.getMessage(key);
    }
}
