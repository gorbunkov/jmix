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

package test_support.entity;

import javax.persistence.*;
import java.util.concurrent.atomic.AtomicLong;

@Entity(name = "test_NullableIdItemEntity")
@Table(name = "TEST_NULLABLE_ID_ITEM_ENTITY")
public class TestNullableIdItemEntity implements io.jmix.core.Entity {

    private static final long serialVersionUID = 8115973479078477156L;

    public static AtomicLong sequence = new AtomicLong();

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ID")
    private TestNullableIdEntity master;

    @PrePersist
    protected void onPrePersist() {
        if (id == null)
            id = sequence.incrementAndGet();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestNullableIdEntity getMaster() {
        return master;
    }

    public void setMaster(TestNullableIdEntity master) {
        this.master = master;
    }
}
