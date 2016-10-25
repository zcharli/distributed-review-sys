import DS from 'ember-data';

export default DS.Model.extend({
  reviews: DS.hasMany('review', {async: true}),

  type: DS.attr('string'),
  name: DS.attr('string'),
  identifier: DS.attr('string')
});
