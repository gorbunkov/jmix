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

package io.jmix.security.impl;

import io.jmix.core.FetchPlanRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.security.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class StandardUserDetailsService implements UserDetailsService {

    private EntityManagerFactory entityManagerFactory;

    private FetchPlanRepository fetchPlanRepository;

    public StandardUserDetailsService(EntityManagerFactory entityManagerFactory, FetchPlanRepository fetchPlanRepository) {
        this.entityManagerFactory = entityManagerFactory;
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<User> users = entityManager.createQuery("select u from sec_User u where u.loginLowerCase = ?1", User.class)
                    .setParameter(1, username)
                    .setHint(PersistenceHints.FETCH_PLAN, fetchPlanRepository.getFetchPlan(User.class, "user-login"))
                    .getResultList();

        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
