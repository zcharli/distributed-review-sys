import Ember from 'ember';

export default Ember.Component.extend({
  disableProductEditing: Ember.computed("settings.disabledProductEditing", function() {
    return this.get("settings.disabledProductEditing");
  }),
  disableReviewEditing: Ember.computed("settings.disableReviewEditing", function() {
    return this.get("settings.disableReviewEditing");
  }),

  onDomLoad: Ember.on('didInsertElement', function() {
    Ember.$(".ui.star.rating.disabled.review").rating('disable');
  }),

  actions: {
    submitChanges() {
      console.log("submitted");
    }
  }
});
