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

package io.jmix.ui.component.factory;

import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.annotation.Lookup;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.GuiActionSupport;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component(DataGridEditorComponentGenerationStrategy.NAME)
public class DataGridEditorComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {
    public static final String NAME = "jmix_DataGridEditorMetaComponentStrategy";

    protected DataComponents dataComponents;
    protected GuiActionSupport guiActionSupport;

    @Autowired
    public void setDataComponents(DataComponents dataComponents) {
        this.dataComponents = dataComponents;
    }

    @Autowired
    public void setGuiActionSupport(GuiActionSupport guiActionSupport) {
        this.guiActionSupport = guiActionSupport;
    }

    @Autowired
    public DataGridEditorComponentGenerationStrategy(Messages messages, GuiActionSupport guiActionSupport) {
        super(messages, guiActionSupport);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getComponentClass() == null
                || !DataGrid.class.isAssignableFrom(context.getComponentClass())) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Override
    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        TextField component = uiComponents.create(TextField.class);
        setValueSource(component, context);
        return component;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Field createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Options options = context.getOptions();

        Lookup lookupAnnotation;
        if ((lookupAnnotation = mpp.getMetaProperty().getAnnotatedElement().getAnnotation(Lookup.class)) != null
                && lookupAnnotation.type() == LookupType.DROPDOWN) {
            MetaClass metaClass = mpp.getMetaProperty().getRange().asClass();
            CollectionContainer<Entity> container = dataComponents.createCollectionContainer(metaClass.getJavaClass());
            CollectionLoader<Entity> loader = dataComponents.createCollectionLoader();
            loader.setQuery("select e from " + metaClass.getName() + " e");
            loader.setFetchPlan(FetchPlan.MINIMAL);
            loader.setContainer(container);
            loader.load();
            options = new ContainerOptions(container);
        }

        PickerField pickerField;
        if (options == null) {
            pickerField = uiComponents.create(PickerField.class);
            setValueSource(pickerField, context);
            pickerField.addLookupAction();
            PickerField.LookupAction lookupAction =
                    (PickerField.LookupAction) pickerField.getActionNN(PickerField.LookupAction.NAME);
            // Opening lookup screen in another mode will close editor
            // lookupAction.setLookupScreenOpenType(OpenType.DIALOG); TODO: legacy-ui
            // In case of adding special logic for lookup screen opened from DataGrid editor
            lookupAction.setLookupScreenParams(ParamsMap.of("dataGridEditor", true));
            boolean actionsByMetaAnnotations = guiActionSupport.createActionsByMetaAnnotations(pickerField);
            if (!actionsByMetaAnnotations) {
                pickerField.addClearAction();
            }
        } else {
            LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);
            setValueSource(lookupPickerField, context);
            lookupPickerField.setOptions(options);

            pickerField = lookupPickerField;

            guiActionSupport.createActionsByMetaAnnotations(pickerField);
        }

        return pickerField;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 30;
    }
}