import Ember from 'ember';

export default Ember.Component.extend({
  previews: function () {
    const previews = this.get("data.previews");
    return previews.map(function(item){
      const urlSegment = item.get("url");
      console.log(urlSegment.objectAt(0));
      return {
        "identifier": item.get("identifier"),
        "title": item.get("title"),
        "value": item.get("value"),
        "route": urlSegment.objectAt(0)|| 'index',
        "routeParam": urlSegment.objectAt(0) ? urlSegment.objectAt(1) || "index" : "index"
      };
    });
  }.property('data'),

});
