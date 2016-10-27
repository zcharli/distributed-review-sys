import Ember from 'ember';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default Ember.Route.extend(AuthenticatedRouteMixin, {
  constants: Ember.inject.service("constants"),

  model() {
    const url = this.get("constants.baseApi") + "/review/approval";
    console.log(url);
    return Ember.$.getJSON(url).then((res) => {
      const results = res.results;
      const newModel = [];

      for (let i = 0; i < res.results.length; ++i) {
        newModel.pushObject({
          parentLevel: {
            name: results[i].description || "(not named)",
            identifier: results[i].barcode || "(no indentifier)",
          },
          childLevel: results[i] // returns the ember classes for each review
        });
      }
      return newModel;
    });
  }
});
