import Ember from 'ember';

export default Ember.Service.extend({
  backendApi: "http://134.117.26.135:9090",
  namespace: "/api",

  baseApi: Ember.computed('backendApi', 'namespace', function() {
    return this.get('backendApi') + this.get("namespace");
  })
});
