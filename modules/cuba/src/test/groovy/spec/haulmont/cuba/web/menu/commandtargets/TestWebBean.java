/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.menu.commandtargets;

import io.jmix.ui.component.mainwindow.impl.WebAppWorkArea;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component(TestWebBean.NAME)
public class TestWebBean {

    public static final String NAME = "cuba_TestWebBean";

    public static final ThreadLocal<Boolean> testMethodInvoked = new ThreadLocal<>();

    public static final ThreadLocal<Boolean> screenOpenedEventHandled = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> screenClosedEventHandled = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> workAreaTabChangedEventHandled = new ThreadLocal<>();

    public void testMethod() {
        testMethodInvoked.set(true);
    }

    /*@EventListener todo port
    public void onScreenOpened(ScreenOpenedEvent evt) {
        screenOpenedEventHandled.set(true);
    }

    @EventListener
    public void onScreenClosed(ScreenClosedEvent evt) {
        screenClosedEventHandled.set(true);
    }*/

    @EventListener
    public void onWorkAreaTabChangedEvent(WebAppWorkArea.WorkAreaTabChangedEvent evt) {
        workAreaTabChangedEventHandled.set(true);
    }
}
