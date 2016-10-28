import Ember from 'ember';

export default Ember.Controller.extend({
  constants: Ember.inject.service("constants"),
  notifications: Ember.inject.service('notification-messages'),

  errorMessage: null,
  newPassword: null,
  confirmPassword: null,
  isUploading: false,

  profilePicture: Ember.computed("model.profile", function () {
    const profileUrl = this.get('model.profile');
    if (profileUrl) {
      return profileUrl;
    } else {
      return false;
    }
  }),

  profileCompletion: 0,
  profileCompletionDisplay: Ember.computed("profileCompletion", function () {
    const completion = this.get("profileCompletion");
    return completion;
  }),

  imgurPostObj: Ember.computed("model", function () {
    const email = this.get("model.email");
    const api = this.get('constants.baseApi') + "/account/upload";
    return {
      url: api,
      data: {
        key: email
      }
    };
  }),

  onInit: Ember.on("init", function () {
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
      const newPassword = this.get("newPassword");
      const oldPassword = this.get("oldPassword");
      let changePassword = false;
      if (newPassword) {
        if (newPassword !== oldPassword) {
          this.set("errorMessage", "New passwords do not match.");
          return;
        }
        changePassword = true;
        this.set("model.password", newPassword);
      }
      const url = this.get("constants.baseApi") + "/account/update";
      let data = this.get("model");
      var user = JSON.parse(localStorage['loggedInUser']|| {});
      if (user.data) {
        data.set("user_id", user.data[0].id);
      }
      data = JSON.parse(JSON.stringify(data));
      if (!changePassword) {
        delete data.password;
      }
      const opts = {
        method: "PUT",
        url: url,
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify(data)
      };
      Ember.$.ajax(opts).then((response) => {
        if (response.status === 200) {
          this.get('notifications').success('Account successfully updated!', {
            autoClear: true,
            clearDuration: 1200
          });
          const realModel = this.get("model");
          localStorage["loggedInUser"] = JSON.stringify({
            data: [{
              id: realModel.user_id,
              type: "account",
              attributes: realModel
            }]
          });
        } else {
          this.set("errorMessage", response.responseText);
        }
      });
    },

    profileUploadComplete(data) {
      this.set("model.profile", data.message);
      let currentUser = JSON.parse(localStorage["loggedInUser"] || {});
      if (currentUser.data) {
        currentUser.data[0].attributes.profile = data.message;
      }
      localStorage["loggedInUser"] = JSON.stringify(currentUser);
    }
  }
});
