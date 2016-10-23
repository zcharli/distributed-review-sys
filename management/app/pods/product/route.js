import Ember from 'ember';

export default Ember.Route.extend({

  model() {
    let reviews = []
    return this.store.findAll('product').then((data) => {
      console.log(data);
      reviews = data.getEach('reviews');
      return reviews;
    });
  }
  // setupController(controller, model) {
  //   console.log("2");
  //   const productModel = this.modelFor('product');
  //   this.store.query('review', { param: productModel }).then((data) => {
  //     console.log(data);
  //   });
  // }
});
