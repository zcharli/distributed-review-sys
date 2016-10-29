import DS from 'ember-data';

export default DS.Model.extend({

  name: DS.attr("string"),
  component: DS.attr("string"),
  position: DS.attr("string"),
  description: DS.attr("string"),
  icon: DS.attr("string"),
  value: DS.attr("string"),

  data: DS.attr("array"),
  labels: DS.attr("array"),

  previews: DS.hasMany("preview")
});
