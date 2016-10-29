import Ember from 'ember';
import ColorGenerator from '../../../mixins/color-generator';

export default Ember.Component.extend(ColorGenerator, {
  classNames: ['metric-pie-chart', "fullwidth"],
  baseBarColor: "#2c2c2c",
  colorStep: 0.20,

  backgroundColors: Ember.computed('baseBarColor', 'data.values', function () {
    const numColorsToGen = this.get("data.values").length;
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
      labels: this.get("data.values"),
      datasets: [{
        // fillColor: "rgb(44, 44, 44)",
        backgroundColor: this.get('backgroundColors'),
        strokeColor: 'black',
        data: this.get("data.values"),
      }]
    };
  }.property('data')
});
