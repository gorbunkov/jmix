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

package io.jmix.ui.widget.client.passwordfield;

import io.jmix.ui.widget.JmixPasswordField;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.passwordfield.PasswordFieldConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixPasswordField.class)
public class JmixPasswordFieldConnector extends PasswordFieldConnector {

    @Override
    public JmixPasswordFieldState getState() {
        return (JmixPasswordFieldState) super.getState();
    }

    @Override
    public JmixPasswordFieldWidget getWidget() {
        return (JmixPasswordFieldWidget) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        getWidget().setAutocomplete(getState().autocomplete);

        if (stateChangeEvent.hasPropertyChanged("capsLockIndicator")) {
            ComponentConnector capsLockIndicator = (ComponentConnector) getState().capsLockIndicator;

            getWidget().setIndicateCapsLock(capsLockIndicator == null ? null : capsLockIndicator.getWidget());
        }

        if (stateChangeEvent.hasPropertyChanged("htmlName")) {
            getWidget().setHtmlName(getState().htmlName);
        }
    }
}
