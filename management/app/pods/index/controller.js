import Ember from 'ember';

export default Ember.Controller.extend({

  topStats: Ember.computed("model", function() {
    return this.get("model").filterBy("position", "top");
  }),
  mainBarChart: Ember.computed("model", function() {
    return this.get("model").filterBy("position", "center");
  }),
  midTopTens: Ember.computed("model", function() {
    return this.get("model").filterBy("position", "middle");
  }),
  bottomStatistics: Ember.computed("model", function() {
    return this.get("model").filterBy("position", "bottom");
  }),


});
