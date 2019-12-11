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
package io.jmix.ui.xml.layout.loaders;


import io.jmix.core.commons.util.ParamsMap;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.components.CaptionMode;
import io.jmix.ui.components.LookupField;
import io.jmix.ui.components.data.options.ContainerOptions;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class LookupFieldLoader extends AbstractFieldLoader<LookupField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(LookupField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            resultComponent.setOptionCaptionProvider(o -> CaptionMode.PROPERTY);
            resultComponent.setOptionCaptionProvider(o -> captionProperty);
        }

        String nullName = element.attributeValue("nullName");
        if (StringUtils.isNotEmpty(nullName)) {
            resultComponent.setNullSelectionCaption(loadResourceString(nullName));
        }

        String pageLength = element.attributeValue("pageLength");
        if (StringUtils.isNotEmpty(pageLength)) {
            resultComponent.setPageLength(Integer.parseInt(pageLength));
        }

        loadBuffered(resultComponent, element);

        loadTextInputAllowed();
        loadInputPrompt(resultComponent, element);

        loadFilterMode(resultComponent, element);
        loadNewOptionHandler(resultComponent, element);

        loadNullOptionVisible(resultComponent, element);

        loadOptionsEnum(resultComponent, element);
    }

    @SuppressWarnings("unchecked")
    protected void loadOptionsEnum(LookupField resultComponent, Element element) {
        String optionsEnumClass = element.attributeValue("optionsEnum");
        if (StringUtils.isNotEmpty(optionsEnumClass)) {
            resultComponent.setOptionsEnum(getScripting().loadClass(optionsEnumClass));
        }
    }

    protected void loadNullOptionVisible(LookupField resultComponent, Element element) {
        String nullOptionVisible = element.attributeValue("nullOptionVisible");
        if (StringUtils.isNotEmpty(nullOptionVisible)) {
            resultComponent.setNullOptionVisible(Boolean.parseBoolean(nullOptionVisible));
        }
    }

    protected void loadTextInputAllowed() {
        String textInputAllowed = element.attributeValue("textInputAllowed");
        if (StringUtils.isNotEmpty(textInputAllowed)) {
            resultComponent.setTextInputAllowed(Boolean.parseBoolean(textInputAllowed));
        }
    }

    protected void loadNewOptionHandler(LookupField component, Element element) {
        String newOptionAllowed = element.attributeValue("newOptionAllowed");
        if (StringUtils.isNotEmpty(newOptionAllowed)) {
            component.setNewOptionAllowed(Boolean.parseBoolean(newOptionAllowed));
        }

        String newOptionHandlerMethod = element.attributeValue("newOptionHandler");
        if (StringUtils.isNotEmpty(newOptionHandlerMethod)) {
            FrameOwner controller = getComponentContext().getFrame().getFrameOwner();
            Class<? extends FrameOwner> windowClass = controller.getClass();

            Method newOptionHandler;
            try {
                newOptionHandler = windowClass.getMethod(newOptionHandlerMethod, LookupField.class, String.class);
            } catch (NoSuchMethodException e) {
                Map<String, Object> params = ParamsMap.of(
                        "LookupField Id", component.getId(),
                        "Method name", newOptionHandlerMethod
                );

                throw new GuiDevelopmentException("Unable to find new option handler method for lookup field",
                        context, params);
            }

            component.setNewOptionHandler(caption -> {
                try {
                    newOptionHandler.invoke(controller, component, caption);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Unable to invoke new option handler", e);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void loadContainer(LookupField component, Element element) {
        super.loadContainer(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));
    }

    protected void loadFilterMode(LookupField component, Element element) {
        String filterMode = element.attributeValue("filterMode");
        if (!StringUtils.isEmpty(filterMode)) {
            component.setFilterMode(LookupField.FilterMode.valueOf(filterMode));
        }
    }
}