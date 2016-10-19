import DS from 'ember-data';

export default DS.Model.extend({
  reviews: DS.hasMany('review'),

  identifier: DS.attr('string'),
  name: DS.attr('string')
});
