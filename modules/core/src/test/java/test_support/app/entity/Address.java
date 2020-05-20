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

package test_support.app.entity;


import io.jmix.core.Entity;
import io.jmix.core.metamodel.annotations.InstanceName;
import io.jmix.core.metamodel.annotations.ModelObject;
import org.apache.commons.lang3.LocaleUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Locale;

@Embeddable
@ModelObject(name = "app_Address")
public class Address implements Entity {

    private static final long serialVersionUID = 3973674066005826186L;

    @Column(name = "CITY")
    protected String city;

    @Column(name = "ZIP", length = 10)
    protected String zip;

    @InstanceName(relatedProperties = {"city","zip"})
    public String getName(Locale locale) {
        if (LocaleUtils.toLocale("ru").equals(locale))
            return "Город: " + city + ", индекс: " + zip;

        return "City: " + city + ", zip: " + zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
