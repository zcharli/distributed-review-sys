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

  userName: Ember.computed("user.fullName", function() {
    if (this.get("user.fullName") === " ") {
      return false;
    }
    return Ember.String.htmlSafe(this.get("user.fullName"));
  }),

  hasUserProfile: Ember.computed("user.profile", function() {
    const profileUrl = this.get("user.profile");
    if (profileUrl) {
      return profileUrl;
    }
    return false;
  }),

  actions: {
    logout() {
      this.sendAction("action");
    }
  }
});
