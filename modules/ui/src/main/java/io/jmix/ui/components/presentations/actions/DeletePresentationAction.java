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

package io.jmix.ui.components.presentations.actions;

import io.jmix.ui.presentations.model.TablePresentation;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Table;
import io.jmix.ui.presentations.TablePresentations;

public class DeletePresentationAction extends AbstractPresentationAction {

    public DeletePresentationAction(Table table) {
        super(table, "PresentationsPopup.delete", null);
    }

    @Override
    public void actionPerform(Component component) {
        tableImpl.hidePresentationsPopup();

        TablePresentations presentations = table.getPresentations();
        TablePresentation current = presentations.getCurrent();
        presentations.remove(current);
        presentations.commit();
    }
}
