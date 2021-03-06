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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider;
import io.jmix.ui.component.calendar.CalendarEventProvider;

import javax.annotation.Nullable;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.Calendar} instead
 */
@Deprecated
public interface Calendar<V> extends io.jmix.ui.component.Calendar<V> {

    /**
     * Set collection datasource for the calendar component with a collection of events.
     *
     * @param datasource a datasource to set
     * @deprecated @deprecated Use {@link #setEventProvider(CalendarEventProvider)}
     * with {@link EntityCalendarEventProvider} instead
     */
    @Deprecated
    void setDatasource(CollectionDatasource datasource);

    /**
     * @return a datasource
     * @deprecated Use {@link #getEventProvider()} instead
     */
    @Nullable
    @Deprecated
    CollectionDatasource getDatasource();
}
