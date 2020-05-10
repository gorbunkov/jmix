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

package io.jmix.ui.components.validation;

import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.core.commons.util.ParamsMap;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.ValidationException;
import io.jmix.ui.components.validation.numbers.NumberConstraint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static io.jmix.ui.components.validation.ValidatorHelper.getNumberConstraint;

/**
 * Negative validator checks that value should be a strictly less than 0.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="jmix_NegativeValidator" class="io.jmix.ui.components.validation.NegativeValidator" scope="prototype"/&gt;
 *     </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> BigDecimal, BigInteger, Long, Integer, Double, Float
 */
@Component(NegativeValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NegativeValidator<T extends Number> extends AbstractValidator<T> {

    public static final String NAME = "jmix_NegativeValidator";

    public NegativeValidator() {
    }

    /**
     * Constructor for custom error message. This message can contain '$value' key for formatted output.
     * <p>
     * Example: "Value '$value' should be less than 0".
     *
     * @param message error message
     */
    public NegativeValidator(String message) {
        this.message = message;
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }


    @Inject
    protected void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Inject
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        NumberConstraint constraint = getNumberConstraint(value);
        if (constraint == null) {
            throw new IllegalArgumentException("NegativeValidator doesn't support following type: '" + value.getClass() + "'");
        }

        if (!constraint.isNegative()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.negative");
            }

            String formattedValue = formatValue(value);
            throw new ValidationException(getTemplateErrorMessage(message, ParamsMap.of("value", formattedValue)));
        }
    }
}
