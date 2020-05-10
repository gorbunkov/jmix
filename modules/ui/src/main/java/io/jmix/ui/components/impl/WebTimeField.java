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

import com.vaadin.data.HasValue;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.TimeField;
import io.jmix.ui.components.data.ConversionException;
import io.jmix.ui.components.data.DataAwareComponentsTools;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.widgets.CubaTimeFieldWrapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.Date;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.components.impl.WebWrapperUtils.*;

public class WebTimeField<V> extends WebV8AbstractField<CubaTimeFieldWrapper, LocalTime, V>
        implements TimeField<V>, InitializingBean {

    @Inject
    protected DateTimeTransformations dateTimeTransformations;
    protected DataAwareComponentsTools dataAwareComponentsTools;

    protected Datatype<V> datatype;

    public WebTimeField() {
        component = createComponent();
        component.addValueChangeListener(this::componentValueChanged);
    }

    @Inject
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Override
    public void afterPropertiesSet() {
        CurrentAuthentication currentAuthentication = beanLocator.get(CurrentAuthentication.NAME);
        FormatStringsRegistry formatStringsRegistry = beanLocator.get(FormatStringsRegistry.NAME);
        String timeFormat = formatStringsRegistry.getFormatStringsNN(currentAuthentication.getLocale()).getTimeFormat();
        setFormat(timeFormat);
    }

    @Override
    public void setFormat(String format) {
        component.setTimeFormat(format);
    }

    @Override
    public String getFormat() {
        return component.getTimeFormat();
    }

    @Override
    public Resolution getResolution() {
        return fromVaadinTimeResolution(component.getResolution());
    }

    @Override
    public void setResolution(Resolution resolution) {
        checkNotNullArgument(resolution);

        component.setResolution(toVaadinTimeResolution(resolution));
    }


    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @Override
    public boolean getShowSeconds() {
        return fromVaadinTimeResolution(component.getResolution()) == Resolution.SEC;
    }

    @Override
    public void setShowSeconds(boolean showSeconds) {
        setResolution(Resolution.SEC);
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
    public void setTimeMode(TimeMode timeMode) {
        checkNotNullArgument("Time mode cannot be null");

        component.setTimeMode(toVaadinTimeMode(timeMode));
    }

    @Override
    public TimeMode getTimeMode() {
        return fromVaadinTimeMode(component.getTimeMode());
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

    @Override
    protected LocalTime convertToPresentation(V modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }
        return dateTimeTransformations.transformToLocalTime(modelValue);
    }

    protected CubaTimeFieldWrapper createComponent() {
        return new CubaTimeFieldWrapper();
    }

    protected void componentValueChanged(HasValue.ValueChangeEvent<LocalTime> e) {
        if (e.isUserOriginated()) {
            V value;

            try {
                value = constructModelValue();

                setValueToPresentation(convertToPresentation(value));
            } catch (ConversionException ce) {
                LoggerFactory.getLogger(WebDateField.class)
                        .trace("Unable to convert presentation value to model", ce);

                setValidationError(ce.getLocalizedMessage());
                return;
            }

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, true);
                publish(ValueChangeEvent.class, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected V constructModelValue() {
        LocalTime timeValue = component.getValue();
        if (timeValue == null) {
            return null;
        }

        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource)
                    .getMetaPropertyPath().getMetaProperty();
            return (V) convertFromLocalTime(timeValue,
                    metaProperty.getRange().asDatatype().getJavaClass());
        }

        return (V) convertFromLocalTime(timeValue,
                datatype == null ? Date.class : datatype.getJavaClass());
    }

    protected Object convertFromLocalTime(LocalTime localTime, Class javaType) {
        return dateTimeTransformations.transformFromLocalTime(localTime, javaType);
    }
}
