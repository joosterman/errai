package org.jboss.errai.demo.todo.client.local;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.RegistrationResult;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path="signup")
public class SignupPage extends Composite {

  @Inject private @DataField Label overallErrorMessage;

  @Inject private @Model User user;
  @Inject private @Bound @DataField TextBox shortName;
  @Inject private @Bound @DataField TextBox fullName;
  @Inject private @Bound @DataField TextBox email;

  @Inject private @DataField PasswordTextBox password1;
  @Inject private @DataField PasswordTextBox password2;

  @Inject private @DataField Button signupButton;

  @Inject private Caller<SignupService> signupService;

  @Inject private TransitionTo<TodoListPage> todoListPageLink;

  @Inject private Validator validator;

  @PostConstruct
  private void init() {
    overallErrorMessage.setVisible(false);
  }

  @EventHandler("signupButton")
  private void doSignup(ClickEvent e) {
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    if (violations.size() > 0) {
      ConstraintViolation<User> violation = violations.iterator().next();
      overallErrorMessage.setText(violation.getPropertyPath() + " " + violation.getMessage());
      overallErrorMessage.setVisible(true);
      return;
    }

    // Passwords are not part of the user bean, so we do manual validation for these fields
    if (password1.getText().length() < 8) {
      overallErrorMessage.setText("Password must be at least 8 characters");
      overallErrorMessage.setVisible(true);
      return;
    }

    if (!password1.getText().equals(password2.getText())) {
      overallErrorMessage.setText("Password fields do not match");
      overallErrorMessage.setVisible(true);
      return;
    }

    try {
      signupService.call(new RemoteCallback<RegistrationResult>() {
        @Override
        public void callback(final RegistrationResult response) {
          todoListPageLink.go();
        }
      },
      new BusErrorCallback() {
        @Override
        public boolean error(Message message, Throwable throwable) {
          overallErrorMessage.setText(throwable.getMessage());
          overallErrorMessage.setVisible(true);
          return false;
        }
      }).register(user, password1.getText());
    } catch (RegistrationException e1) {
      // won't happen for async remote call
    }
  }

}
