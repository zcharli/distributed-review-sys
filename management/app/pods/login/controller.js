import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service('session'),

  actions: {
    authenticate() {
      let {identification, password} = this.getProperties('identification', 'password');
      this.get('session').authenticate('authenticator:drsauth', identification, password)
        .then((response) => {
          if (response.status === 200 && response.result) {
            this.set('session.account', response.result);

            const id = response.result.id;
            this.set('session.accountId', id);
            delete response.result.id;
            this.store.pushPayload({
              data: [{
                id: id,
                type: 'account',
                attributes: response.result,
                relationships: {}
              }]
            });
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
