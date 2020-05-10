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

import io.jmix.core.Messages;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.CurrencyField;
import io.jmix.ui.components.data.ConversionException;
import io.jmix.ui.components.data.DataAwareComponentsTools;
import io.jmix.ui.components.data.ValueConversionException;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.meta.EntityValueSource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class WebCurrencyField<V extends Number> extends WebV8AbstractField<CubaCurrencyField, String, V>
        implements CurrencyField<V> {

    protected Locale locale;
    protected Datatype<V> datatype;
    protected Datatype<V> defaultDatatype;
    protected String conversionErrorMessage;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public WebCurrencyField() {
        component = new CubaCurrencyField();
        component.setCurrencyLabelPosition(toWidgetLabelPosition(CurrencyLabelPosition.RIGHT));

        attachValueChangeListener(component);
    }

    @Inject
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Inject
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        //noinspection unchecked
        this.defaultDatatype = (Datatype<V>) datatypeRegistry.get(BigDecimal.class);
    }

    @Override
    protected void attachValueChangeListener(CubaCurrencyField component) {
        component.getInternalComponent()
                .addValueChangeListener(event ->
                        componentValueChanged(event.getOldValue(), event.getValue(), event.isUserOriginated()));
    }

    @Inject
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Override
    protected String convertToPresentation(V modelValue) throws ConversionException {
        Datatype<V> datatype = getDatatypeInternal();
        // Vaadin TextField does not permit `null` value
        if (datatype != null) {
            return nullToEmpty(datatype.format(modelValue, locale));
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            return nullToEmpty(propertyDataType.format(modelValue, locale));
        }

        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Override
    protected V convertToModel(String componentRawValue) throws ConversionException {
        String value = StringUtils.trimToNull(emptyToNull(componentRawValue));

        Datatype<V> datatype = getDatatypeInternal();
        if (datatype != null) {
            try {
                return datatype.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessageInternal(), e);
            }
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            try {
                return propertyDataType.parse(componentRawValue, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessageInternal(), e);
            }
        }

        return super.convertToModel(componentRawValue);
    }

    @Override
    public void setConversionErrorMessage(String conversionErrorMessage) {
        this.conversionErrorMessage = conversionErrorMessage;
    }

    @Override
    public String getConversionErrorMessage() {
        return conversionErrorMessage;
    }

    protected String getConversionErrorMessageInternal() {
        String customErrorMessage = getConversionErrorMessage();
        if (StringUtils.isNotEmpty(customErrorMessage)) {
            return customErrorMessage;
        }

        Datatype<V> datatype = this.datatype;

        if (datatype == null
                && valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {

            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            datatype = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
        }

        if (datatype != null) {
            String msg = getDatatypeConversionErrorMsg(datatype);
            if (StringUtils.isNotEmpty(msg)) {
                return msg;
            }
        }

        return beanLocator.get(Messages.class)
                .getMessage("databinding.conversion.error");
    }

    @Override
    public void setCurrency(String currency) {
        component.setCurrency(currency);
    }

    @Override
    public String getCurrency() {
        return component.getCurrency();
    }

    @Override
    public void setShowCurrencyLabel(boolean showCurrencyLabel) {
        component.setShowCurrencyLabel(showCurrencyLabel);
    }

    @Override
    public boolean getShowCurrencyLabel() {
        return component.getShowCurrencyLabel();
    }

    @Override
    public void setCurrencyLabelPosition(CurrencyLabelPosition currencyLabelPosition) {
        Preconditions.checkNotNullArgument(currencyLabelPosition);

        component.setCurrencyLabelPosition(toWidgetLabelPosition(currencyLabelPosition));
    }

    @Override
    public CurrencyLabelPosition getCurrencyLabelPosition() {
        return fromWidgetLabelPosition(component.getCurrencyLabelPosition());
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();
            if (metaPropertyPath.getRange().isDatatype()) {
                Datatype datatype = metaPropertyPath.getRange().asDatatype();
                if (!Number.class.isAssignableFrom(datatype.getJavaClass())) {
                    throw new IllegalArgumentException("CurrencyField doesn't support Datatype with class: " + datatype.getJavaClass());
                }
            } else {
                throw new IllegalArgumentException("CurrencyField doesn't support properties with association");
            }

            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

            Object annotation = metaProperty.getAnnotations()
                    .get(CurrencyValue.class.getName());
            if (annotation == null) {
                return;
            }

            //noinspection unchecked
            Map<String, Object> annotationProperties = (Map<String, Object>) annotation;

            String currencyName = (String) annotationProperties.get("currency");
            component.setCurrency(currencyName);

            String labelPosition = ((io.jmix.core.entity.annotation.CurrencyLabelPosition) annotationProperties.get("labelPosition")).name();
            setCurrencyLabelPosition(CurrencyLabelPosition.valueOf(labelPosition));
        }
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        Preconditions.checkNotNullArgument(datatype);
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());
        if (!Number.class.isAssignableFrom(datatype.getJavaClass())) {
            throw new IllegalArgumentException("CurrencyField doesn't support Datatype with class: " + datatype.getJavaClass());
        }

        this.datatype = datatype;
    }

    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    protected Datatype<V> getDatatypeInternal() {
        if (datatype != null) {
            return datatype;
        }
        return valueBinding == null ? defaultDatatype : null;
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

    protected io.jmix.ui.widgets.CurrencyLabelPosition toWidgetLabelPosition(CurrencyLabelPosition labelPosition) {
        return io.jmix.ui.widgets.CurrencyLabelPosition.valueOf(labelPosition.name());
    }

    protected CurrencyLabelPosition fromWidgetLabelPosition(io.jmix.ui.widgets.CurrencyLabelPosition wLabelPosition) {
        return CurrencyLabelPosition.valueOf(wLabelPosition.name());
    }
}
