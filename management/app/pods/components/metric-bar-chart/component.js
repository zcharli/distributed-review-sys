import Ember from 'ember';
import ColorGenerator from 'mixins/color-generator';

export default Ember.Component.extend(ColorGenerator, {
  classNames: ['metric-bar-chart'],
  baseBarColor: "#2c2c2c",
  colorStep: 0.05,

  backgroundColors: Ember.computed('baseBarColor', 'model.values', function () {
    const numColorsToGen = this.get("model.values").length;
    const step = this.get("colorStep");
    const baseColor = this.get("baseBarColor");
    const colorCodes = [];
    const generateColor = this.get("generateColor");
    const modRollOver = 100 / step;
    for (let i = 0; i < numColorsToGen; i++) {
      colorCodes.push(generateColor(baseColor, ((i + 1) % modRollOver) * step));
    }
    return colorCodes;
  }),

  chartOptions: function () {
    return {
      responsive: true
    };
  },

  chartData: function () {
    return {
      data: this.get("model.values"),
      datasets: [{
        // fillColor: "rgb(44, 44, 44)",
        backgroundColor: this.get('backgroundColors'),
        strokeColor: 'black',
        data: this.get("model.labels"),
        label: this.get("model.name"),
      }]
    };
  }.property('model')
});
