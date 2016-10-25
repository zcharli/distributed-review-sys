import Ember from 'ember';
var inflector = Ember.Inflector.inflector;

export default Ember.Component.extend({

  onDomLoad: Ember.on("didInsertElement", function() {
    const api = this.get('api');
    const self = this;
    console.log(this.get('action'));
    Ember.$("#" + this.elementId + " .search-api").search({
      apiSettings: {
        url: api + '/search?q={query}'
      },
      searchDelay: 200,
      type: 'category',
      onSelect: function(result, response) {
        console.log(result, response);
        if (!(result && result.route)) {
          return;
        }
        console.log(self.get('action'));
        self.sendAction("action", result.route, result.param);
      }
    });
  }),

  placeholder: function() {
    const settings = this.get("settings");
    return "Search " + inflector.pluralize(settings.resource);
  }.property('settings'),

  api: function() {
    const settings = this.get("settings");
    if (!(settings && settings.api && settings.resource)) {
      return;
    }
    return settings.api + "/" + settings.resource;
  }.property('settings')

});
