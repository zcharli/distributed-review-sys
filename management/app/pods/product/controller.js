import Ember from 'ember';

export default Ember.Controller.extend({

  init() {
    console.log("product controller loaded");
  },

  onModelLoad: function() {
    console.log("model loaded");
    console.log(this.get('model'));
  }.observes("model")
});
