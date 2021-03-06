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

package com.haulmont.cuba.core.testsupport;

import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

public class TestAppContextLifecycleListener {

    List<ApplicationContextEvent> events = new ArrayList<>();

    public List<ApplicationContextEvent> getEvents() {
        return events;
    }

    @EventListener
    void onRefreshed(ContextRefreshedEvent event) {
        events.add(event);
    }

    @EventListener
    void onInitialized(AppContextInitializedEvent event) {
        events.add(event);
    }

    @EventListener
    void onStarted(AppContextStartedEvent event) {
        events.add(event);
    }
}
