import Ember from 'ember';

export default Ember.Route.extend({
  model(params, transition) {
    console.log(params, transition);
    return this.store.peekRecord("product", params.productid);
  }
});
