import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service('session'),

  actions: {
    authenticate() {
      let {identification, password} = this.getProperties('identification', 'password');
      this.get('session').authenticate('authenticator:drsauth', identification, password)
        .then((response) => {
          if (response.staus === 200 && !!response.clientId) {
            this.get('session').set("clientId", response.clientId);
            this.set('session.isAuthenticated', true);
          } else {
            this.set('errorMessage', response.responseText);
          }
        })
        .catch((reason) => {
          this.set('errorMessage', reason.error || reason);
        });
    }
  }
});
