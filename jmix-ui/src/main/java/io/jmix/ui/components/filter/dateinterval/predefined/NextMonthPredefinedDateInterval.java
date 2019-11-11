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

package io.jmix.ui.components.filter.dateinterval.predefined;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 */
@Component("cuba_NextMonthPredefinedDateInterval")
@Order(60)
public class NextMonthPredefinedDateInterval extends PredefinedDateInterval {

    public NextMonthPredefinedDateInterval() {
        super("nextMonth");
    }

    @Override
    public String getJPQL(String propertyName) {
        return String.format("@between({E}.%s, now+1, now+2, month)", propertyName);
    }
}
