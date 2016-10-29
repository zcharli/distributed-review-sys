import Ember from 'ember';

export default Ember.Component.extend({
  previews: function () {
    const previews = this.get("data.previews");
    return previews.map(function(item){
      const urlSegment = item.get("url");
      return {
        "identifier": item.get("identifier"),
        "title": item.get("title"),
        "value": item.get("value") > 1477718800000 ?
          new Date(parseInt(item.get('value'))).toLocaleTimeString("en-us", {
          weekday: "long", year: "numeric", month: "short", timezone: "America/New_York",
          day: "numeric", hour: "2-digit", minute: "2-digit"
        }) : item.get("value"),
        "route": urlSegment.objectAt(0)|| 'index',
        "routeParam": urlSegment.objectAt(0) ? urlSegment.objectAt(1) || "index" : "index"
      };
    });
  }.property('data'),

});
