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

package io.jmix.ui.persistence.settings;

import io.jmix.core.BeanLocator;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.HasDataLoadingSettings;
import io.jmix.ui.components.HasPresentations;
import io.jmix.ui.components.HasSettings;
import io.jmix.ui.presentations.Presentations;
import io.jmix.ui.settings.component.ComponentSettings.HasSettingsPresentation;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.worker.ComponentSettingsWorker;
import io.jmix.ui.settings.component.worker.DataLoadingSettingsWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.UUID;

import static io.jmix.ui.components.ComponentsHelper.getComponentPath;

/**
 * Provides functionality for applying and saving component settings.
 */
@org.springframework.stereotype.Component(ScreenSettingsManager.NAME)
public class ScreenSettingsManager {

    private static final Logger log = LoggerFactory.getLogger(ScreenSettingsManager.class);

    public static final String NAME = "jmix_ScreenSettingsManager";

    @Inject
    protected ComponentSettingsRegistry settingsRegistry;

    @Inject
    protected BeanLocator beanLocator;

    /**
     * Applies settings for components if they have own {@link ComponentSettingsWorker}.
     *
     * @param components     components to apply settings
     * @param screenSettings screen settings
     */
    public void applySettings(Collection<Component> components, ScreenSettings screenSettings) {
        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", getComponentPath(component), component);

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);

            ComponentSettingsWorker worker = beanLocator.get(settingsRegistry.getWorkerClass(settingsClass));

            if (component instanceof HasPresentations) {
                ComponentSettings defaultSettings = worker.getSettings(component);
                ((HasPresentations) component).setDefaultSettings(new SettingsWrapperImpl(defaultSettings));
            }

            worker.applySettings(component, new SettingsWrapperImpl(settings));

            if (component instanceof HasPresentations
                    && settings instanceof HasSettingsPresentation) {
                UUID presentationId = ((HasSettingsPresentation) settings).getPresentationId();
                if (presentationId != null) {
                    ((HasPresentations) component).applyPresentationAsDefault(presentationId);
                }
            }
        }
    }

    /**
     * Applies data loading settings for components if they have own {@link ComponentSettingsWorker}..
     *
     * @param components     components to apply settings
     * @param screenSettings screen settings
     */
    public void applyDataLoadingSettings(Collection<Component> components, ScreenSettings screenSettings) {
        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", getComponentPath(component), component);

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettingsWorker worker = beanLocator.get(settingsRegistry.getWorkerClass(settingsClass));

            if (worker instanceof DataLoadingSettingsWorker) {
                ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);
                ((DataLoadingSettingsWorker) worker).applyDataLoadingSettings(component, new SettingsWrapperImpl(settings));
            }
        }
    }

    /**
     * Saves settings and persist if they are changed or screen settings is modified. Components must
     * have {@link ComponentSettingsWorker}.
     *
     * @param components     components to save settings
     * @param screenSettings screen settings
     */
    public void saveSettings(Collection<Component> components, ScreenSettings screenSettings) {
        boolean isModified = false;

        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())) {
                continue;
            }

            log.trace("Saving settings for {} : {}", getComponentPath(component), component);

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);

            ComponentSettingsWorker worker = beanLocator.get(settingsRegistry.getWorkerClass(settingsClass));

            boolean settingsChanged = worker.saveSettings(component, new SettingsWrapperImpl(settings));
            if (settingsChanged) {
                isModified = true;

                screenSettings.put(settings);
            }

            if (component instanceof HasPresentations) {
                HasPresentations compWithPres = (HasPresentations) component;
                if (compWithPres.isUsePresentations()) {
                    Presentations presentations = compWithPres.getPresentations();
                    presentations.commit();
                }
            }
        }

        if (isModified || screenSettings.isModified()) {
            if (screenSettings instanceof AbstractScreenSettings) {
                ((AbstractScreenSettings) screenSettings).commit();
            }
        }
    }
}
