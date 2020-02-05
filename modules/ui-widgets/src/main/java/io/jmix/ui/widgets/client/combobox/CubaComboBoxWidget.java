/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.widgets.client.combobox;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.ShortcutActionHandler;
import com.vaadin.client.ui.VComboBox;

import java.util.Iterator;
import java.util.LinkedList;

public class CubaComboBoxWidget extends VComboBox
        implements ShortcutActionHandler.ShortcutActionHandlerOwner, HasEnabled, HasFocusHandlers {

    private static final String READONLY_STYLE_SUFFIX = "readonly";
    private static final String PROMPT_STYLE = "prompt";
    private static final String CUBA_DISABLED_OR_READONLY = "c-disabled-or-readonly";
    private static final String CUBA_EMPTY_VALUE = "c-empty-value";

    protected ShortcutActionHandler shortcutHandler;

    protected boolean enabled = true;
    protected int tabIndex = 0;

    public CubaComboBoxWidget() {
        // handle shortcuts
        DOM.sinkEvents(getElement(), Event.ONKEYDOWN);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        final int type = DOM.eventGetType(event);

        if (type == Event.ONKEYDOWN && shortcutHandler != null) {
            shortcutHandler.handleKeyboardEvent(event);
        }
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        if (enabled && !readonly) {
            switch (event.getNativeKeyCode()) {
                case KeyCodes.KEY_ENTER:
                case KeyCodes.KEY_TAB:
                case KeyCodes.KEY_SHIFT:
                case KeyCodes.KEY_CTRL:
                case KeyCodes.KEY_ALT:
                case KeyCodes.KEY_DOWN:
                case KeyCodes.KEY_UP:
                case KeyCodes.KEY_PAGEDOWN:
                case KeyCodes.KEY_PAGEUP:
                case KeyCodes.KEY_ESCAPE:
                    // NOP
                    break;
                default:
                    // special case for "clear" shortcut action
                    if (event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
                        suggestionPopup.hide();
                    } else {
                        // do not show options popup if we handle shortcut action
                        if (!event.isControlKeyDown()
                                && !event.isAltKeyDown()) {
                            super.onKeyUp(event);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected boolean isAllowNewItemsOnInputFieldKeyDown(KeyDownEvent event) {
        return super.isAllowNewItemsOnInputFieldKeyDown(event)
                && !event.isAnyModifierKeyDown();
    }

    public void setShortcutActionHandler(ShortcutActionHandler handler) {
        this.shortcutHandler = handler;
    }

    @Override
    public ShortcutActionHandler getShortcutActionHandler() {
        return shortcutHandler;
    }

    @Override
    public void add(Widget w) {
    }

    @Override
    public void clear() {
    }

    @Override
    public Iterator<Widget> iterator() {
        return new LinkedList<Widget>().iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        refreshEnabledOrReadonly();
    }

    protected boolean isReadonly() {
        return getStyleName().contains(getStylePrimaryName() + "-" + READONLY_STYLE_SUFFIX);
    }

    protected void refreshEnabledOrReadonly() {
        if (!isEnabled() || isReadonly()) {
            addStyleName(CUBA_DISABLED_OR_READONLY);
        } else {
            removeStyleName(CUBA_DISABLED_OR_READONLY);
        }
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return tb.addFocusHandler(handler);
    }
}
