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

      const endpoint = 'http://134.117.26.135:9090';
      const options = {
        url: endpoint + '/api/account/new',
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
        if (response.status === 200) {
          this.get('session').authenticate('authenticator:drsauth', identification, password)
            .then(() => {
              var loginResult = this.get("session.session.content.authenticated");
              if (loginResult) {
                const sessionStore = this.get("session.store");
                sessionStore.set('account', loginResult.result);
                const newUser = {
                  data: [{
                    id: loginResult.result.user_id,
                    type: 'account',
                    attributes: loginResult.result,
                    relationships: {}
                  }]
                };
                localStorage["loggedInUser"] = JSON.stringify(newUser);
                sessionStore.set('accountId', loginResult.result.user_id);
                this.store.pushPayload(newUser);
              } else {
                this.set('errorMessage', "Oops, an error occured while authenticating.");
              }
              // this.set('session.isAuthenticated', true);
            })
            .catch(() => {
              this.set('errorMessage', "Oops, an error occured while authenticating.");
            });
        } else {
          this.set('errorMessage', "Oops, an error occured while creating your account.");
        }
      }, () => {
        this.set('errorMessage', "Oops, an error occured while creating your account.");
      });
    }
  }
});
