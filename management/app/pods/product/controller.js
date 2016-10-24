import Ember from 'ember';

export default Ember.Controller.extend({
  reviews: null,
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
