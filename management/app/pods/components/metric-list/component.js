import Ember from 'ember';

export default Ember.Component.extend({
  previews: function () {
    return this.get("data.previews");
  }.property('data'),


});
