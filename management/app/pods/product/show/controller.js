import Ember from 'ember';

export default Ember.Controller.extend({

  productName: Ember.computed("model", function() {
    return Ember.String.htmlSafe("Reviews for product: " + (this.get("model.name") || "<no name>") + "");
  }),

  productIdentifier: Ember.computed("model", function() {
    return Ember.String.htmlSafe("Identified by code: " + this.get("model.identifier"));
  }),

  tableSettings: function() {
    const model = this.get('model');
    return {
      numResultsPerPage: 10,
      innerTableSettings: {
        numResultsPerPage: 10,
        columnOrder: ['barcode', 'type', 'title', 'review_content', 'stars', 'upvotes', 'description', 'created_at'],
        disabledProductEditing: false,
        disableReviewEditing: true
      },
      headers: ["Identifier", "Type", "Title", "Review", "Stars", "Up Votes", "Description", "Created"],
      data: {
        childLevel: model.get("reviews").toArray()
      }
    };
  }.property('model'),
});
