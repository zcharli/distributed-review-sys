import Ember from "ember";
import ApplicationRouteMixin from 'ember-simple-auth/mixins/application-route-mixin';

export default Ember.Route.extend(ApplicationRouteMixin, {
  session: Ember.inject.service("session"),
  constants: Ember.inject.service("constants"),

  model() {
    console.log("application model hit");
    if (this.get("session.isAuthenticated")) {
      const userJson = localStorage["loggedInUser"];
      if (!userJson) {
        this.get("session").invalidate();
      } else {
        const user = JSON.parse(userJson);
        console.log(user);
        this.store.pushPayload(user);
        return this.store.peekRecord("account", user.data[0].id);
      }
    } else {
      console.log("not authenticated");
    }
  },

  hasLoggedIn: Ember.computed("session.isAuthenticated", function () {
    console.log("session.isAuthenticated");
  }),


  actions: {
    didTransition() {
      console.log("transitioned");
      if (!this.controller.get("model") && this.get("session.isAuthenticated")) {
        const userJson = localStorage["loggedInUser"];
        if (!userJson) {
          this.get("session").invalidate();
        } else {
          const user = JSON.parse(userJson);
          console.log(user);
          this.store.pushPayload(user);
          const account = this.store.peekRecord("account", user.data[0].id);
          this.controller.set("model", account);
        }
      }
    },

    sessionInvalidate() {
      console.log("application route invalidating the session");
      delete localStorage["loggedInUser"];
      this.get("session").invalidate();
    }
  }
});
