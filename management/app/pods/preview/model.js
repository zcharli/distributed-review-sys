import DS from 'ember-data';

export default DS.Model.extend({
  identifier: DS.attr('string'),
  title: DS.attr('string'),
  value: DS.attr('string'),
  type: DS.attr('string'),
  url: DS.attr('array')
});
