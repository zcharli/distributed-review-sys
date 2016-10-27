import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service('session'),

  actions: {
    authenticate() {
      let {identification, password} = this.getProperties('identification', 'password');
      this.get('session').authenticate('authenticator:drsauth', identification, password)
        .then(() => {
          var loginResult = this.get("session.session.content.authenticated");

          if (loginResult) {
            const sessionStore = this.get("session.store");
            sessionStore.set('account', loginResult.result);
            const newUser = {
              data: [{
                id: loginResult.result.id,
                type: 'account',
                attributes: loginResult.result,
                relationships: {}
              }]
            };
            localStorage["loggedInUser"] = JSON.stringify(newUser);
            const id = loginResult.result.id;
            sessionStore.set('accountId', id);
            delete loginResult.result.id;
            this.store.pushPayload(newUser);
          } else {
            this.set('errorMessage', "Oops, an error occured while authenticating.");
          }
          // this.set('session.isAuthenticated', true);
        })
        .catch(() => {
          this.set('errorMessage', "Oops, an error occured while authenticating.");
        });
    }
  }
});
