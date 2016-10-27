import Ember from 'ember';

export default Ember.Component.extend({
  classNames: ["side-nav"],

  onDomLoad: Ember.on('didInsertElement', function() {
    // Ember.$("#toc")
    //   .sidebar({
    //     dimPage          : true,
    //     transition       : 'overlay',
    //     mobileTransition : 'uncover'
    //   });
  }),

  actions: {
    logout() {
      this.sendAction("action");
    }
  }
});
