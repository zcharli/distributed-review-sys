import Ember from 'ember';

export default Ember.Controller.extend({
  selected: 10,
  types: [10,25,50,100],

  numElementsPer: Ember.computed("selected", function() {
    return this.get('selected');
  }),

  hasRecordsToApprove: Ember.computed("model", function() {
    return this.get('model.length') > 0;
  }),

  tableSettings: function() {
    const model = this.get('model');
    return {
      component: "approval-form",
      numResultsPerPage: 10,
      data: model
    };
  }.property('model'),

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
