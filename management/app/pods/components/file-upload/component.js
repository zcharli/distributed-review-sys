import Ember from 'ember';
import EmberUploader from 'ember-uploader';

export default EmberUploader.FileField.extend({
  filesDidChange: function(files) {
    const postObj = this.get("url");
    console.log(postObj);
    const uploader = EmberUploader.Uploader.create({
      url: postObj.url
    });
    const self = this;
    if (!Ember.isEmpty(files)) {
      // this second argument is optional and can to be sent as extra data with the upload
      uploader.upload(files[0], { name: postObj.data.key });
      self.sendAction("uploadStart");
      uploader.on('progress', e => {
        console.log(e.percent);
        self.set('completion', e.percent);
      });

      uploader.on('didUpload', e => {
        console.log(e);
        self.sendAction("uploadComplete", e);
      });

      uploader.on('didError', (jqXHR, textStatus, errorThrown) => {
        // Handle unsuccessful upload
        console.log(jqXHR, textStatus, errorThrown);
      });
    }
  }
});