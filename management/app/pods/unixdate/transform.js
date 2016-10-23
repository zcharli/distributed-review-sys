import DS from 'ember-data';

export default DS.DateTransform.extend({
  deserialize(serialized) {
    console.log(serialized);
    console.log(new Date(serialized));
    return new Date(serialized);
  },

  serialize(deserialized) {
    return deserialized.valueOf();
  }
});
