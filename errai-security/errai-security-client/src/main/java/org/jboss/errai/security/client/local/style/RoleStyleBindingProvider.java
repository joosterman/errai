/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.client.local.style;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.shared.api.style.AnnotationStyleBindingExecutor;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Element;

/**
 * RoleStyleBindingProvider makes sure that client elements annotated by {@link RestrictedAccess} are made invisible for
 * users that do not have the role or roles specified.
 *
 * @see RestrictedAccess
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class RoleStyleBindingProvider {

  private final ActiveUserCache userProvider;

  @Inject
  public RoleStyleBindingProvider(final ActiveUserCache userProvider) {
    this.userProvider = userProvider;
  }

  @PostConstruct
  public void init() {
    StyleBindingsRegistry.get().addStyleBinding(this, RestrictedAccess.class, new AnnotationStyleBindingExecutor() {
      @Override
      public void invokeBinding(final Element element, final Annotation annotation) {
        final User user = userProvider.getUser();
        if (user == null || user.getRoles() == null || !hasRoles(user.getRoles(), ((RestrictedAccess) annotation).roles()))
          element.getStyle().setDisplay(Display.NONE);
        else
          element.getStyle().clearDisplay();
      }
    });
  }
  
  private boolean hasRoles(final Collection<Role> userRoles, final String[] requiredRoles) {
    final Set<String> userRolesByName = new HashSet<String>();
    for (final Role role : userRoles) {
      userRolesByName.add(role.getName());
    }

    for (int i = 0; i < requiredRoles.length; i++) {
      if (!userRolesByName.contains(requiredRoles[i]))
        return false;
    }
    
    return true;
  }

}
