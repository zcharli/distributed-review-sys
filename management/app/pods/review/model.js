import DS from 'ember-data';

export default DS.Model.extend({
  product: DS.belongsTo('product'),

  title: DS.attr('string'),
  review_content: DS.attr('string'),
  stars: DS.attr('number'),
  upvotes: DS.attr('number'),
  downvotes: DS.attr('number'),
  description: DS.attr('string'),
  barcode: DS.attr('string'),
  type: DS.attr("string"),

  created_at: DS.attr('date'),

  contentId: DS.attr('string'),
  domainId: DS.attr('string'),
  locationId: DS.attr('string'),
});
