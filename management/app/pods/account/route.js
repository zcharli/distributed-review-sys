import Ember from 'ember';

export default Ember.Route.extend({
  session: Ember.inject.service("session"),

  beforeModel() {
    if (!this.get('session.isAuthenticated')) {
      this.transitionTo("account.new");
    } else {
      return this._super(...arguments);
    }
  }
});
