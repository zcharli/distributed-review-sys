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
    const id = this.get("model.id");
    const api = this.get('constants.baseApi') + "/account/upload";
    return {
      url: api,
      data: {
        key: id
      }
    };
  }),

  onInit: Ember.on("init", function(){
    Ember.$(".profile-upload").popup();
  }),

  actions: {

    handleUploadError(jqXHR, textStatus, errorThrown) {
      // Handle unsuccessful upload
      console.log(jqXHR, textStatus, errorThrown);
    },

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
