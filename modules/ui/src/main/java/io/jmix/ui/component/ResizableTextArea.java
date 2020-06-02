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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Resizable text area component.
 *
 * @param <V> type of value
 * @see TextArea
 */
public interface ResizableTextArea<V> extends TextArea<V> {
    String NAME = "resizableTextArea";

    /**
     * @deprecated Use {@link ResizableTextArea#setResizableDirection(ResizeDirection)} instead.
     */
    @Deprecated
    void setResizable(boolean resizable);

    /**
     * @deprecated Use {@link ResizableTextArea#getResizableDirection()} instead.
     */
    @Deprecated
    boolean isResizable();

    /**
     * Allows resizing textArea in a given direction.
     *
     * @param direction the direction in which resizes textArea.
     */
    void setResizableDirection(ResizeDirection direction);

    /**
     * Get the direction in which the textArea size changes.
     *
     * @return direction.
     */
    ResizeDirection getResizableDirection();

    class ResizeEvent extends EventObject {
        private final ResizableTextArea component;
        private final String prevWidth;
        private final String width;
        private final String prevHeight;
        private final String height;

        public ResizeEvent(ResizableTextArea component, String prevWidth, String width, String prevHeight, String height) {
            super(component);

            this.component = component;
            this.prevWidth = prevWidth;
            this.width = width;
            this.prevHeight = prevHeight;
            this.height = height;
        }

        /**
         * @return resizable text area
         * @deprecated Use {@link #getSource()} instead.
         */
        @Deprecated
        public ResizableTextArea getComponent() {
            return component;
        }

        /**
         * @return source component of event
         */
        @Override
        public ResizableTextArea getSource() {
            return (ResizableTextArea) super.getSource();
        }

        public String getHeight() {
            return height;
        }

        public String getPrevHeight() {
            return prevHeight;
        }

        public String getPrevWidth() {
            return prevWidth;
        }

        public String getWidth() {
            return width;
        }
    }

    /**
     * Represents directions in which textArea can be resized.
     */
    enum ResizeDirection {
        HORIZONTAL, VERTICAL, BOTH, NONE
    }

    Subscription addResizeListener(Consumer<ResizeEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeResizeListener(Consumer<ResizeEvent> listener);
}