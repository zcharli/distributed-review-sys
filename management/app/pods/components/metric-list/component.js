import Ember from 'ember';

export default Ember.Component.extend({
  previews: function () {
    const previews = this.get("data.previews");
    return previews.map(function(item){
      console.log(item.get("title"));
      return {
        "identifier": item.get("identifier"),
        "title": item.get("title"),
        "value": item.get("value"),
        "route": item.get("url").objectAt(0) || 'index',
        "routeParam": item.get("item.url").objectAt(0) ? item.get("item.url").objectAt(1) || "index" : "index"
      };
    });
  }.property('data'),

});
