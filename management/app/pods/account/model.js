import DS from 'ember-data';
import Ember from 'ember';
export default DS.Model.extend({
  email: DS.attr("string"),
  fname: DS.attr("string"),
  lname: DS.attr("string"),
  profile: DS.attr("string"),
  token: DS.attr("string"),
  user_id: DS.attr("string"),
  password: DS.attr("string"),

  fullName: Ember.computed('fname', 'lname', function() {
    const fname = this.get("fname") || "";
    const lname = this.get("lname") || "";
    return fname + " " + lname ;
  })

});
