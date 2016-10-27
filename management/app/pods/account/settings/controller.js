import Ember from 'ember';

export default Ember.Controller.extend({
  newPassword: null,
  confirmPassword: null,
  isUploading: false,
  constants: Ember.inject.service("constants"),

  profileCompletion: 0,
  profileCompletionDisplay: Ember.computed("profileCompletion", function() {
    const completion = this.get("profileCompletion");
    console.log(completion);
    return completion;
  }),

  imgurPostObj: Ember.computed("model", function() {
    const userId = this.get("model.user_id");
    const api = this.get('constants.baseApi') + "/account/upload";
    return {
      url: api,
      data: {
        key: userId
      }
    };
  }),

  onInit: Ember.on("init", function(){
    Ember.$(".profile-upload").popup();
  }),

  actions: {
    showProgressBar() {
      this.set("isUploading", true);
    },

    saveAccount() {

    },

    profileUploadComplete(data) {
      console.log(data);
      console.log("uploadComplete");
    }
  }
});
