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
package io.jmix.ui.sys;

import io.jmix.core.commons.events.Subscription;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Frame;
import io.jmix.ui.components.HasValue;
import io.jmix.ui.components.ListComponent;
import io.jmix.ui.components.FrameContext;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Consumer;

public class FrameContextImpl implements FrameContext {

    protected final Frame frame;

    public FrameContextImpl(Frame window) {
        this.frame = window;
    }

    public Collection<String> getParameterNames() {
        List<String> names = new ArrayList<>();
        for (String s : getParams().keySet()) {
            names.add(s.substring("param$".length()));
        }
        return names;
    }

    public <T> T getParameterValue(String property) {
        //noinspection unchecked
        return (T) getParams().get("param$" + property);
    }

    protected Map<String, Object> getParamsMap(ScreenOptions options) {
        if (options instanceof MapScreenOptions) {
            return ((MapScreenOptions) options).getParams();
        }
        return Collections.emptyMap();
    }

    @Override
    public Frame getFrame() {
        return frame;
    }

    @Override
    public Map<String, Object> getParams() {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(frame.getFrameOwner());
        if (screenContext.getScreenOptions() instanceof MapScreenOptions) {
            return ((MapScreenOptions) screenContext.getScreenOptions()).getParams();
        }

        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getParamValue(String param) {
        return (T) getParams().get(param);
    }

    @Override
    public <T> T getValue(String property) {
        String[] elements = ValuePathHelper.parse(property);
        String[] path = elements;

        Component component = frame.getComponent(property);
        while (component == null && path.length > 1) {
            // in case of property contains a drill-down part
            path = ArrayUtils.subarray(path, 0, path.length - 1);
            component = frame.getComponent(ValuePathHelper.format(path));
        }

        if (component == null || component instanceof Frame
                || ((component instanceof Component.Wrapper) && ((Component.Wrapper) component).getComponent() instanceof Frame))
        {
            // if component not found or found a frame, try to search in the parent frame
            if (frame.getFrame() != null && frame.getFrame() != frame)
                return frame.getFrame().getContext().getValue(property);
            else
                return null;
        }

        final Object value = getValue(component);
        if (value == null)
            return null;

        if (path.length == elements.length) {
            //noinspection unchecked
            return (T) value;
        } else {
            List<String> propertyPath = Arrays.asList(elements).subList(path.length, elements.length);
            String[] properties = propertyPath.toArray(new String[0]);

            if (value instanceof Entity) {
                //noinspection RedundantTypeArguments
                return EntityValues.getValueEx(((Entity) value), properties);
            } else if (value instanceof EnumClass) {
                if (properties.length == 1 && "id".equals(properties[0])) {
                    //noinspection unchecked
                    return (T) ((EnumClass) value).getId();
                } else {
                    throw new UnsupportedOperationException(String.format("Can't get property '%s' of enum %s", propertyPath, value));
                }
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getValue(Component component) {
        if (component instanceof HasValue) {
            return (T) ((HasValue) component).getValue();
        } else if (component instanceof ListComponent) {
            ListComponent list = (ListComponent) component;
            //noinspection unchecked
            return list.isMultiSelect() ? (T)list.getSelected() : (T)list.getSingleSelected();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(String property, Object value) {
        final Component component = frame.getComponent(property);
        if (component instanceof HasValue) {
            ((HasValue) component).setValue(value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Subscription addValueChangeListener(String componentName, Consumer<HasValue.ValueChangeEvent> listener) {
        Component component = frame.getComponent(componentName);
        if (component == null)
            throw new RuntimeException("Component not found: " + componentName);
        if (component instanceof HasValue) {
            return ((HasValue) component).addValueChangeListener(listener);
        } else if (component instanceof ListComponent) {
            throw new UnsupportedOperationException("List component is not supported yet");
        } else {
            throw new RuntimeException("Unable to add listener to the component " + component);
        }
    }
}
