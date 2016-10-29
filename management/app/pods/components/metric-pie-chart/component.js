import Ember from 'ember';
import ColorGenerator from '../../../mixins/color-generator';

export default Ember.Component.extend(ColorGenerator, {
  classNames: ['metric-pie-chart', "fullwidth"],
  baseBarColor: "#2c2c2c",
  colorStep: 0.6,
  hoverLightRatio: 0.6,

  backgroundColors: Ember.computed('baseBarColor', 'data.values', function () {
    const numColorsToGen = this.get("data.values").length;
    const step = this.get("colorStep");
    const baseColor = this.get("baseBarColor");
    const colorCodes = [];
    const generateColor = this.get("generateColor");
    // const modRollOver = 100 / step;
    for (let i = 0; i < numColorsToGen; i++) {
      colorCodes.push(generateColor(baseColor, (i + 1) * step));
    }
    return colorCodes;
  }),

  hoverBackgroundColor: function () {
    const colors = this.get("backgroundColors");
    const hoverColor = [];
    const hoverLight = this.get("hoverLightRatio");
    const generateColor = this.get("generateColor");
    for (let i = 0; i < colors.length; ++i) {
      hoverColor.push(generateColor(colors[i], hoverLight));
    }
    return hoverColor;
  }.property("backgroundColors"),

  chartOptions: function () {
    return {
      responsive: true
    };
  },

  valuesToNumber: function () {
    return this.get("data.values").map(function (number) {
      return parseInt(number);
    });
  }.property("data.values"),

  chartData: function () {
    return {
      labels: this.get("data.labels"),
      datasets: [{
        // fillColor: "rgb(44, 44, 44)",
        backgroundColor: this.get('backgroundColors'),
        hoverBackgroundColor: this.get('hoverBackgroundColor'),
        strokeColor: 'black',
        data: this.get("data.values"),
      }]
    };
  }.property('data')
});
