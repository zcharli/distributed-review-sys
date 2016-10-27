import Ember from "ember";
import ApplicationRouteMixin from 'ember-simple-auth/mixins/application-route-mixin';

export default Ember.Route.extend(ApplicationRouteMixin, {
  session: Ember.inject.service("session"),
  constants: Ember.inject.service("constants"),

  model() {
    if (this.get("session.isAuthenticated")) {
      const userJson = localStorage["loggedInUser"];
      if (!userJson) {
        this.get("session").invalidate();
      } else {
        const user = JSON.parse(userJson);
        this.store.pushPayload(user);
        return this.store.peekRecord("account", user.user_id);
      }
    }
  },

  actions: {
    sessionInvalidate() {
      console.log("application route invalidating the session");
      delete localStorage["loggedInUser"];
      this.get("session").invalidate();
    }
  }
});
