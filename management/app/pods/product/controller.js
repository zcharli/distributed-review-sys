import Ember from 'ember';
import Base from 'semantic-ui-ember/mixins/base';

export default Ember.Controller.extend(Base, {
  constants: Ember.inject.service('constants'),
  reviews: null,
  selected: 10,
  types: [10,25,50,100],

  numElementsPer: Ember.computed("selected", function() {
    return this.get('selected');
  }),

  searchSettings: function() {
    return {
      api: this.get('constants.baseApi'),
      resource: "product"
    };
  }.property('model', 'constants'),

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
      data: model
    };
  }.property('model'),

  init() {
    console.log("product controller loaded");
  },

  actions: {
    transition(route, param) {
      console.log("got action", route);
      if (!route) {
        return;
      }
      try {
        if (param) {
          this.transitionToRoute(route, param);
        } else {
          this.transitionToRoute(route);
        }
      } catch(e) {}
      return false;
    }
  }
});
