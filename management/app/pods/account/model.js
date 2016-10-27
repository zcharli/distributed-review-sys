import DS from 'ember-data';

export default DS.Model.extend({
  email: DS.attr("string"),
  fname: DS.attr("string"),
  lname: DS.attr("string")
});
