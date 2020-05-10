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

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.ValueChangeMode;
import io.jmix.core.Messages;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.MaskedField;
import io.jmix.ui.components.data.ConversionException;
import io.jmix.ui.components.data.DataAwareComponentsTools;
import io.jmix.ui.components.data.ValueConversionException;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.widgets.CubaMaskedTextField;
import io.jmix.ui.widgets.ShortcutListenerDelegate;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class WebMaskedField<V> extends WebV8AbstractField<CubaMaskedTextField, String, V>
        implements MaskedField<V>, InitializingBean {

    protected static final char PLACE_HOLDER = '_';
    protected static final Character[] MASK_SYMBOLS = new Character[]{'#', 'U', 'L', '?', 'A', '*', 'H', 'h', '~'};

    protected ShortcutListener enterShortcutListener;
    protected String nullRepresentation;

    protected Datatype<V> datatype;
    protected Locale locale;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public WebMaskedField() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    @Inject
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Inject
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Override
    public void setMask(String mask) {
        component.setMask(mask);
        updateNullRepresentation();
    }

    protected void updateNullRepresentation() {
        StringBuilder valueBuilder = new StringBuilder();
        String mask = getMask();

        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);

            if (c == '\'') {
                valueBuilder.append(mask.charAt(++i));
            } else if (ArrayUtils.contains(MASK_SYMBOLS, c)) {
                valueBuilder.append(PLACE_HOLDER);
            } else {
                valueBuilder.append(c);
            }
        }
        nullRepresentation = valueBuilder.toString();
    }

    protected String getNullRepresentation() {
        return nullRepresentation;
    }

    @Override
    public boolean isEmpty() {
        return MaskedField.super.isEmpty()
                || Objects.equals(getValue(), getNullRepresentation());
    }

    @Override
    public String getMask() {
        return component.getMask();
    }

    @Override
    public void setValueMode(ValueMode mode) {
        component.setMaskedMode(mode == ValueMode.MASKED);
    }

    @Override
    public ValueMode getValueMode() {
        return component.isMaskedMode() ? ValueMode.MASKED : ValueMode.CLEAR;
    }

    @Override
    public boolean isSendNullRepresentation() {
        return component.isSendNullRepresentation();
    }

    @Override
    public void setSendNullRepresentation(boolean sendNullRepresentation) {
        component.setSendNullRepresentation(sendNullRepresentation);
    }

    @Override
    public String getRawValue() {
        return component.getValue();
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
    protected String convertToPresentation(V modelValue) throws ConversionException {
        if (datatype != null) {
            return nullToEmpty(datatype.format(modelValue, locale));
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Range range = entityValueSource.getMetaPropertyPath().getRange();
            if (range.isDatatype()) {
                Datatype<V> propertyDataType = range.asDatatype();
                return nullToEmpty(propertyDataType.format(modelValue, locale));
            }
        }

        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Override
    protected V convertToModel(String componentRawValue) throws ConversionException {
        String value = emptyToNull(componentRawValue);

        if (datatype != null) {
            try {
                return datatype.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessage(), e);
            }
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            try {
                return propertyDataType.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessage(), e);
            }
        }

        return super.convertToModel(value);
    }

    protected String getConversionErrorMessage() {
        Messages messages = beanLocator.get(Messages.NAME);
        return messages.getMessage("databinding.conversion.error");
    }

    protected CubaMaskedTextField createComponent() {
        return new CubaMaskedTextField();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(CubaMaskedTextField component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Override
    public void setCursorPosition(int position) {
        component.setCursorPosition(position);
    }

    @Override
    public void selectAll() {
        component.selectAll();
    }

    @Override
    public void setSelectionRange(int pos, int length) {
        component.setSelection(pos, length);
    }

    @Override
    public Subscription addEnterPressListener(Consumer<EnterPressEvent> listener) {
        if (enterShortcutListener == null) {
            enterShortcutListener = new ShortcutListenerDelegate("enter", KeyCode.ENTER, null)
                    .withHandler((sender, target) -> {
                        EnterPressEvent event = new EnterPressEvent(WebMaskedField.this);
                        publish(EnterPressEvent.class, event);
                    });
            component.addShortcutListener(enterShortcutListener);
        }

        getEventHub().subscribe(EnterPressEvent.class, listener);

        return () -> removeEnterPressListener(listener);
    }

    @Override
    public void removeEnterPressListener(Consumer<EnterPressEvent> listener) {
        unsubscribe(EnterPressEvent.class, listener);

        if (enterShortcutListener != null
                && !hasSubscriptions(EnterPressEvent.class)) {
            component.removeShortcutListener(enterShortcutListener);
            enterShortcutListener = null;
        }
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
}
