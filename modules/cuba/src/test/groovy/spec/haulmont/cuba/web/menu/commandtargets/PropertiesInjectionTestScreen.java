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

import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;

@UiController("cuba_PropertiesInjectionTestScreen")
public class PropertiesInjectionTestScreen extends Screen {

    protected int testIntProperty;

    protected String testStringProperty;

    protected User entityToEdit;

    public void setTestIntProperty(int testIntProperty) {
        this.testIntProperty = testIntProperty;
    }

    public void setTestStringProperty(String testStringProperty) {
        this.testStringProperty = testStringProperty;
    }

    public void setEntityToEdit(User entityToEdit) {
        this.entityToEdit = entityToEdit;
    }
}
