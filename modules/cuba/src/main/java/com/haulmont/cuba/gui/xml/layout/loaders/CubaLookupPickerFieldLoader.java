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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.LookupPickerField;
import com.haulmont.cuba.gui.components.OptionsField;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.actions.GuiActionSupport;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.xml.layout.loader.EntityComboBoxLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaLookupPickerFieldLoader extends EntityComboBoxLoader {

    @Override
    public void createComponent() {
        resultComponent = factory.create(LookupPickerField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public LookupPickerField getResultComponent() {
        return (LookupPickerField) super.getResultComponent();
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadNewOptionAllowed(getResultComponent(), element);
    }

    protected void loadNewOptionAllowed(LookupPickerField lookupPickerField, Element element) {
        String newOptionAllowed = element.attributeValue("newOptionAllowed");
        if (StringUtils.isNotEmpty(newOptionAllowed)) {
            lookupPickerField.setNewOptionAllowed(Boolean.parseBoolean(newOptionAllowed));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(ComboBox component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);

        DatasourceLoaderHelper
                .loadOptionsDatasourceIfOptionsNull((OptionsField) resultComponent, element,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setOptions);
    }

    @Override
    protected boolean createActionsByMetaAnnotations() {
        GuiActionSupport guiActionSupport = getCubaGuiActionSupport();
        return guiActionSupport.createActionsByMetaAnnotations(getResultComponent());
    }

    @Override
    protected void addDefaultActions() {
        if (ComponentLoaderHelper.isLegacyFrame(context)) {
            getResultComponent().addLookupAction();
            getResultComponent().addClearAction();
        } else {
            super.addDefaultActions();
        }
    }

    protected GuiActionSupport getCubaGuiActionSupport() {
        return beanLocator.get(GuiActionSupport.NAME);
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        String id = loadActionId(element);

        return ComponentLoaderHelper
                .loadLegacyPickerAction(((PickerField) actionsHolder), element, context, id)
                .orElseGet(() ->
                        super.loadDeclarativeAction(actionsHolder, element));
    }
}
