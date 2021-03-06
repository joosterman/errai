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
package org.jboss.errai.security.shared.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.AuthenticationException;

/**
 * AuthenticationService service for authenticating users and getting their roles.
 *
 * @author edewit@redhat.com
 */
@Remote
public interface AuthenticationService {

  /**
   * Login with the given username and password.
   * 
   * @param username The username to log in with.
   * @param password The password to authenticate with.
   * @return The logged in {@link User}.
   * @throws Implementations should throw an {@link AuthenticationException} if authentication fails.
   */
  public User login(String username, String password);

  /**
   * @return True iff the user is currently logged in.
   */
  public boolean isLoggedIn();

  /**
   * Log out the currently authenticated user.
   */
  public void logout();

  /**
   * Get the currently authenitcated user.
   * 
   * @return The currently authenticated user, or {@code null} if no user is authenticated.
   */
  public User getUser();
}
