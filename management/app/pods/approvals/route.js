import Ember from 'ember';

export default Ember.Route.extend({
  constants: Ember.inject.service("constants"),

  model() {
    const url = this.get("constants.baseApi") + "/review/approval";
    console.log(url);
    return Ember.$.getJSON(url);
  }
});
