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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.components.TreeDataGrid;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.dynattrui.DynAttrEmbeddingStrategies;
import io.jmix.ui.xml.layout.loader.TreeDataGridLoader;

@SuppressWarnings("rawtypes")
public class CubaTreeDataGridLoader extends TreeDataGridLoader {

    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadSettingsEnabled((TreeDataGrid) resultComponent, element);
    }

    @Override
    protected CubaTreeDataGridDataHolder initDataGridDataHolder() {
        CubaTreeDataGridDataHolder holder = new CubaTreeDataGridDataHolder();

        String datasourceId = element.attributeValue("datasource");
        if (Strings.isNullOrEmpty(datasourceId)) {
            return holder;
        }

        CollectionDatasource datasource = DatasourceLoaderHelper.loadCollectionDatasource(
                datasourceId, context, (ComponentLoaderContext) getComponentContext()
        );

        holder.setDatasource(datasource);
        holder.setMetaClass(datasource.getMetaClass());
        holder.setFetchPlan(datasource.getView());

        return holder;
    }

    @Override
    protected void setupDataContainer(DataGridDataHolder holder) {
        CollectionDatasource datasource = ((CubaTreeDataGridDataHolder) holder).getDatasource();
        if (datasource == null) {
            return;
        }
        ((TreeDataGrid) resultComponent).setDatasource(datasource);

        DynAttrEmbeddingStrategies embeddingStrategies = beanLocator.get(DynAttrEmbeddingStrategies.class);
        embeddingStrategies.embedAttributes(resultComponent, getComponentContext().getFrame());
    }

    protected static class CubaTreeDataGridDataHolder extends DataGridDataHolder {

        protected CollectionDatasource datasource;

        public CollectionDatasource getDatasource() {
            return datasource;
        }

        public void setDatasource(CollectionDatasource datasource) {
            this.datasource = datasource;
        }

        @Override
        public boolean isContainerLoaded() {
            return super.isContainerLoaded() || datasource != null;
        }
    }
}
