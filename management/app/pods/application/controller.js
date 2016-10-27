import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service('session'),
  classNames: ["fullscreen"],

  actions: {
    invalidateSession() {
      console.log("hit");
      console.log(this.get("actions"));
      Ember.run.next(function() {
        this.send('sessionInvalidate');
      }.bind(this));
      return true;
    }
  }
});
