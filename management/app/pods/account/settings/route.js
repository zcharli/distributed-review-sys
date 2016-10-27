import Ember from 'ember';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default Ember.Route.extend(AuthenticatedRouteMixin, {
  session: Ember.inject.service("session"),
  model() {
    try {
      const localUser = localStorage['loggedInUser'] && JSON.parse(localStorage['loggedInUser']);
      console.log(localUser);
      return this.store.peekRecord("account", localUser.data[0].id);
    } catch (e) {
      console.log("Oops");
    }
  },

  onBeforeModel: function() {
  }.on("beforeModel")
});
