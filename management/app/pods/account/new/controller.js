import Ember from 'ember';

export default Ember.Controller.extend({
  notifications: Ember.inject.service('notification-messages'),
  session: Ember.inject.service('session'),

  actions: {
    createAccount() {
      let {identification, password, passwordConfirm} = this.getProperties('identification', 'password', 'passwordConfirm');
      if (password !== passwordConfirm) {
        this.set('errorMessage', "Passwords do not match!");
        return;
      }
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&]{8,}/;
      if (!passwordRegex.test(password)) {
        this.set('errorMessage', 'Your password must have at least one number, one uppercase, one lowercase, and one special character');
        return;
      }
      const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      if (!emailRegex.test(identification)) {
        this.set("errorMessage", "That email is invalid");
        return;
      }

      const options = {
        url: '/api/account/new',
        data: JSON.stringify({
          identification: identification,
          password: password
        }),
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json'
      };

      Ember.$.ajax(options).then((response) => {
        this.get('notifications').success('Account successfully created!', {
          autoClear: true,
          clearDuration: 1200
        });
        this.get('session').set("clientId", response.clientId);
        this.set('session.isAuthenticated', true);
        this.transitionToRoute('index');
      }, (xhr) => {
        this.set('errorMessage', xhr.errorMessage);
      });
    }
  }
});
