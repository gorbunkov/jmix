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

package io.jmix.ui.settings.component.worker;

import io.jmix.ui.components.Component;
import io.jmix.ui.components.GroupBoxLayout;
import io.jmix.ui.components.impl.WebGroupBox;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.GroupBoxSettings;
import io.jmix.ui.settings.component.SettingsWrapper;

@org.springframework.stereotype.Component(GroupBoxSettingsWorker.NAME)
public class GroupBoxSettingsWorker implements ComponentSettingsWorker {

    public static final String NAME = "jmix_GroupBoxSettingsWorker";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebGroupBox.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return GroupBoxSettings.class;
    }

    @Override
    public void applySettings(Component component, SettingsWrapper wrapper) {
        GroupBoxLayout groupBox = (GroupBoxLayout) component;
        GroupBoxSettings settings = wrapper.getSettings();

        if (settings.getExpanded() != null) {
            groupBox.setExpanded(settings.getExpanded());
        }
    }

    @Override
    public void applyDataLoadingSettings(Component component, SettingsWrapper wrapper) {
        // does not have data loading settings
    }

    @Override
    public boolean saveSettings(Component component, SettingsWrapper wrapper) {
        GroupBoxLayout groupBox = (GroupBoxLayout) component;
        GroupBoxSettings settings = wrapper.getSettings();

        if (settings.getExpanded() == null
                || settings.getExpanded() != groupBox.isExpanded()) {
            settings.setExpanded(groupBox.isExpanded());

            return true;
        }

        return false;
    }

    @Override
    public ComponentSettings getSettings(Component component) {
        GroupBoxLayout groupBox = (GroupBoxLayout) component;

        GroupBoxSettings settings = createSettings();
        settings.setExpanded(groupBox.isExpanded());

        return settings;
    }

    protected GroupBoxSettings createSettings() {
        return new GroupBoxSettings();
    }
}