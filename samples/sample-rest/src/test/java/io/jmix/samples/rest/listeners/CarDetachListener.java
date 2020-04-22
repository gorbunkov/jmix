/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.listeners;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.data.PersistenceTools;
import io.jmix.data.listener.BeforeAttachEntityListener;
import io.jmix.data.listener.BeforeDetachEntityListener;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.Currency;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("jmix_CarDetachListener")
public class CarDetachListener implements BeforeDetachEntityListener<Car>, BeforeAttachEntityListener<Car> {

    @Inject
    private PersistenceTools persistenceTools;

    @Inject
    private EntityStates entityStates;

    @Inject
    private DataManager dataManager;

    @Override
    public void onBeforeDetach(Car entity) {
        // This is for testing the listener only. Usage of persistenceTools.getReferenceId() does not make sense here.
        PersistenceTools.RefId refId = persistenceTools.getReferenceId(entity, "currency");
        if (refId.isLoaded() && entityStates.isLoaded(entity, "currencyCode")) {
            entity.setCurrencyCode((String) refId.getValue());
        }
    }

    @Override
    public void onBeforeAttach(Car entity) {
        if (entityStates.isLoaded(entity, "currency") && entity.getCurrency() == null && entity.getCurrencyCode() != null) {
            Currency currency = dataManager.load(Currency.class)
                    .query("select c from ref_Currency c where c.code = :code")
                    .parameter("code", entity.getCurrencyCode())
                    .one();
            entity.setCurrency(currency);
        }
    }
}
