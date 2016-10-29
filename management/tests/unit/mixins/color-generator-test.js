import Ember from 'ember';
import ColorGeneratorMixin from 'management/mixins/color-generator';
import { module, test } from 'qunit';

module('Unit | Mixin | color generator');

// Replace this with your real tests.
test('it works', function(assert) {
  let ColorGeneratorObject = Ember.Object.extend(ColorGeneratorMixin);
  let subject = ColorGeneratorObject.create();
  assert.ok(subject);
});
