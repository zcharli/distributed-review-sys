import Ember from "ember";
import ApplicationRouteMixin from 'ember-simple-auth/mixins/application-route-mixin';

export default Ember.Route.extend(ApplicationRouteMixin, {
  session: Ember.inject.service("session"),
  constants: Ember.inject.service("constants"),

  model() {
    if (this.get("session.isAuthenticated")) {
      const user = this.get("session.account");
      console.log(user);
      if (!user) {
        this.get("session").invalidate();
      } else {
        this.store.pushPayload({account: user});
      }
    }
  },

  actions: {
    invalidateSession() {
      this.session.invalidate();
    }
  }
});
