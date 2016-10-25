import Ember from 'ember';

export default Ember.Route.extend({

  model(params, transition) {
    console.log(params, transition);
    return this.store.findAll('product').then((data) => {
      console.log(data);
      // We need to do a loop to combine our data unfortunately
      const productNames = data.getEach('name');
      const productIdentifiers = data.getEach('identifier');
      const productReviews = data.getEach('reviews');
      // both lengths must be equal and order is preserved.

      const newModel = [];

      for (let i = 0; i < productNames.length; ++i) {
        newModel.pushObject({
          parentLevel: {
            name: productNames[i] || "(not named)",
            identifier: productIdentifiers[i] || "(no indentifier)",
          },
          childLevel: productReviews[i].toArray() // returns the ember classes for each review
        });
      }
      return newModel;
    });
  },
  setupController(controller, model) {
    controller.set('model', model);
  }
});
