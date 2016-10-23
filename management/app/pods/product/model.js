import DS from 'ember-data';

export default DS.Model.extend({
  reviews: DS.hasMany('review', {async: true}),

  name: DS.attr('string')
});
