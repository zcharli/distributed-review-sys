import Ember from 'ember';

export default Ember.Controller.extend({
  constants: Ember.service.inject(),
  reviews: null,

  searchSettings: function() {
    return {
      api: this.get('constants.baseApi'),
      resource: "product"
    };
  }.property('model', 'constants'),

  tableSettings: function() {
    const model = this.get('model');
    console.log(model);
    return {
      numResultsPerPage: 10,
      innerTableSettings: {
        numResultsPerPage: 10,
        columnOrder: ['barcode', 'type', 'title', 'review_content', 'stars', 'upvotes', 'description', 'created_at'],
        disabledProductEditing: false,
        disableReviewEditing: true
      },
      headers: ["Identifier", "Type", "Title", "Review", "Stars", "Up Votes", "Description", "Created"],
      data: model
    };
  }.property('model'),

  init() {
    console.log("product controller loaded");
  }
});
