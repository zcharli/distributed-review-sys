import DS from 'ember-data';

export default DS.Model.extend({
  product: DS.belongsTo('product'),

  id: DS.attr("string"),

  domainId: DS.attr('string'),
  locationId: DS.attr('string'),

  title: DS.attr('string'),
  content: DS.attr('string'),
  stars: DS.attr('number'),
  upvotes: DS.attr('number'),

  created: DS.attr('date')
});
