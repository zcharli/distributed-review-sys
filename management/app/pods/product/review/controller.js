import Ember from 'ember';

export default Ember.Controller.extend({

  reviewTitle: Ember.computed("model", function() {
    return Ember.String.htmlSafe("Review: " + this.get("model.title"));
  }),

  reviewIdentifier: Ember.computed("model", function() {
    return Ember.String.htmlSafe("Identified by code: " + this.get("model.id"));
  }),

});
